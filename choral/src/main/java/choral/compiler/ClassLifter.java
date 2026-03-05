package choral.compiler;

import choral.ast.CompilationUnit;
import choral.ast.Name;
import choral.ast.Position;
import choral.ast.body.*;
import choral.ast.body.Class;
import choral.ast.statement.NilStatement;
import choral.ast.type.FormalTypeParameter;
import choral.ast.type.FormalWorldParameter;
import choral.ast.type.TypeExpression;
import choral.ast.type.WorldArgument;

import java.lang.Enum;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;

class LiftException extends Exception {
	LiftException( String message ) {
		super( message );
	}

	static LiftException array() {
		return new LiftException( "array" );
	}

	static LiftException wildcard() {
		return new LiftException( "wildcard" );
	}

	static LiftException innerClass() {
		return new LiftException( "inner class" );
	}
}

/**
 * ClassLifter is responsible for "lifting" Java class files into Choral's internal AST
 * representation. This allows Choral code to interact with existing Java classes without
 * needing to declare Choral headers for them manually.
 */
public class ClassLifter {

	///////////////////// CONSTANTS /////////////////////

	private static final Position NOWHERE = new Position( null, 0, 0 );

	private static final FormalWorldParameter DEFAULT_WORLD_PARAMETER =
			new FormalWorldParameter( new Name( "A", NOWHERE ), NOWHERE );

	private static final WorldArgument DEFAULT_WORLD_ARGUMENT =
			new WorldArgument( new Name( "A", NOWHERE ), NOWHERE );

	private static final Set< String > trackedCompilationUnits = new HashSet<>( List.of(
			"java.lang.Object", "java.io.Serializable", "java.lang.Enum" ) );



	/////////////////////////////////////////////////////////////////////
	////////////////////// CLASS LIFTING METHODS  ///////////////////////
	/////////////////////////////////////////////////////////////////////


	/**
	 * Finds the given type and lifts it into a choral CompilationUnit.
	 *
	 * @param fullyQualifiedName The fully qualified name of class to be lifted.
	 * @return Compilation units for the class and all its public dependencies.
	 */
	public static Stream< CompilationUnit > liftPackage( String fullyQualifiedName ) {
		List< CompilationUnit > compilationUnitAccumulator = new ArrayList<>();
		liftPackage( fullyQualifiedName, compilationUnitAccumulator );
		return compilationUnitAccumulator.stream();
	}

	// Helper method to avoid passing empty mutable list to `liftPackage()` method
	private static void liftPackage(
			String fullyQualifiedName, List< CompilationUnit > compilationUnitAccumulator ) {
		//System.out.println( "Lifting class: " + fullyQualifiedName );

		try {
			java.lang.Class<?> clazz = java.lang.Class.forName( fullyQualifiedName );

			// Skip inner classes
			if( clazz.isMemberClass() ) {
				// System.err.println( "WARNING: Class lifter does not support inner classes, skipping: " +
				// 		fullyQualifiedName );
				return;
			}

			trackedCompilationUnits.add( clazz.getName() );

			if( clazz.isEnum() ) {
				liftEnum( clazz, compilationUnitAccumulator );
			} else if( clazz.isInterface() ) {
				liftInterface( clazz, compilationUnitAccumulator );
			} else {
				liftClass( clazz, compilationUnitAccumulator );
			}
		} catch( ClassNotFoundException e ) {
			System.err.println( "WARNING: Could not find class: " + fullyQualifiedName );
			throw new RuntimeException( "Could not find class: " + fullyQualifiedName, e );
		}
	}

	private static void liftClass(
			java.lang.Class< ? > clazz,
			List< CompilationUnit > compilationUnitAccumulator
	) {
		// for keeping track of which dependencies to lift
		Set< String > dependencyIdentifiers = new HashSet<>();

		// TRANSLATE FIELDS
		java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
		List< Field > choralFields = new ArrayList<>();
		for( java.lang.reflect.Field field : fields ) {
			// private fields will never be accessed
			if( Modifier.isPrivate( field.getModifiers() ) ) continue;

			EnumSet< FieldModifier > modifiers = parseModifiers( FieldModifier.class,
					field.getModifiers() );

			TypeExpression fieldTypeExpression;
			try {
				fieldTypeExpression = liftType( field.getGenericType() );
			} catch( LiftException e ) {
				warn( field.getName(), e );
				continue;
			}

			// add fields type to depencies
			extractClassDependencies( dependencyIdentifiers, field.getGenericType() );

			Field choralField = new Field(
					new Name( field.getName() ),
					fieldTypeExpression,
					Collections.emptyList(), // ignore annotations for now
					modifiers,
					NOWHERE );
			choralFields.add( choralField );
		}

		// TRANSLATE METHODS
		List< ClassMethodDefinition > methods = liftMethods(
				clazz.getDeclaredMethods(),
				ClassMethodModifier.class,
				dependencyIdentifiers,
				( signature, modifiers ) -> new ClassMethodDefinition(
						signature,
						new NilStatement( NOWHERE ),
						Collections.emptyList(),
						modifiers,
						NOWHERE )
		);

		// TRANSLATE CONSTRUCTORS
		List< ConstructorDefinition > choralConstructors = liftConstructors(
				clazz.getConstructors(),
				ConstructorModifier.class,
				dependencyIdentifiers,
				( signature, modifiers) -> new ConstructorDefinition(
					signature,
					null, // Represents calling `this()` or `super()` at the start of a constructor
					new NilStatement( NOWHERE ),
					Collections.emptyList(),
					modifiers,
					NOWHERE )
		);

		// TRANSLATE TYPE PARAMETERS
		List< FormalTypeParameter > choralTypeParameters;
		try {
			choralTypeParameters = liftTypeParameters( clazz.getTypeParameters() );
		} catch( LiftException e ) {
			warn( clazz.getSimpleName(), e );
			return;
		}

		// TRANSLATE SUPERINTERFACES
		java.lang.reflect.Type[] genericInterfaces = clazz.getGenericInterfaces();
		List< TypeExpression > parentInterfaces = new ArrayList<>();
		for( java.lang.reflect.Type genericInterface : genericInterfaces ) {
			try {
				parentInterfaces.add( liftType( genericInterface ) );
			} catch( LiftException e ) {
				warn( genericInterface.toString(), e );
			}
		}

		// TRANSLATE SUPERCLASS
		TypeExpression extendedExpression = null;
		java.lang.reflect.Type genericSuperclass = clazz.getGenericSuperclass();
		if( genericSuperclass != null ) {
			try {
				extendedExpression = liftType( genericSuperclass );
			} catch( LiftException e ) {
				warn( clazz.getSuperclass() != null ? clazz.getSuperclass().getName() : "superclass", e );
			}
		}

		// add superclass to depedencies
		java.lang.Class< ? > superClass = clazz.getSuperclass();
		if( superClass != null ) {
			dependencyIdentifiers.add( superClass.getName() );
		}

		// add implemented interfaces to dependencies
		for( java.lang.Class< ? > superInterface : clazz.getInterfaces() ) {
			dependencyIdentifiers.add( superInterface.getName() );
		}

		EnumSet< ClassModifier > classModifiers = parseModifiers( ClassModifier.class,
				clazz.getModifiers() );

		choral.ast.body.Class choralClass = new Class(
				new Name( clazz.getSimpleName(), NOWHERE ),
				List.of( DEFAULT_WORLD_PARAMETER ),
				choralTypeParameters,
				extendedExpression,
				parentInterfaces,
				choralFields,
				methods,
				choralConstructors,
				Collections.emptyList(), // ignore annotations for now
				classModifiers,
				NOWHERE );

		CompilationUnit compilationUnit = new CompilationUnit(
				Optional.of( clazz.getPackageName() ),
				// No imports, because classfiles use fully qualified names
				Collections.emptyList(),
				Collections.emptyList(),
				List.of( choralClass ),
				Collections.emptyList(),
				null );

		compilationUnitAccumulator.add( compilationUnit );

		// recursively visit referenced classfiles
		for( String dependency : dependencyIdentifiers ) {
			if( trackedCompilationUnits.add( dependency ) ) {
				liftPackage( dependency, compilationUnitAccumulator );
			}
		}

		// recursively visit super class
		if( superClass != null ) {
			if( trackedCompilationUnits.add( superClass.getName() ) ) {
				liftPackage( superClass.getName(), compilationUnitAccumulator );
			}
		}

		// recursively visit super interfaces
		for( java.lang.Class< ? > superInterface : clazz.getInterfaces() ) {
			if( trackedCompilationUnits.add( superInterface.getName() ) ) {
				liftPackage( superInterface.getName(), compilationUnitAccumulator );
			}
		}
	}

	private static void liftInterface(
			java.lang.Class< ? > clazz,
			List< CompilationUnit > compilationUnitAccumulator
	) {
		// TRANSLATE METHODS
		Set< String > dependencyIdentifiers = new HashSet<>();
		List< InterfaceMethodDefinition > choralInterfaceMethods = liftMethods(
				clazz.getDeclaredMethods(),
				InterfaceMethodModifier.class, 
				dependencyIdentifiers,
				( signature, modifiers ) -> new InterfaceMethodDefinition(
						signature,
						Collections.emptyList(),
						modifiers,
						NOWHERE )
		);

		// TRANSLATE SUPER INTERFACES
		java.lang.reflect.Type[] genericInterfaces = clazz.getGenericInterfaces();
		List< TypeExpression > choralExtendedInterfaces = new ArrayList<>();
		for( java.lang.reflect.Type genericInterface : genericInterfaces ) {
			try {
				choralExtendedInterfaces.add( liftType( genericInterface ) );
			} catch( LiftException e ) {
				warn( genericInterface.toString(), e );
			}
		}

		// TRANSLATE TYPE PARAMETERS
		List< FormalTypeParameter > choralTypeParameters;
		try {
			choralTypeParameters = liftTypeParameters( clazz.getTypeParameters() );
		} catch( LiftException e ) {
			warn( clazz.getSimpleName(), e );
			return;
		}

		// add super interfaces to dependencies
		for( java.lang.Class< ? > superInterface : clazz.getInterfaces() ) {
			dependencyIdentifiers.add( superInterface.getName() );
		}

		EnumSet< InterfaceModifier > interfaceModifiers = parseModifiers( InterfaceModifier.class,
				clazz.getModifiers() );

		Interface choralInterface = new Interface(
				new Name( clazz.getSimpleName(), NOWHERE ),
				List.of( DEFAULT_WORLD_PARAMETER ),
				choralTypeParameters,
				choralExtendedInterfaces,
				choralInterfaceMethods,
				Collections.emptyList(), // ignore annotations for now
				interfaceModifiers,
				NOWHERE );

		CompilationUnit compilationUnit = new CompilationUnit(
				Optional.of( clazz.getPackageName() ),
				// No imports, because classfiles use fully qualified names
				Collections.emptyList(),
				List.of( choralInterface ),
				Collections.emptyList(),
				Collections.emptyList(),
				null );

		compilationUnitAccumulator.add( compilationUnit );

		// recursively visit referenced classfiles
		for( String dependency : dependencyIdentifiers ) {
			if( trackedCompilationUnits.add( dependency ) ) {
				liftPackage( dependency, compilationUnitAccumulator );
			}
		}

		// recursively visit super interfaces
		for( java.lang.Class< ? > superInterface : clazz.getInterfaces() ) {
			if( trackedCompilationUnits.add( superInterface.getName() ) ) {
				liftPackage( superInterface.getName(), compilationUnitAccumulator );
			}
		}
	}

	private static void liftEnum(
			java.lang.Class<?> enumClass,
			List< CompilationUnit > compilationUnitAccumulator
	) {
		// TRANSLATE CONSTANTS
		java.lang.reflect.Field[] allFields = enumClass.getFields();
		List< EnumConstant > choralEnumConstants = new ArrayList<>();
		for( java.lang.reflect.Field field : allFields ) {
			if( field.isEnumConstant() ) {
				EnumConstant newConstant = new EnumConstant(
						new Name( field.getName(), NOWHERE ),
						Collections.emptyList(), // ignore annotations for now
						NOWHERE );
				choralEnumConstants.add( newConstant );
			}
		}

		EnumSet< ClassModifier > enumModifiers = parseModifiers( ClassModifier.class,
				enumClass.getModifiers() );
		// Enum for enum modifiers in choral internals is the same Enum used for class modifiers
		// but enums are not allowed to be abstract
		enumModifiers.remove( ClassModifier.ABSTRACT );

		choral.ast.body.Enum choralEnum = new choral.ast.body.Enum(
				new Name( enumClass.getSimpleName(), NOWHERE ),
				DEFAULT_WORLD_PARAMETER,
				choralEnumConstants,
				Collections.emptyList(), // ignore annotations for now
				enumModifiers,
				NOWHERE );

		CompilationUnit compilationUnit = new CompilationUnit(
				Optional.of( enumClass.getPackageName() ),
				// No imports, because classfiles use fully qualified names
				Collections.emptyList(),
				Collections.emptyList(),
				Collections.emptyList(),
				List.of( choralEnum ),
				null );

		compilationUnitAccumulator.add( compilationUnit );

	}


	/////////////////////////////////////////////////////////////////////
	///////////////////////// METHOD-LIFTING  ///////////////////////////
	/////////////////////////////////////////////////////////////////////


	/**
	 * Lifts Java methods into their Choral representation.
	 * @see #liftConstructors
	 */
	private static < M extends Enum< M >, D > List< D > liftMethods(
			java.lang.reflect.Method[] methods,
			java.lang.Class< M > modifierClass,
			Set< String > dependencyIdentifiers,
			MethodDefinitionFactory< M, D > factory
	) {
		List< D > methodDefinitions = new ArrayList<>();
		for( java.lang.reflect.Method method : methods ) {
			// private methods will never be accessed
			if( Modifier.isPrivate( method.getModifiers() ) ) continue;
			// Java "bridge" methods cause problems because they give out "erased" types. For
			// example, `IntStream` has a method `java.util.Iterator iterator()` - notice the return
			// type is missing a type parameter. Choral can't cope with this, so we skip it.
			if( method.isBridge() ) continue;

			EnumSet< M > modifiers = parseModifiers( modifierClass, method.getModifiers() );
			if(method.isDefault()) {
				modifiers.add( Enum.valueOf( modifierClass, "DEFAULT" ));
			}

			MethodSignature methodSignature;
			try {
				methodSignature = liftMethodSignature( method );
			} catch( LiftException e ) {
				warn( method.getName(), e );
				continue;
			}
			methodDefinitions.add( factory.create( methodSignature, modifiers ) );

			// by adding method dependencies at this point, arrays and wildcards have already been checked for.
			addMethodDependencies( dependencyIdentifiers, method );
		}
		return methodDefinitions;
	}

	/**
	 * Lifts Java constructors into their Choral representation.
	 * @see #liftMethods
	 */
	private static < M extends Enum< M >, D > List< D > liftConstructors(
			java.lang.reflect.Constructor< ? >[] constructors,
			java.lang.Class< M > modifierClass,
			Set< String > dependencyIdentifiers,
			ConstructorDefinitionFactory< M, D > factory
	) {
		List< D > methodDefinitions = new ArrayList<>();
		for( java.lang.reflect.Constructor< ? > ctor : constructors ) {
			// private methods will never be accessed
			if( Modifier.isPrivate( ctor.getModifiers() ) ) continue;

			EnumSet< M > modifiers = parseModifiers( modifierClass, ctor.getModifiers() );

			ConstructorSignature methodSignature;
			try {
				methodSignature = liftConstructorSignature( ctor );
			} catch( LiftException e ) {
				warn( ctor.getName(), e );
				continue;
			}
			methodDefinitions.add( factory.create( methodSignature, modifiers ) );

			// by adding method dependencies at this point, arrays and wildcards have already been checked for.
			addMethodDependencies( dependencyIdentifiers, ctor );
		}
		return methodDefinitions;
	}

	private static MethodSignature liftMethodSignature(
			java.lang.reflect.Method method
	) throws LiftException {
		return new MethodSignature(
				new Name( method.getName(), NOWHERE ),
				liftTypeParameters( method.getTypeParameters() ),
				liftMethodParameters( method.getGenericParameterTypes() ),
				liftType( method.getGenericReturnType() ),
				NOWHERE );
	}

	private static ConstructorSignature liftConstructorSignature(
			java.lang.reflect.Constructor< ? > constructor
	) throws LiftException {
		return new ConstructorSignature(
				new Name( constructor.getClass().getSimpleName(), NOWHERE ),
				liftTypeParameters( constructor.getTypeParameters() ),
				liftMethodParameters( constructor.getGenericParameterTypes() ),
				NOWHERE );
	}

	/**
	 * Translates method parameters from Java reflection to Choral's internal representation.
	 */
	private static List< FormalMethodParameter > liftMethodParameters(
			java.lang.reflect.Type[] parameterTypes
	) throws LiftException {
		List< FormalMethodParameter > parameters = new ArrayList<>();

		for( int i = 0; i < parameterTypes.length; i++ ) {
			TypeExpression type = liftType( parameterTypes[i] );
			parameters.add( new FormalMethodParameter(
					new Name( "param" + i, NOWHERE ),
					type,
					Collections.emptyList(), // ignore annotations for now
					NOWHERE ) );
		}
		return parameters;
	}

	@FunctionalInterface
	interface MethodDefinitionFactory< M extends Enum< M >, D > {
		D create( MethodSignature signature, EnumSet< M > modifiers );
	}

	@FunctionalInterface
	interface ConstructorDefinitionFactory< M extends Enum< M >, D > {
		D create( ConstructorSignature signature, EnumSet< M > modifiers );
	}

	/////////////////////////////////////////////////////////////////////
	//////////////////// HELPERS FOR LIFTING TYPES  /////////////////////
	/////////////////////////////////////////////////////////////////////


	private static List< FormalTypeParameter > liftTypeParameters(
			java.lang.reflect.TypeVariable< ? >[] typeParameters
	) throws LiftException {

		List< FormalTypeParameter > choralTypeParameters = new ArrayList<>();
		for( java.lang.reflect.TypeVariable< ? > typeParameter : typeParameters ) {
			// choral does not support lower bounds, so only upper bounds are found
			List< TypeExpression > upperBounds = new ArrayList<>();

			java.lang.reflect.Type[] bounds = typeParameter.getBounds();
			for( java.lang.reflect.Type bound : bounds ) {
				// Skip Object as a bound - it's the default and not meaningful
				if( bound.equals( Object.class ) ) {
					continue;
				}
				upperBounds.add( liftType( bound ) );
			}

			FormalTypeParameter choralTypeParameter = new FormalTypeParameter(
					new Name( typeParameter.getName(), NOWHERE ),
					List.of( DEFAULT_WORLD_PARAMETER ),
					upperBounds,
					Collections.emptyList(), // ignore annotations for now
					NOWHERE );
			choralTypeParameters.add( choralTypeParameter );
		}
		return choralTypeParameters;
	}


	/**
	 * Generates the choral TypeExpression from the given Java reflection Type.
	 * Does so recursively if given Type is nested (or has type arguments).
	 */
	private static TypeExpression liftType( java.lang.reflect.Type type )
			throws LiftException {
		return liftType( type, List.of( DEFAULT_WORLD_ARGUMENT ) );
	}

	private static TypeExpression liftType(
			java.lang.reflect.Type type,
			List< WorldArgument > worlds
	) throws LiftException {

		// Handle Class types (includes primitive types and regular classes)
		// We check the class name to avoid conflict with choral.ast.body.Class import
		if( type.getClass().getName().equals( "java.lang.Class" ) ) {
			java.lang.Class<?> clazz = (java.lang.Class<?>) type;

			// Handle array types
			if( clazz.isArray() ) {
				throw LiftException.array();
			}

			// Handle inner classes
			if( clazz.isMemberClass() ) {
				throw LiftException.innerClass();
			}

			// Handle primitive types and regular classes
			String typeName;
			if( clazz.isPrimitive() ) {
				typeName = clazz.getName(); // "int", "void", etc.
				// void should not have world arguments
				if( typeName.equals( "void" ) ) {
					worlds = Collections.emptyList();
				}
			} else {
				typeName = clazz.getCanonicalName(); // fully qualified name
			}

			return new TypeExpression(
					new Name( typeName, NOWHERE ),
					worlds,
					Collections.emptyList(),
					NOWHERE );
		}
		// Handle ParameterizedType (generic types like List<String>)
		else if( type instanceof java.lang.reflect.ParameterizedType ) {
			java.lang.reflect.ParameterizedType paramType = (java.lang.reflect.ParameterizedType) type;
			java.lang.reflect.Type rawType = paramType.getRawType();

			// rawType should be a Class
			java.lang.Class<?> rawClass = (java.lang.Class<?>) rawType;

			// Check if the raw class is an inner class
			if( rawClass.isMemberClass() ) {
				throw LiftException.innerClass();
			}

			List< TypeExpression > typeExpressions = new ArrayList<>();
			java.lang.reflect.Type[] typeArgs = paramType.getActualTypeArguments();

			for( java.lang.reflect.Type typeArg : typeArgs ) {
				// Check for wildcards
				if( typeArg instanceof java.lang.reflect.WildcardType ) {
					throw LiftException.wildcard();
				}
				// Recursively process type arguments without world arguments
				TypeExpression typeExpression = liftType( typeArg, Collections.emptyList() );
				typeExpressions.add( typeExpression );
			}

			return new TypeExpression(
					new Name( rawClass.getCanonicalName(), NOWHERE ),
					worlds,
					typeExpressions,
					NOWHERE );
		}
		// Handle TypeVariable (type parameters like T, E, K, V)
		else if( type instanceof java.lang.reflect.TypeVariable< ? > typeVar ) {
			return new TypeExpression(
					new Name( typeVar.getName(), NOWHERE ),
					worlds,
					Collections.emptyList(),
					NOWHERE );
		}
		// Handle GenericArrayType (generic array types like T[])
		else if( type instanceof java.lang.reflect.GenericArrayType ) {
			throw LiftException.array();
		}
		// Handle WildcardType (wildcard types like ? extends T, ? super T)
		else if( type instanceof java.lang.reflect.WildcardType ) {
			throw LiftException.wildcard();
		}
		else {
			throw new UnsupportedOperationException( "This type is not yet supported: "
					+ type + ". Type class: " + type.getClass().getName() );
		}
	}


	/////////////////////////////////////////////////////////////////////
	////////////////////// DEPENDENCY MANAGEMENT  ///////////////////////
	/////////////////////////////////////////////////////////////////////


	/**
	 * Add dependencies for a method to dependencyIdentifiers. This currently includes return type, type parameters,
	 * and method parameters. Exceptions are not included since those are not part of the MethodSignature in Choral.
	 */
	private static void addMethodDependencies(
			Set< String > dependencyIdentifiers,
			java.lang.reflect.Executable method
	) {
		// Extract return type dependencies
		if (method instanceof java.lang.reflect.Method m)
			extractClassDependencies( dependencyIdentifiers, m.getGenericReturnType() );

		// Extract type parameter bounds dependencies
		for( java.lang.reflect.TypeVariable< ? > typeParameter : method.getTypeParameters() ) {
			for( java.lang.reflect.Type bound : typeParameter.getBounds() ) {
				extractClassDependencies( dependencyIdentifiers, bound );
			}
		}

		// Extract parameter type dependencies
		for( java.lang.reflect.Type paramType : method.getGenericParameterTypes() ) {
			extractClassDependencies( dependencyIdentifiers, paramType );
		}
	}

	/**
	 * Extracts all class dependencies from a Java reflection Type and adds them to the dependency set.
	 * This recursively extracts class names, handling generic types by also extracting
	 * dependencies from type arguments. Primitive types and inner classes are skipped.
	 *
	 * For example, "java.util.Map<java.lang.String, java.util.List<java.lang.Integer>>"
	 * would extract: java.util.Map, java.lang.String, java.util.List, java.lang.Integer
	 */
	private static void extractClassDependencies(
			Set< String > dependencyIdentifiers,
			java.lang.reflect.Type type
	) {
		// Handle Class types (includes primitive types and regular classes)
		if( type.getClass().getName().equals( "java.lang.Class" ) ) {
			java.lang.Class<?> clazz = (java.lang.Class<?>) type;

			// Skip primitive types, arrays, and inner classes
			if( clazz.isPrimitive() || clazz.isArray() || clazz.isMemberClass() ) {
				return;
			}

			// getName() returns the binary name which is what we need for consistency
			dependencyIdentifiers.add( clazz.getName() );
		}
		// Handle ParameterizedType (generic types like List<String>)
		else if( type instanceof java.lang.reflect.ParameterizedType ) {
			java.lang.reflect.ParameterizedType paramType = (java.lang.reflect.ParameterizedType) type;
			java.lang.reflect.Type rawType = paramType.getRawType();

			// Add the raw type
			if( rawType instanceof java.lang.Class<?> rawClass ) {
				// Skip inner classes
				if( !rawClass.isMemberClass() ) {
					dependencyIdentifiers.add( rawClass.getName() );
				}
			}

			// Recursively extract dependencies from type arguments
			for( java.lang.reflect.Type typeArg : paramType.getActualTypeArguments() ) {
				// Skip wildcards
				if( !(typeArg instanceof java.lang.reflect.WildcardType) ) {
					extractClassDependencies( dependencyIdentifiers, typeArg );
				}
			}
		}
		// Handle GenericArrayType (generic array types like T[])
		else if( type instanceof java.lang.reflect.GenericArrayType ) {
			// We don't support arrays
			return;
		}
		// Handle TypeVariable (type parameters like T, E)
		else if( type instanceof java.lang.reflect.TypeVariable< ? > ) {
			// Type variables are not dependencies - they're defined elsewhere
			return;
		}
		// Handle WildcardType (wildcard types like ? extends T)
		else if( type instanceof java.lang.reflect.WildcardType ) {
			// Wildcards are not supported
			return;
		}
	}

	/**
	 * Parses modifiers found by the Java reflection API into modifiers used by choral internals.
	 */
	private static < E extends Enum< E > > EnumSet< E > parseModifiers(
			java.lang.Class< E > enumClass,
			int modifierBits
	) {
		EnumSet< E > modifiers = EnumSet.noneOf( enumClass );

		for( String modifier : Modifier.toString( modifierBits ).split(" ") ) {
			try {
				modifiers.add( Enum.valueOf( enumClass, modifier.toUpperCase() ) );
			} catch( IllegalArgumentException e ) {
				continue;
			}
		}
		return modifiers;
	}


	private static void warn( String id, LiftException e ) {
		// System.out.println( "WARNING: Failed to lift " + id + " because " + e.getMessage() +
        //         " types are not supported" );
	}

	/**
	 * Clears the set of tracked compilation units that have already been lifted.
	 * This method should only be called for testing purposes.
	 */
	public static void clearTrackedCompilationUnits() {
		trackedCompilationUnits.clear();
	}
}
