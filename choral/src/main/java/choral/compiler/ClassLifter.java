package choral.compiler;

import choral.ast.CompilationUnit;
import choral.ast.Name;
import choral.ast.Position;
import choral.ast.body.*;
import choral.ast.body.Class;
import choral.ast.expression.LiteralExpression;
import choral.ast.expression.LiteralExpression.BooleanLiteralExpression;
import choral.ast.expression.LiteralExpression.DoubleLiteralExpression;
import choral.ast.expression.LiteralExpression.IntegerLiteralExpression;
import choral.ast.expression.LiteralExpression.StringLiteralExpression;
import choral.ast.statement.NilStatement;
import choral.ast.type.FormalTypeParameter;
import choral.ast.type.FormalWorldParameter;
import choral.ast.type.TypeExpression;
import choral.ast.type.WorldArgument;
import io.github.classgraph.*;
import io.github.classgraph.TypeArgument.Wildcard;

import java.lang.Enum;
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


	///////////////////// MAIN LIFTING METHODS /////////////////////


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
		System.out.println( "Lifting class: " + fullyQualifiedName );
		int lastSeparator = fullyQualifiedName.lastIndexOf( "." );
		String packageName = fullyQualifiedName.substring( 0, lastSeparator );

		try( ScanResult scanResult = new ClassGraph()
				//.verbose()
				.enableAllInfo()
				.enableInterClassDependencies()
				.enableExternalClasses()
				.enableSystemJarsAndModules()
				.acceptPackages( packageName )
				.scan() ) {
			ClassInfo classInfo = scanResult.getClassInfo( fullyQualifiedName );

			if( classInfo == null ) {
				System.err.println( "WARNING: Could not find class: " + fullyQualifiedName );
				System.err.println( "Package scanned: " + packageName );
				System.err.println(
						"All classes found in scan: " + scanResult.getAllClasses().size() );
				throw new RuntimeException( "Could not find class: " + fullyQualifiedName );
			}

			if( classInfo.isInnerClass() ) {
				System.err.println( "WARNING: Class lifter couldn't import inner class: " +
                        fullyQualifiedName );
				return;
			}

			trackedCompilationUnits.add( classInfo.getName() );

			// Load the class using reflection
			try {
				java.lang.Class<?> clazz = java.lang.Class.forName( fullyQualifiedName );
				if( classInfo.isEnum() ) {
					liftEnum( clazz, compilationUnitAccumulator );
				} else if( classInfo.isInterface() ) {
					liftInterface( classInfo, clazz, compilationUnitAccumulator );
				} else {
					liftClass( classInfo, clazz, compilationUnitAccumulator );
				}
			} catch( ClassNotFoundException e ) {
				System.err.println( "WARNING: Could not find class: " + fullyQualifiedName );
				throw new RuntimeException( "Could not find class: " + fullyQualifiedName, e );
			}
		}
	}

	private static void liftClass(
			ClassInfo classInfo,
			java.lang.Class< ? > clazz,
			List< CompilationUnit > compilationUnitAccumulator
	) {
		// for keeping track of which dependencies to lift
		Set< String > dependencyIdentifiers = new HashSet<>();

		// TRANSLATE FIELDS
		FieldInfoList fieldInfoList = classInfo.getFieldInfo();
		List< Field > choralFields = new ArrayList<>();
		for( FieldInfo fieldInfo : fieldInfoList ) {
			// private fields will never be accessed
			if( fieldInfo.isPrivate() ) continue;

			EnumSet< FieldModifier > modifiers = parseModifiers( FieldModifier.class,
					fieldInfo.getModifiers() );

			TypeSignature fieldTypeSig = fieldInfo.getTypeSignatureOrTypeDescriptor();
			TypeExpression fieldTypeExpression;
			try {
				fieldTypeExpression = getTypeExpressions( fieldTypeSig );
			} catch( LiftException e ) {
				warn( fieldInfo.getName(), e );
				continue;
			}

			// add fields type to depencies
			extractClassDependencies( dependencyIdentifiers, fieldTypeSig );

			Field field = new Field(
					new Name( fieldInfo.getName() ),
					fieldTypeExpression,
					Collections.emptyList(), // ignore annotations for now
					modifiers,
					NOWHERE );
			choralFields.add( field );
		}

		// TRANSLATE METHODS
		List< ClassMethodDefinition > methods = liftMethods(
				classInfo.getMethodInfo(),
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
		MethodInfoList constructors = classInfo.getConstructorInfo();
		List< ConstructorDefinition > choralConstructors = new ArrayList<>();
		for( MethodInfo constructor : constructors ) {
			EnumSet< ConstructorModifier > modifiersConstructor = parseModifiers(
					ConstructorModifier.class, constructor.getModifiers() );

			LiftedSignatureData liftedSignatureData;
			try {
				liftedSignatureData = liftSignatureData( constructor );
			} catch( LiftException e ) {
				warn( constructor.getName(), e );
				continue;
			}

			ConstructorSignature constructorSignature = new ConstructorSignature(
					new Name( classInfo.getSimpleName(), NOWHERE ),
					liftedSignatureData.typeParameters(),
					liftedSignatureData.parameters(),
					NOWHERE );

			ConstructorDefinition constructorChoral = new ConstructorDefinition(
					constructorSignature,
					null, // not supported by ClassGraph
					// ^Represents calling `this()` or `super()` at the start of a constructor
					new NilStatement( NOWHERE ), // ignore constructor body
					Collections.emptyList(), // ignore annotations for now
					modifiersConstructor,
					NOWHERE );

			choralConstructors.add( constructorChoral );
		}

		// TRANSLATE TYPE PARAMETERS
		ClassTypeSignature classTypeSignature = classInfo.getTypeSignatureOrTypeDescriptor();
		List< FormalTypeParameter > choralTypeParameters;
		try {
			choralTypeParameters = liftTypeParameters( classTypeSignature.getTypeParameters() );
		} catch( LiftException e ) {
			warn( classTypeSignature.toString(), e );
			return;
		}

		// TRANSLATE SUPERINTERFACES
		ClassRefTypeSignature extendedClassTypeSignature = classTypeSignature.getSuperclassSignature();
		List< TypeExpression > parentInterfaces = liftSuperInterfaces(
				classTypeSignature.getSuperinterfaceSignatures() );

		// TRANSLATE SUPERCLASS
		TypeExpression extendedExpression = null;
		if( extendedClassTypeSignature != null ) {
			try {
				extendedExpression = getTypeExpressions( extendedClassTypeSignature );
			} catch( LiftException e ) {
				warn( extendedClassTypeSignature.getBaseClassName(), e );
			}
		}

		// add superclass to depedencies
		ClassInfo superClass = classInfo.getSuperclass();
		if( superClass != null ) {
			dependencyIdentifiers.add( superClass.getName() );
		}

		// add implemented interfaces to dependencies
		for( ClassInfo superInterface : classInfo.getInterfaces() ) {
			dependencyIdentifiers.add( superInterface.getName() );
		}

		EnumSet< ClassModifier > classModifiers = parseModifiers( ClassModifier.class,
				classInfo.getModifiers() );

		choral.ast.body.Class choralClass = new Class(
				new Name( classInfo.getSimpleName(), NOWHERE ),
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
				Optional.of( classInfo.getPackageName() ),
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
		if( extendedClassTypeSignature != null ) {
			if( trackedCompilationUnits.add( extendedClassTypeSignature.getBaseClassName() ) ) {
				// getBaseClassName returns fully qualified name of class, similarly to getName
				liftPackage( extendedClassTypeSignature.getBaseClassName(),
						compilationUnitAccumulator );
			}
		}

		// recursively visit super interfaces
		for( ClassRefTypeSignature interfaceSignature : classTypeSignature.getSuperinterfaceSignatures() ) {
			if( trackedCompilationUnits.add( interfaceSignature.getBaseClassName() ) ) {
				liftPackage( interfaceSignature.getBaseClassName(),
						compilationUnitAccumulator );
			}
		}
	}

	private static void liftInterface(
			ClassInfo interfaceInfo,
			java.lang.Class< ? > clazz,
			List< CompilationUnit > compilationUnitAccumulator
	) {
		// TRANSLATE METHODS
		Set< String > dependencyIdentifiers = new HashSet<>();
		List< InterfaceMethodDefinition > choralInterfaceMethods = liftMethods(
				interfaceInfo.getMethodInfo(),
				InterfaceMethodModifier.class,
				dependencyIdentifiers,
				( signature, modifiers ) -> new InterfaceMethodDefinition(
						signature,
						Collections.emptyList(),
						modifiers,
						NOWHERE )
		);

		// find super interfaces
		ClassTypeSignature interfaceTypeSignature = interfaceInfo.getTypeSignatureOrTypeDescriptor();
		List< TypeExpression > choralExtendedInterfaces = liftSuperInterfaces(
				interfaceTypeSignature.getSuperinterfaceSignatures() );

		// TRANSLATE TYPE PARAMETERS
		List< FormalTypeParameter > choralTypeParameters;
		try {
			choralTypeParameters = liftTypeParameters( interfaceTypeSignature.getTypeParameters() );
		} catch( LiftException e ) {
			warn( interfaceTypeSignature.toString(), e );
			return;
		}

		// add super interfaces to dependencies
		for( ClassInfo superInterface : interfaceInfo.getInterfaces() ) {
			dependencyIdentifiers.add( superInterface.getName() );
		}

		EnumSet< InterfaceModifier > interfaceModifiers = parseModifiers( InterfaceModifier.class,
				interfaceInfo.getModifiers() );

		Interface choralInterface = new Interface(
				new Name( interfaceInfo.getSimpleName(), NOWHERE ),
				List.of( DEFAULT_WORLD_PARAMETER ),
				choralTypeParameters,
				choralExtendedInterfaces,
				choralInterfaceMethods,
				Collections.emptyList(), // ignore annotations for now
				interfaceModifiers,
				NOWHERE );

		CompilationUnit compilationUnit = new CompilationUnit(
				Optional.of( interfaceInfo.getPackageName() ),
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
		for( ClassRefTypeSignature interfaceSig : interfaceTypeSignature.getSuperinterfaceSignatures() ) {
			if( trackedCompilationUnits.add( interfaceSig.getBaseClassName() ) ) {
				liftPackage( interfaceSig.getBaseClassName(), compilationUnitAccumulator );
			}
		}
	}

	private static List< TypeExpression > liftSuperInterfaces(
			List< ClassRefTypeSignature > interfaceSignatures
	) {
		List< TypeExpression > translatedSuperInterfaces = new ArrayList<>();
		for( ClassRefTypeSignature implementedTypeSignature : interfaceSignatures ) {
			TypeExpression interfaceExpression;
			try {
				interfaceExpression = getTypeExpressions( implementedTypeSignature );
			} catch( LiftException e ) {
				warn( implementedTypeSignature.getBaseClassName(), e );
				continue;
			}
			translatedSuperInterfaces.add( interfaceExpression );
		}

		return translatedSuperInterfaces;
	}

	// Reflection-based version of liftEnum - package-private for testing
	static void liftEnum(
			java.lang.Class<?> enumClass, List< CompilationUnit > compilationUnitAccumulator ) {
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

	private static < M extends Enum< M >, D > List< D > liftMethods(
			MethodInfoList methodInfoList,
			java.lang.Class< M > modifierClass,
			Set< String > dependencyIdentifiers,
			MethodDefinitionFactory< M, D > factory
	) {
		List< D > methodDefinitions = new ArrayList<>();
		for( MethodInfo methodInfo : methodInfoList ) {
			// private methods will never be accessed
			if( methodInfo.isPrivate() ) continue;

			EnumSet< M > modifiers = parseModifiers( modifierClass, methodInfo.getModifiers() );

			MethodSignature methodSignature;
			try {
				methodSignature = getMethodSignature( methodInfo );
			} catch( LiftException e ) {
				warn( methodInfo.getName(), e );
				continue;
			}
			methodDefinitions.add( factory.create( methodSignature, modifiers ) );

			// by adding method dependencies at this point, arrays and wildcards have already been checked for.
			addMethodDependencies( dependencyIdentifiers, methodInfo );
		}
		return methodDefinitions;
	}

	/**
	 * Add dependencies for a method to dependencyIdentifiers. This currently includes return type, type parameters,
	 * and method parameters. Exceptions are not included since those are not part of the MethodSignature in Choral.
	 */
	private static void addMethodDependencies(
			Set< String > dependencyIdentifiers, MethodInfo methodInfo ) {
		MethodTypeSignature typeSignature = methodInfo.getTypeSignatureOrTypeDescriptor();
		TypeSignature returnType = typeSignature.getResultType();
		extractClassDependencies( dependencyIdentifiers, returnType );

		for( TypeParameter typeParameter : typeSignature.getTypeParameters() ) {
			ReferenceTypeSignature classBound = typeParameter.getClassBound();
			if( classBound != null ) {
				extractClassDependencies( dependencyIdentifiers, classBound );
			}

			for( ReferenceTypeSignature referenceTypeSignature : typeParameter.getInterfaceBounds() ) {
				extractClassDependencies( dependencyIdentifiers, referenceTypeSignature );
			}
		}

		for( MethodParameterInfo param : methodInfo.getParameterInfo() ) {
			extractClassDependencies( dependencyIdentifiers, param.getTypeSignatureOrTypeDescriptor() );
		}
	}

	/**
	 * Extracts all class dependencies from a type signature and adds them to the dependency set.
	 * This recursively extracts base class names, handling generic types by also extracting
	 * dependencies from type arguments. Primitive types are skipped.
	 *
	 * For example, "java.util.Map<java.lang.String, java.util.List<java.lang.Integer>>"
	 * would extract: java.util.Map, java.lang.String, java.util.List, java.lang.Integer
	 */
	private static void extractClassDependencies(
			Set< String > dependencyIdentifiers, TypeSignature typeSignature ) {
		if( typeSignature instanceof BaseTypeSignature ) {
			// Skip primitive types - they don't need to be lifted as dependencies
			return;
		} else if( typeSignature instanceof ClassRefTypeSignature classRef ) {
			// Add the base class name (without generic parameters)
			dependencyIdentifiers.add( classRef.getBaseClassName() );

			// Recursively extract dependencies from type arguments
			List< TypeArgument > typeArguments = classRef.getTypeArguments();
			if( typeArguments != null ) {
				for( TypeArgument typeArg : typeArguments ) {
					TypeSignature argType = typeArg.getTypeSignature();
					if( argType != null ) {
						extractClassDependencies( dependencyIdentifiers, argType );
					}
				}
			}
		} else if( typeSignature instanceof ArrayTypeSignature arrayType ) {
			// We don't support arrays
			return;
		} else if( typeSignature instanceof TypeVariableSignature ) {
			// Type variables (e.g., T, E) are not dependencies - they're defined elsewhere
			return;
		}
		// Note: ReferenceTypeSignature is the parent of ClassRefTypeSignature and others,
		// so if we get here with an unhandled ReferenceTypeSignature subtype, we skip it
	}

	/**
	 * Parses modifiers found by ClassGraph, into modifiers used by choral internals.
	 */
	private static < E extends Enum< E > > EnumSet< E > parseModifiers(
			java.lang.Class< E > enumClass, int modifierBits
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

	private static List< FormalTypeParameter > liftTypeParameters(
			List< TypeParameter > typeParameters
	)
			throws LiftException {
		List< FormalTypeParameter > choralTypeParameters = new ArrayList<>();
		for( TypeParameter typeParameter : typeParameters ) {
			// choral does not support lower bounds, so only upper bounds are found
			List< TypeExpression > upperBounds = new ArrayList<>();

			ReferenceTypeSignature classBound = typeParameter.getClassBound();
			if( containsWildcards( classBound ) ) {
				throw LiftException.wildcard();
			}

			TypeExpression classBoundExpression;
			if( classBound != null ) {
				classBoundExpression = getTypeExpressions( classBound );
				upperBounds.add( classBoundExpression );
			}

			List< ReferenceTypeSignature > interfaceBounds = typeParameter.getInterfaceBounds();
			for( ReferenceTypeSignature interfaceBound : interfaceBounds ) {
				if( containsWildcards( interfaceBound ) ) {
					throw LiftException.wildcard();
				}
				upperBounds.add( getTypeExpressions( interfaceBound ) );
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

	// recursively checks whether a type parameter contains a wild card.
	// recursion is done to account for nested types
	private static boolean containsWildcards( TypeSignature typeSignature ) {
		if( typeSignature instanceof ClassRefTypeSignature classRef ) {
			List< TypeArgument > typeArguments = classRef.getTypeArguments();
			for( TypeArgument typeArgument : typeArguments ) {
				if( typeArgument.getWildcard() != Wildcard.NONE ) {
					return true;
				}
				TypeSignature argumentTypeSignature = typeArgument.getTypeSignature();
				if( containsWildcards( argumentTypeSignature ) ) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Generates the choral TypeExpression from the given ClassGraph TypeSignature.
	 * Does so recursively if given TypeSignature is nested.
	 *
	 * @param typeSig
	 * @return
	 */
	private static TypeExpression getTypeExpressions( TypeSignature typeSig )
			throws LiftException {
		List< TypeExpression > typeExpressions = new ArrayList<>();
		if( typeSig instanceof ClassRefTypeSignature classref ) { // for nested types
			String baseClassName = classref.getBaseClassName();
			String simpleName = baseClassName.substring( baseClassName.lastIndexOf( '.' ) + 1 );

			List< TypeArgument > typeArguments = classref.getTypeArguments();
			if( typeArguments != null ) {
				for( int i = 0; i < typeArguments.size(); i++ ) {
					TypeArgument arg = typeArguments.get( i );
					TypeSignature argType = arg.getTypeSignature();
					if( argType == null ) {
						throw LiftException.wildcard();
					}
					TypeExpression typeExpression = getTypeExpressionsHelper( argType );
					typeExpressions.add( typeExpression );
				}
			}
			return new TypeExpression(
					new Name( baseClassName, NOWHERE ),
					List.of( DEFAULT_WORLD_ARGUMENT ),
					typeExpressions,
					NOWHERE );
		} else if( typeSig instanceof BaseTypeSignature baseRef ) { // for primitive types
			return new TypeExpression(
					new Name( baseRef.getTypeStr(), NOWHERE ),
					// void should not have world arguments
					baseRef.getTypeStr().equals( "void" ) ? Collections.emptyList() : List.of(
							DEFAULT_WORLD_ARGUMENT ),
					Collections.emptyList(),
					NOWHERE );
		} else if( typeSig instanceof TypeVariableSignature typeVar ) { // for type parameters
			return new TypeExpression(
					new Name( typeVar.getName(), NOWHERE ),
					List.of( DEFAULT_WORLD_ARGUMENT ),
					Collections.emptyList(),
					NOWHERE );
		} else if( typeSig instanceof ArrayTypeSignature arrTypeVar ) { // for array types
			throw LiftException.array();
		} else {
			throw new UnsupportedOperationException( "This type of signature is not yet supported: "
					+ typeSig + ". Type of signature: " + typeSig.getClass().getName() );
		}
	}

	// This helper method exists because inner types of a nested type should not have any world arguments
	private static TypeExpression getTypeExpressionsHelper( TypeSignature typeSig )
			throws LiftException {
		List< TypeExpression > typeExpressions = new ArrayList<>();
		if( typeSig instanceof ClassRefTypeSignature classref ) { // for nested types
			String baseClassName = classref.getBaseClassName();
			List< TypeArgument > typeArguments = classref.getTypeArguments();
			if( typeArguments != null && !typeArguments.isEmpty() ) {
				for( int i = 0; i < typeArguments.size(); i++ ) {
					TypeArgument arg = typeArguments.get( i );
					TypeSignature argType = arg.getTypeSignature();
					if( argType == null ) throw LiftException.wildcard();
					TypeExpression typeExpression = getTypeExpressionsHelper( argType );
					typeExpressions.add( typeExpression );
				}
			}
			return new TypeExpression(
					new Name( baseClassName, NOWHERE ),
					Collections.emptyList(),
					typeExpressions,
					NOWHERE );
		} else if( typeSig instanceof BaseTypeSignature baseRef ) { // for primitive types
			return new TypeExpression(
					new Name( baseRef.getTypeStr(), NOWHERE ),
					Collections.emptyList(),
					typeExpressions,
					NOWHERE );
		} else if( typeSig instanceof TypeVariableSignature typeVar ) { // for type parameters
			return new TypeExpression(
					new Name( typeVar.getName(), NOWHERE ),
					Collections.emptyList(),
					Collections.emptyList(),
					NOWHERE );
		} else if( typeSig instanceof ArrayTypeSignature ) { // for array types
			throw LiftException.array();
		} else { // implement other typesignatures? (might not be necessary)
			throw new UnsupportedOperationException( "This type of signature is not yet supported: "
					+ typeSig + ". Type of signature: " + typeSig.getClass().getName() );
		}
	}

	private static MethodSignature getMethodSignature(
			MethodInfo methodInfo
	) throws LiftException {
		MethodTypeSignature methodTypeSignature = methodInfo.getTypeSignatureOrTypeDescriptor();
		TypeExpression returnType = getTypeExpressions( methodTypeSignature.getResultType() );

		LiftedSignatureData liftedSignatureData = liftSignatureData( methodInfo );

		return new MethodSignature(
				new Name( methodInfo.getName(), NOWHERE ),
				liftedSignatureData.typeParameters(),
				liftedSignatureData.parameters(),
				returnType,
				NOWHERE );
	}

	/**
	 * Translates method parameters from ClassGraph to Choral's internal representation.
	 *
	 * @param methodParams
	 * @return
	 */
	private static List< FormalMethodParameter > getMethodParameters(
			MethodParameterInfo[] methodParams
	)
			throws LiftException {
		List< FormalMethodParameter > parameters = new ArrayList<>();
		int paramCount = 0;
		for( MethodParameterInfo param : methodParams ) {
			TypeExpression type = getTypeExpressions( param.getTypeSignatureOrTypeDescriptor() );
			parameters.add( new FormalMethodParameter(
					new Name( "param" + paramCount++, NOWHERE ),
					type,
					Collections.emptyList(), // ignore annotations for now
					NOWHERE ) );
		}
		return parameters;
	}

	// A record to hold data shared by MethodSignature and ConstructorSignature
	private record LiftedSignatureData(
			List< FormalTypeParameter > typeParameters,
			List< FormalMethodParameter > parameters
	) {
	}

	private static LiftedSignatureData liftSignatureData(
			MethodInfo methodInfo
	) throws LiftException {
		MethodTypeSignature methodTypeSignature = methodInfo.getTypeSignatureOrTypeDescriptor();
		return new LiftedSignatureData(
				liftTypeParameters( methodTypeSignature.getTypeParameters() ),
				getMethodParameters( methodInfo.getParameterInfo() )
		);
	}

	@FunctionalInterface
	interface MethodDefinitionFactory< M extends Enum< M >, D > {
		D create( MethodSignature signature, EnumSet< M > modifiers );
	}

	private static void warn( String id, LiftException e ) {
		System.out.println( "WARNING: Failed to lift " + id + " because " + e.getMessage() +
                " types are not supported" );
	}

	/**
	 * Clears the set of tracked compilation units that have already been lifted.
	 * This method should only be called for testing purposes.
	 */
	public static void clearTrackedCompilationUnits() {
		trackedCompilationUnits.clear();
	}

	@Deprecated // comment or annotation
	private static List< Annotation > translateAnnotations(
			AnnotationInfoList annotationInfoList
	) {
		List< Annotation > annotations = new ArrayList<>();

		for( AnnotationInfo annotationInfo : annotationInfoList ) {
			String annotationName = annotationInfo.getName();
			AnnotationParameterValueList annotationParameterValueList = annotationInfo.getParameterValues();
			Map< Name, LiteralExpression > annotationValues = new HashMap<>();

			for( AnnotationParameterValue annotationParameterValue : annotationParameterValueList ) {
				Object annotationValue = annotationParameterValue.getValue();
				if( annotationValue instanceof String s ) {
					annotationValues.put( new Name( annotationParameterValue.getName(), NOWHERE ),
							new StringLiteralExpression( s, DEFAULT_WORLD_ARGUMENT ) );
				} else if( annotationValue instanceof Integer i ) {
					annotationValues.put( new Name( annotationParameterValue.getName(), NOWHERE ),
							new IntegerLiteralExpression( i, DEFAULT_WORLD_ARGUMENT ) );
				} else if( annotationValue instanceof Boolean b ) {
					annotationValues.put( new Name( annotationParameterValue.getName(), NOWHERE ),
							new BooleanLiteralExpression( b, DEFAULT_WORLD_ARGUMENT ) );
				} else if( annotationValue instanceof Double d ) {
					annotationValues.put( new Name( annotationParameterValue.getName(), NOWHERE ),
							new DoubleLiteralExpression( d, DEFAULT_WORLD_ARGUMENT ) );
				} else {
					throw new RuntimeException(
							"ClassGraph found value in annotation parameter list not compatible with choral: "
									+ annotationValue.toString() + annotationValue.getClass().descriptorString() );
				}
			}
			Annotation annotation = new Annotation( new Name( annotationName ), annotationValues );
			annotations.add( annotation );
		}
		return annotations;
	}
}
