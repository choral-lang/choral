package choral.compiler.typer;

import choral.ast.Name;
import choral.ast.Position;
import choral.ast.body.*;
import choral.ast.body.Class;
import choral.ast.statement.NilStatement;
import choral.ast.type.FormalTypeParameter;
import choral.ast.type.FormalWorldParameter;
import choral.ast.type.TypeExpression;
import choral.ast.type.WorldArgument;
import choral.types.HigherClassOrInterface;
import choral.types.HigherEnum;
import choral.types.HigherInterface;
import choral.types.HigherTypeParameter;
import choral.types.Member;
import choral.types.Package;
import choral.types.World;

import java.lang.Enum;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import choral.types.GroundClass;
import choral.types.GroundClassOrInterface;
import choral.types.GroundDataType;
import choral.types.GroundDataTypeOrVoid;
import choral.types.GroundInterface;
import choral.types.GroundReferenceType;
import choral.types.HigherClass;
import choral.types.HigherPrimitiveDataType;
import choral.types.HigherReferenceType;
import choral.types.Universe;
import choral.types.Universe.PrimitiveTypeTag;

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

	private static final String WORLD_IDENTIFIER = "A";

	private static final FormalWorldParameter DEFAULT_WORLD_PARAMETER =
			new FormalWorldParameter( new Name( WORLD_IDENTIFIER, NOWHERE ), NOWHERE );

	private static final WorldArgument DEFAULT_WORLD_ARGUMENT =
			new WorldArgument( new Name( WORLD_IDENTIFIER, NOWHERE ), NOWHERE );


	///////////////////// LOCAL STATE /////////////////////

	private final Universe universe;
	private final TaskQueue taskQueue;

	public ClassLifter( Universe universe ) {
		this.universe = universe;
		this.taskQueue = new TaskQueue();
	}


	/////////////////////////////////////////////////////////////////////
	////////////////////// CLASS LIFTING METHODS  ///////////////////////
	/////////////////////////////////////////////////////////////////////

	/**
	 * Looks up the given type on the classpath and lifts it into a Choral type - adding it
	 * to the Universe as a side-effect.
	 *
	 * @param fullyQualifiedName The fully qualified name of type to be lifted.
	 * @return A choral type representing the type, or nothing if classpath lookup failed.
	 */
	public Optional< HigherClassOrInterface > liftClassOrInterface( String fullyQualifiedName ) {
		//System.out.println( "Lifting class: " + fullyQualifiedName );

		try {
			java.lang.Class<?> clazz = java.lang.Class.forName( fullyQualifiedName );

			// Skip inner classes
			if( clazz.isMemberClass() ) {
				// System.err.println( "WARNING: Class lifter does not support inner classes, skipping: " +
				// 		fullyQualifiedName );
				return Optional.empty();
			}

			var specialType = universe.specialTypeTag( fullyQualifiedName ).map( universe::specialType );
			if( specialType.isPresent() ) {
				return specialType;
			}

			var cachedType = universe.rootPackage().declaredType( fullyQualifiedName );
			if( cachedType.isPresent() ) {
				return cachedType;
			}

			if( clazz.isEnum() ) {
				return liftEnum( clazz );
			} else if( clazz.isInterface() ) {
				return liftInterface( clazz );
			} else {
				return liftClass( clazz );
			}
		} catch( ClassNotFoundException e ) {
			System.err.println( "WARNING: Could not find class: " + fullyQualifiedName );
			return Optional.empty();
		}
	}

	private Optional< HigherClassOrInterface > liftClass( java.lang.Class< ? > clazz ) {
		// for keeping track of which dependencies to lift
		Set< String > dependencyIdentifiers = new HashSet<>();

		java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
		
		// Extract dependencies from class members
		for( java.lang.reflect.Field field : clazz.getDeclaredFields() ) {
			if(Modifier.isPrivate(field.getModifiers())) continue;
			extractClassDependencies( dependencyIdentifiers, field.getGenericType() );
		}
		for(java.lang.reflect.Method method : clazz.getDeclaredMethods()){
			if( Modifier.isPrivate( method.getModifiers() ) ) continue;
			if(method.isBridge()) continue;
			addMethodDependencies(dependencyIdentifiers, method);
		}
		for(java.lang.reflect.Constructor< ? > constructor : clazz.getConstructors()){
			if( Modifier.isPrivate( constructor.getModifiers() ) ) continue;
			addMethodDependencies(dependencyIdentifiers, constructor);
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

		Package pkg = universe.rootPackage().declarePackage(clazz.getPackageName());

		EnumSet<choral.types.Modifier> modifiers = parseModifiers(choral.types.Modifier.class, clazz.getModifiers());

		HigherClass higherClass = new HigherClass(
			pkg, 
			modifiers, 
			clazz.getSimpleName(), 
			List.of( new World(universe, WORLD_IDENTIFIER) ),
			higherLiftTypeParameters( clazz.getTypeParameters() ));

		// recursively visit super class
		if( superClass != null ) {
			Optional< HigherClassOrInterface > result = liftClassOrInterface( superClass.getName() );
			if( result.isEmpty() ) {
				return Optional.empty();
			}
			if(result.get() instanceof HigherClass resultClass){
				higherClass.innerType().setExtendedClass(resultClass.innerType());
			}
		}

		// recursively visit super interfaces
		for( java.lang.Class< ? > superInterface : clazz.getInterfaces() ) {
			Optional< HigherClassOrInterface > result = liftClassOrInterface( superInterface.getName() );
			if( result.isEmpty() ) {
				return Optional.empty();
			}
			if(result.get() instanceof  HigherInterface resultInterface){
				GroundInterface groundSuperInterface = resultInterface.applyTo(higherClass.innerType().worldArguments());
				higherClass.innerType().addExtendedInterface(groundSuperInterface);
			}
			else {
				System.err.println("warning: non-interface found in extended interfaces");
			}
		}

		// recursively visit referenced classfiles
		for( String dependency : dependencyIdentifiers ) {
			liftClassOrInterface(dependency );
		}

		// add bounds to type parameters
		var choralParams = higherClass.typeParameters();
		var clazzParams = clazz.getTypeParameters();
		for(int i = 0; i < clazzParams.length; i++ ){
			try {
				addBounds(choralParams.get(i), clazzParams[i]);
			} catch (LiftException e) {
				// If we can't lift the type parameters, give up.
				warn(clazz.getCanonicalName(), e);
				return Optional.empty();
			}
		}

		// add fields
		for(java.lang.reflect.Field field : fields){
			// private fields will never be accessed
			if(Modifier.isPrivate(field.getModifiers())) continue;
			
			EnumSet<choral.types.Modifier> fieldModifiers = parseModifiers(choral.types.Modifier.class, field.getModifiers());
			GroundDataTypeOrVoid fieldTypeOrVoid;
			try{
				fieldTypeOrVoid = higherliftType(field.getGenericType());
			} catch(LiftException e){
				warn(field.getName(), e);
				continue;
			}
			if(fieldTypeOrVoid instanceof GroundDataType fieldType){
				Member.Field choralField = new Member.Field(
					higherClass.innerType(), 
					field.getName(), 
					fieldModifiers, 
					fieldType);
				higherClass.innerType().addField(choralField);
			} else{
				throw new RuntimeException("field '" + field.getName() + "' was found to be of type void: ");
			}
		} 

		// add methods
		for(Method method : clazz.getDeclaredMethods()){
			if(Modifier.isPrivate(method.getModifiers())) continue;
			Member.HigherMethod higherMethod;
			try{
				higherMethod = higherLiftMethod(method, higherClass.innerType());
			} catch(LiftException e){
				warn(method.getName(), e);
				continue;
			}
			higherClass.innerType().addMethod(higherMethod);
		}

		// add constructors to higherClass
		for(Constructor<?> constructor : clazz.getConstructors()){
			if(Modifier.isPrivate(constructor.getModifiers())) continue;
			Member.HigherConstructor higherConstructor;
			try{
				higherConstructor = higherLiftConstructor(constructor, higherClass.innerType());
			} catch(LiftException e){
				warn(constructor.getName(), e);
				continue;
			}
			higherClass.innerType().addConstructor(higherConstructor);
		}

		return Optional.of( higherClass );
	}

	private Optional< HigherClassOrInterface > liftInterface( java.lang.Class< ? > clazz ) {
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
			return Optional.empty();
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

		Package pkg = universe.rootPackage().declarePackage(clazz.getPackageName());

		EnumSet<choral.types.Modifier> modifiers = parseModifiers(choral.types.Modifier.class, clazz.getModifiers());

		World world = new World(universe, WORLD_IDENTIFIER);

		HigherInterface higherInterface = new HigherInterface(
			pkg, 
			modifiers, 
			clazz.getSimpleName(), 
			List.of(world),
			higherLiftTypeParameters(clazz.getTypeParameters()),
			choralInterface);

		// recursively visit super interfaces
		for( java.lang.Class< ? > superInterface : clazz.getInterfaces() ) {
			Optional< HigherClassOrInterface > result = liftClassOrInterface( superInterface.getName() );
			if( result.isEmpty() ) {
				return Optional.empty();
			}
			if(result.get() instanceof HigherInterface resultInterface){
				GroundInterface groundSuperInterface = resultInterface.applyTo(
					higherInterface.innerType().worldArguments());
				higherInterface.innerType().addExtendedInterface(groundSuperInterface);
			} else{
				System.err.println("warning: non-interface found in extended interfaces");
			}
		}

		// recursively visit referenced classfiles
		for( String dependency : dependencyIdentifiers ) {
			liftClassOrInterface( dependency );
		}

		// add bounds to type parameters
		var choralParams = higherInterface.typeParameters();
		var clazzParams = clazz.getTypeParameters();
		for(int i = 0; i < clazzParams.length; i++ ){
			try {
				addBounds(choralParams.get(i), clazzParams[i]);
			} catch (LiftException e) {
				// If we can't lift the type parameters, give up.
				warn(clazz.getCanonicalName(), e);
				return Optional.empty();
			}
		}

		// add methods to higherinterface
		for(Method method : clazz.getDeclaredMethods()){
			if(Modifier.isPrivate(method.getModifiers())) continue;
			Member.HigherMethod higherMethod;
			try{
				higherMethod = higherLiftMethod(method, higherInterface.innerType());
			} catch(LiftException e){
				warn(method.getName(), e);
				continue;
			}
			higherInterface.innerType().addMethod(higherMethod);
		}

		return Optional.of( higherInterface );
	}

	private Optional< HigherClassOrInterface > liftEnum( java.lang.Class<?> enumClass ) {
		// TRANSLATE CONSTANTS
		java.lang.reflect.Field[] allFields = enumClass.getFields();

		// TODO: Check if this line is actually needed / whether removing it breaks anything
		Package pkg = universe.rootPackage().declarePackage(enumClass.getPackageName());

		EnumSet<choral.types.Modifier> modifiers = parseModifiers(choral.types.Modifier.class, enumClass.getModifiers());
		modifiers.remove(choral.types.Modifier.ABSTRACT);

		World world = new World(universe, WORLD_IDENTIFIER);

		HigherEnum higherEnum = new HigherEnum(
			pkg, 
			modifiers, 
			enumClass.getSimpleName(), 
			world);

		for( java.lang.reflect.Field field : allFields){
			if( field.isEnumConstant()){
				higherEnum.innerType().addCase(field.getName());		
			}
		}

		return Optional.of( higherEnum );
	}

	/////////////////////////////////////////////////////////////////////
	///////////////////////// METHOD-LIFTING  ///////////////////////////
	/////////////////////////////////////////////////////////////////////

	private Member.HigherMethod higherLiftMethod(Method method,
	GroundClassOrInterface declarationContext) throws LiftException{
		EnumSet<choral.types.Modifier> methodModifiers = parseModifiers(
			choral.types.Modifier.class, method.getModifiers());

		Member.HigherMethod higherMethod = new Member.HigherMethod(
			declarationContext, 
			method.getName(), 
			methodModifiers, 
			higherLiftTypeParameters( method.getTypeParameters() ));

		// add bounds to type parameters
		var choralParams = higherMethod.typeParameters();
		var clazzParams = method.getTypeParameters();
		for(int i = 0; i < clazzParams.length; i++ ){
			addBounds(choralParams.get(i), clazzParams[i]);
		}

		for(java.lang.reflect.Type formalParam : method.getGenericParameterTypes()){
			GroundDataTypeOrVoid liftedFormalParameterOrVoid = higherliftType(formalParam);
			if(liftedFormalParameterOrVoid instanceof GroundDataType liftedFormalParameter){
				higherMethod.innerCallable().signature().addParameter(formalParam.getTypeName(), liftedFormalParameter);
			} else {
				throw new RuntimeException("method was found to contain void parameter: " 
				+ higherMethod.identifier());
			}
		}
		GroundDataTypeOrVoid returnType;
		returnType = higherliftType(method.getGenericReturnType());
		higherMethod.innerCallable().setReturnType(returnType);
		return higherMethod;
	}

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


	private Member.HigherConstructor higherLiftConstructor(Constructor<?> constructor,
	GroundClass declarationContext) throws LiftException{
		EnumSet<choral.types.Modifier> constructorModifiers = parseModifiers(
				choral.types.Modifier.class, constructor.getModifiers());
		Member.HigherConstructor higherConstructor = new Member.HigherConstructor(
			declarationContext, 
			constructorModifiers, 
			higherLiftTypeParameters( constructor.getTypeParameters() ));

		// add bounds to type parameters
		var choralParams = higherConstructor.typeParameters();
		var clazzParams = constructor.getTypeParameters();
		for(int i = 0; i < clazzParams.length; i++ ){
			addBounds(choralParams.get(i), clazzParams[i]);
		}

		// add formal parameters
		for(java.lang.reflect.Type type : constructor.getGenericParameterTypes()){
			GroundDataTypeOrVoid liftedTypeOrVoid = higherliftType(type);
			if(liftedTypeOrVoid instanceof GroundDataType liftedType ){
				higherConstructor.innerCallable().signature().addParameter(type.getTypeName(), liftedType);
			} else{
				throw new RuntimeException("constructor was found to contain void parameter: " 
				+ higherConstructor.identifier());
			}

		}
		return higherConstructor;
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

	private List< HigherTypeParameter > higherLiftTypeParameters(
			java.lang.reflect.TypeVariable< ? >[] typeParameters
	) {
		List< HigherTypeParameter > results = new ArrayList<>();
		for(java.lang.reflect.TypeVariable<?> typeParameter : typeParameters){
			HigherTypeParameter higherTypeParameter = new HigherTypeParameter(
				universe,
				typeParameter.getName(),
				List.of(new World(universe, WORLD_IDENTIFIER)));

			results.add( higherTypeParameter );
		}
		return results;
	}

	private void addBounds(
			HigherTypeParameter typeParameter,
			java.lang.reflect.Type upperBound
	) throws LiftException {
		// Skip Object as a bound - it's the default and not meaningful
		if(upperBound.equals(Object.class)){
			return;
		}
		GroundReferenceType liftedBound = (GroundReferenceType)higherliftType(upperBound);
		typeParameter.innerType().addUpperBound( liftedBound );
		// TODO "finalise" the bounds - see Typer
	}

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
	private static TypeExpression liftType( java.lang.reflect.Type type)
			throws LiftException {
		return liftType( type, List.of( DEFAULT_WORLD_ARGUMENT ) );
	}

	/**
	 * Generates the choral GroundDataTypeOrVoid from the given Java reflection Type.
	 * Does so recursively if given Type is nested (or has type arguments).
	 */
	private GroundDataTypeOrVoid higherliftType( java.lang.reflect.Type type )
			throws LiftException {
		return higherLiftType( type, List.of( new World(universe, WORLD_IDENTIFIER) ) );
	} 

	private GroundDataTypeOrVoid higherLiftType(
			java.lang.reflect.Type type,
			List< World > worlds
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

			// Handle primitive types, interfaces and regular classes
			String typeName;
			if( clazz.isPrimitive() ) {
				typeName = clazz.getName(); // "int", "void", etc.
				// void should not have world arguments
				if( typeName.equals( "void" ) ) {
					return universe.voidType();
				}	
				HigherPrimitiveDataType primitiveType = universe.primitiveDataType(
					PrimitiveTypeTag.valueOf(typeName.toUpperCase(Locale.ROOT)));
				return primitiveType.applyTo(worlds);
			} else if (clazz.isInterface()){
				typeName = clazz.getCanonicalName(); // fully qualified name
				Optional<HigherClassOrInterface> higherInterface = universe.rootPackage().declaredType( typeName );
				if(higherInterface.isEmpty()){
					higherInterface = liftClassOrInterface(typeName);
					if(higherInterface.isEmpty()){
						throw new RuntimeException("Missing type: " + "'" + typeName + 
							"' even after attempting to eagerly lift the type");
					}
				}
				return higherInterface.get().applyTo(worlds);
			}
			// if neither primitive nor interface, assume class 
			typeName = clazz.getCanonicalName(); 
			Optional<HigherClassOrInterface> higherClass = universe.rootPackage().declaredType(typeName);
			if(higherClass.isEmpty()){
				higherClass = liftClassOrInterface(typeName);
				if(higherClass.isEmpty()){
					throw new RuntimeException("Missing type: " + "'" + typeName + 
							"' even after attempting to eagerly lift the type");
				}
			}
			return higherClass.get().applyTo(worlds);
		}
		// Handle ParameterizedType (generic types like List<String>)
		else if( type instanceof java.lang.reflect.ParameterizedType paramType) {
			java.lang.reflect.Type rawType = paramType.getRawType();

			// rawType should be a Class
			java.lang.Class<?> rawClass = (java.lang.Class<?>) rawType;

			// Check if the raw class is an inner class
			if( rawClass.isMemberClass() ) {
				throw LiftException.innerClass();
			}

			List< HigherReferenceType > liftedTypeArguments = new ArrayList<>();
			java.lang.reflect.Type[] typeArgs = paramType.getActualTypeArguments();

			for( java.lang.reflect.Type typeArg : typeArgs ) {
				// Check for wildcards
				if( typeArg instanceof java.lang.reflect.WildcardType ) {
					throw LiftException.wildcard();
				}
				// Recursively process type arguments without world arguments
				GroundDataTypeOrVoid liftedTypeArgument = higherLiftType( 
						typeArg, Collections.emptyList() );
				if(liftedTypeArgument instanceof GroundReferenceType liftedHigherReferenceType){
					liftedTypeArguments.add( liftedHigherReferenceType.typeConstructor() );
				} else{
					throw new RuntimeException("Type argument returned isn't a GroundReferenceType: " 
					+ "'" + typeArg.getTypeName() + "'");
				}
			}

			// check if type has already been lifted 
			String typeName = rawClass.getCanonicalName();
			var parameterizedClass = liftClassOrInterface(typeName);
			if(parameterizedClass.isPresent()){
				parameterizedClass.get().applyTo(worlds, liftedTypeArguments);
				return parameterizedClass.get().innerType();
			} else{
				parameterizedClass = liftClassOrInterface(typeName);
				if(parameterizedClass.isPresent()){
					parameterizedClass.get().applyTo(worlds, liftedTypeArguments);
					return parameterizedClass.get().innerType();
				}
			}
			var parameterizedInterface = universe.rootPackage().declaredType(rawClass.getCanonicalName());
			if(parameterizedInterface.isEmpty()){
				parameterizedInterface = liftClassOrInterface(typeName);
				if(parameterizedInterface.isEmpty()){
					throw new RuntimeException("Missing type: '" + rawClass.getCanonicalName() 
						+ "' not found");
				}
			}
			parameterizedInterface.get().applyTo(worlds, liftedTypeArguments);
			return parameterizedInterface.get().innerType();
		}
		// Handle TypeVariable (type parameters like T, E, K, V)
		else if( type instanceof java.lang.reflect.TypeVariable< ? > typeVar ) {
			HigherTypeParameter higherTypeParameter = new HigherTypeParameter(
				universe, 
				typeVar.getName(), 
				worlds);
			return higherTypeParameter.innerType();
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
		else if( type instanceof java.lang.reflect.ParameterizedType paramType) {
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
}
