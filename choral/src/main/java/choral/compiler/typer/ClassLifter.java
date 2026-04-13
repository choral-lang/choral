package choral.compiler.typer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import choral.compiler.typer.scope.CallableScope;
import choral.compiler.typer.scope.ClassOrInterfaceInstanceScope;
import choral.compiler.typer.scope.CompilationUnitScope;
import choral.compiler.typer.scope.Scope;
import choral.compiler.typer.scope.TypeParameterScope;
import choral.types.GroundClass;
import choral.types.GroundClassOrInterface;
import choral.types.GroundDataType;
import choral.types.GroundDataTypeOrVoid;
import choral.types.GroundInterface;
import choral.types.GroundReferenceType;
import choral.types.HigherClass;
import choral.types.HigherClassOrInterface;
import choral.types.HigherEnum;
import choral.types.HigherInterface;
import choral.types.HigherPrimitiveDataType;
import choral.types.HigherReferenceType;
import choral.types.HigherTypeParameter;
import choral.types.Member;
import choral.types.Package;
import choral.types.Universe;
import choral.types.Universe.PrimitiveTypeTag;
import choral.types.World;

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

	static LiftException exoticType( java.lang.reflect.Type type ) {
		return new LiftException( type.getClass().getName() );
	}
}

/**
 * ClassLifter is responsible for "lifting" Java class files into Choral's internal AST
 * representation. This allows Choral code to interact with existing Java classes without
 * needing to declare Choral headers for them manually.
 */
public class ClassLifter {

	private static final String WORLD_IDENTIFIER = "A";
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
	public Optional< HigherClassOrInterface > liftClassOrInterface( String fullyQualifiedName ){
		Optional< HigherClassOrInterface > result = liftClassOrInterfaceHelper(fullyQualifiedName);
		taskQueue.process();
		return result;
	}

	/**
	 * Looks up the given type on the classpath and lifts it into a Choral type - adding it
	 * to the Universe as a side-effect.
	 *
	 * @param fullyQualifiedName The fully qualified name of type to be lifted.
	 * @return A choral type representing the type, or nothing if classpath lookup failed.
	 */
	private Optional< HigherClassOrInterface > liftClassOrInterfaceHelper( String fullyQualifiedName ) {
		var specialType = universe.specialTypeTag( fullyQualifiedName ).map( universe::specialType );
		if( specialType.isPresent() ) {
			return specialType;
		}

		var cachedType = universe.rootPackage().declaredType( fullyQualifiedName );
		if( cachedType.isPresent() ) {
			return cachedType;
		}

		try {
			// System.out.println( "Loading class or interface: " + fullyQualifiedName );
			java.lang.Class<?> clazz = java.lang.Class.forName( fullyQualifiedName );

			// Skip inner classes
			if( clazz.isMemberClass() ) {
				System.err.println( "WARNING: Class lifter does not support inner classes, skipping: " + fullyQualifiedName );
				return Optional.empty();
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
		String fullyQualifiedName = clazz.getCanonicalName();
		java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
		
		Package pkg = universe.rootPackage().declarePackage(clazz.getPackageName());

		EnumSet<choral.types.Modifier> modifiers = parseModifiers(clazz.getModifiers());

		HigherClass higherClass = new HigherClass(
			pkg, 
			modifiers, 
			clazz.getSimpleName(), 
			List.of( new World(universe, WORLD_IDENTIFIER) ),
			liftTypeParameters( clazz.getTypeParameters() ));
		List<? extends World> worlds = higherClass.worldParameters();

		ClassOrInterfaceInstanceScope scope = new CompilationUnitScope( pkg, List.of() )
				.getScope( higherClass ).getInstanceScope();

		// recursively visit super class
		java.lang.reflect.Type genericSuperClass = clazz.getGenericSuperclass();
		if(genericSuperClass != null){
			GroundClass liftedSuperClass;
			try {
				liftedSuperClass = (GroundClass)liftSuperType(genericSuperClass, scope, worlds);
			} catch (LiftException e) {
				warn(fullyQualifiedName, e);
				return Optional.empty();
			}
			higherClass.innerType().setExtendedClass(liftedSuperClass);
		}

		// recursively visit super interfaces
		for(java.lang.reflect.Type genericSuperInterface : clazz.getGenericInterfaces()){
			GroundInterface liftedSuperInterface;
			try {
				liftedSuperInterface = (GroundInterface)liftSuperType(genericSuperInterface, scope, worlds);
			} catch (LiftException e) {
				warn(fullyQualifiedName, e);
				continue;
			}
			higherClass.innerType().addExtendedInterface(liftedSuperInterface);
		}

		higherClass.innerType().finaliseInheritance();

		// add bounds to type parameters
		var choralParams = higherClass.typeParameters();
		var clazzParams = clazz.getTypeParameters();
		for(int i = 0; i < clazzParams.length; i++ ){
			try {
				addBounds(choralParams.get(i), clazzParams[i].getBounds(), scope);
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
			
			EnumSet<choral.types.Modifier> fieldModifiers = parseModifiers( field.getModifiers());
			GroundDataTypeOrVoid fieldTypeOrVoid;
			try{
				fieldTypeOrVoid = liftType(field.getGenericType(), scope);
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
			if(method.isBridge()) continue;
			Member.HigherMethod higherMethod;
			try{
				higherMethod = liftMethod(method, higherClass.innerType(), scope);
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
				higherConstructor = liftConstructor(constructor, higherClass.innerType(), scope);
			} catch(LiftException e){
				warn(constructor.getName(), e);
				continue;
			}
			higherClass.innerType().addConstructor(higherConstructor);
		}

		TaskQueue.MemberTask task = new TaskQueue.MemberTask(Phase.MEMBER_DECLARATIONS, higherClass, () -> {
			higherClass.innerType().finaliseInterface();
		});
		taskQueue.enqueue(task);
		return Optional.of( higherClass );
	}

	private Optional< HigherClassOrInterface > liftInterface( java.lang.Class< ? > clazz ) {
		String fullyQualifiedName = clazz.getCanonicalName();

		Package pkg = universe.rootPackage().declarePackage(clazz.getPackageName());

		EnumSet<choral.types.Modifier> modifiers = parseModifiers(clazz.getModifiers());

		HigherInterface higherInterface = new HigherInterface(
			pkg, 
			modifiers, 
			clazz.getSimpleName(), 
			List.of(new World(universe, WORLD_IDENTIFIER)),
			liftTypeParameters(clazz.getTypeParameters()));
		List<? extends World> worlds = higherInterface.worldParameters();

		ClassOrInterfaceInstanceScope scope = new CompilationUnitScope( pkg, List.of() )
				.getScope( higherInterface ).getInstanceScope();

		// recursively visit super interfaces
		for(java.lang.reflect.Type genericSuperInterface : clazz.getGenericInterfaces()){
			GroundInterface liftedSuperInterface;
			try {
				liftedSuperInterface = (GroundInterface)liftSuperType(genericSuperInterface, scope, worlds);
			} catch (LiftException e) {
				warn(fullyQualifiedName, e);
				continue;
			}
			higherInterface.innerType().addExtendedInterface(liftedSuperInterface);
		}

		higherInterface.innerType().finaliseInheritance();

		// add bounds to type parameters
		var choralParams = higherInterface.typeParameters();
		var clazzParams = clazz.getTypeParameters();
		for(int i = 0; i < clazzParams.length; i++ ){
			try {
				addBounds(choralParams.get(i), clazzParams[i].getBounds(), scope);
			} catch (LiftException e) {
				// If we can't lift the type parameters, give up.
				warn(clazz.getCanonicalName(), e);
				return Optional.empty();
			}
		}

		// add methods to higherinterface
		for(Method method : clazz.getDeclaredMethods()){
			if(Modifier.isPrivate(method.getModifiers())) continue;
			if(method.isBridge()) continue;
			Member.HigherMethod higherMethod;
			try{
				higherMethod = liftMethod(method, higherInterface.innerType(), scope);
			} catch(LiftException e){
				warn(method.getName(), e);
				continue;
			}
			higherInterface.innerType().addMethod(higherMethod);
		}

		TaskQueue.MemberTask task = new TaskQueue.MemberTask(Phase.MEMBER_DECLARATIONS, higherInterface, () -> {
			higherInterface.innerType().finaliseInterface();
		});
		taskQueue.enqueue(task);
		return Optional.of( higherInterface );
	}

	private Optional< HigherClassOrInterface > liftEnum( java.lang.Class<?> enumClass ) {
		// TRANSLATE CONSTANTS
		java.lang.reflect.Field[] allFields = enumClass.getFields();

		Package pkg = universe.rootPackage().declarePackage(enumClass.getPackageName());

		EnumSet<choral.types.Modifier> modifiers = parseModifiers(enumClass.getModifiers());
		modifiers.remove(choral.types.Modifier.ABSTRACT);

		HigherEnum higherEnum = new HigherEnum(
			pkg, 
			modifiers, 
			enumClass.getSimpleName(), 
			new World(universe, WORLD_IDENTIFIER));

		higherEnum.innerType().setExtendedClass();
		higherEnum.innerType().finaliseInheritance();

		for( java.lang.reflect.Field field : allFields){
			if( field.isEnumConstant()){
				higherEnum.innerType().addCase(field.getName());		
			}
		}

		TaskQueue.MemberTask task = new TaskQueue.MemberTask(Phase.MEMBER_DECLARATIONS, higherEnum, () -> {
			higherEnum.innerType().finaliseInterface();
		});
		taskQueue.enqueue(task);
		return Optional.of( higherEnum );
	}

	private GroundReferenceType liftSuperType(java.lang.reflect.Type superType, 
			ClassOrInterfaceInstanceScope scope, List<? extends World> worlds) throws LiftException{
		String rawName;
		List<HigherReferenceType> typeArgs = new ArrayList<>();

		if(superType instanceof ParameterizedType parameterizedSuper){
			java.lang.Class<?> rawSuper = (java.lang.Class<?>)parameterizedSuper.getRawType();
			if(rawSuper.isMemberClass()) throw LiftException.innerClass();
			rawName = rawSuper.getName();

			for (java.lang.reflect.Type typeArgument : parameterizedSuper.getActualTypeArguments()){
				if(typeArgument instanceof java.lang.reflect.WildcardType) throw LiftException.wildcard();
				GroundDataTypeOrVoid liftedTypeArgument = liftType(typeArgument, scope);
				if(liftedTypeArgument instanceof  GroundReferenceType validTypeArgument){
					typeArgs.add(validTypeArgument.typeConstructor());
				} else {
					throw new RuntimeException("Type argument resolved to void in super-type: " + liftedTypeArgument);
				}
			}
		} else if (superType instanceof java.lang.Class<?> rawSuper){
			if(rawSuper.isMemberClass()) throw LiftException.innerClass();
			rawName = rawSuper.getName();
		} else {
			throw LiftException.exoticType(superType);
		}

		Optional<HigherClassOrInterface> higherType = liftClassOrInterfaceHelper(rawName);
		if(higherType.isEmpty()){
			throw new LiftException("Could not lift parent type: " + rawName);
		}

		return typeArgs.isEmpty() ? (GroundReferenceType) higherType.get().applyTo(worlds)
			: (GroundReferenceType) higherType.get().applyTo(worlds, typeArgs);
	}

	/////////////////////////////////////////////////////////////////////
	///////////////////////// METHOD-LIFTING  ///////////////////////////
	/////////////////////////////////////////////////////////////////////

	private Member.HigherMethod liftMethod(Method method,
			GroundClassOrInterface declarationContext,
			ClassOrInterfaceInstanceScope scope) throws LiftException{
		//System.out.println("Lifting method: " + method + " in " + declarationContext);
		EnumSet<choral.types.Modifier> methodModifiers = parseModifiers(method.getModifiers());
		// Default not part of modifier bits
		if(method.isDefault()) methodModifiers.add(choral.types.Modifier.valueOf("DEFAULT"));

		Member.HigherMethod higherMethod = new Member.HigherMethod(
			declarationContext, 
			method.getName(), 
			methodModifiers, 
			liftTypeParameters( method.getTypeParameters() ));

		CallableScope methodScope = scope.getScope(higherMethod);

		// add bounds to type parameters
		var choralParams = higherMethod.typeParameters();
		var clazzParams = method.getTypeParameters();
		for(int i = 0; i < clazzParams.length; i++ ){
			addBounds(choralParams.get(i), clazzParams[i].getBounds(), scope);
		}

		int i = 0;
		for(java.lang.reflect.Type formalParam : method.getGenericParameterTypes()){
			higherMethod.innerCallable().signature().addParameter(
					"x" + i++,
					(GroundDataType)liftType(formalParam, methodScope) );
		}
		GroundDataTypeOrVoid returnType = liftType(method.getGenericReturnType(), methodScope);
		higherMethod.innerCallable().setReturnType(returnType);

		higherMethod.innerCallable().finalise();
		return higherMethod;
	}

	private Member.HigherConstructor liftConstructor(Constructor<?> constructor,
			GroundClass declarationContext, ClassOrInterfaceInstanceScope scope) throws LiftException{
		EnumSet<choral.types.Modifier> constructorModifiers = parseModifiers(constructor.getModifiers());
		Member.HigherConstructor higherConstructor = new Member.HigherConstructor(
			declarationContext, 
			constructorModifiers, 
			liftTypeParameters( constructor.getTypeParameters() ));

		// add bounds to type parameters
		var choralParams = higherConstructor.typeParameters();
		var clazzParams = constructor.getTypeParameters();
		for(int i = 0; i < clazzParams.length; i++ ){
			addBounds(choralParams.get(i), clazzParams[i].getBounds(), scope);
		}

		// add formal parameters
		int i = 0;
		for(java.lang.reflect.Type type : constructor.getGenericParameterTypes()){
			GroundDataTypeOrVoid liftedTypeOrVoid = liftType(type, scope);
			if(liftedTypeOrVoid instanceof GroundDataType liftedType ){
				higherConstructor.innerCallable().signature().addParameter("arg" + i++, liftedType);
			} else{
				throw new RuntimeException("constructor was found to contain void parameter: " 
				+ higherConstructor.identifier());
			}
		}

		higherConstructor.innerCallable().finalise();
		return higherConstructor;
	}
	/////////////////////////////////////////////////////////////////////
	//////////////////// HELPERS FOR LIFTING TYPES  /////////////////////
	/////////////////////////////////////////////////////////////////////

	private List< HigherTypeParameter > liftTypeParameters(
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
			java.lang.reflect.Type[] upperBounds,
			ClassOrInterfaceInstanceScope scope
	) throws LiftException {
		TypeParameterScope parameterScope = scope.getScope( typeParameter );
		for(java.lang.reflect.Type bound : upperBounds){
			if(bound.equals(Object.class)) continue;
			GroundReferenceType liftedBound = (GroundReferenceType) liftType(bound, parameterScope);
			typeParameter.innerType().addUpperBound( liftedBound );
		}
		// Locks the type parameter so that no more bounds can be added
		// The type parameter is at this point considered finished. 
		// Puts the type parameter into a "read-only" mode. 
		typeParameter.innerType().finaliseBound();
	}


	/**
	 * Generates the choral GroundDataTypeOrVoid from the given Java reflection Type.
	 * Does so recursively if given Type is nested (or has type arguments).
	 */
	private GroundDataTypeOrVoid liftType(java.lang.reflect.Type type, Scope scope)
			throws LiftException {
		List< World > worlds = List.of( scope.lookupWorldParameter(WORLD_IDENTIFIER).get() );

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

			if( clazz.isPrimitive() ) {
				String typeName = clazz.getName(); // "int", "void", etc.
				// void should not have world arguments
				if( typeName.equals( "void" ) ) {
					return universe.voidType();
				}	
				HigherPrimitiveDataType primitiveType = universe.primitiveDataType(
					PrimitiveTypeTag.valueOf(typeName.toUpperCase(Locale.ROOT)));
				return primitiveType.applyTo(worlds);
			} else {
				String typeName = clazz.getCanonicalName();
				Optional<HigherClassOrInterface> higherType = liftClassOrInterfaceHelper(typeName);
				if(higherType.isEmpty()){
					throw new RuntimeException("Missing type: " + "'" + typeName +
						"' even after attempting to eagerly lift the type");
				}
				return higherType.get().applyTo(worlds);
			}
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
				GroundDataTypeOrVoid liftedTypeArgument = liftType( typeArg, scope );
				if(liftedTypeArgument instanceof GroundReferenceType liftedHigherReferenceType){
					liftedTypeArguments.add( liftedHigherReferenceType.typeConstructor() );
				} else{
					throw new RuntimeException("Type argument returned isn't a GroundReferenceType: " 
					+ "'" + typeArg.getTypeName() + "'");
				}
			}

			String typeName = rawClass.getCanonicalName();
			Optional<HigherClassOrInterface> higherType = liftClassOrInterfaceHelper(typeName);
			if(higherType.isEmpty()){
				throw new RuntimeException("Missing type: " + "'" + typeName +
						"' even after attempting to eagerly lift the type");
			}
			return higherType.get().applyTo(worlds, liftedTypeArguments);
		}
		// Handle TypeVariable (type parameters like T, E, K, V)
		else if( type instanceof java.lang.reflect.TypeVariable< ? > typeVar ) {			
			HigherTypeParameter higherTypeParameter =
				scope.assertLookupTypeParameter( typeVar.getName() );
			return higherTypeParameter.applyTo( worlds );
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
			throw LiftException.exoticType( type );
		}
	}


	/////////////////////////////////////////////////////////////////////
	////////////////////// DEPENDENCY MANAGEMENT  ///////////////////////
	/////////////////////////////////////////////////////////////////////

	/**
	 * Parses modifiers found by the Java reflection API into modifiers used by choral internals.
	 */
	private static EnumSet< choral.types.Modifier > parseModifiers( int modifierBits ) {
		EnumSet<choral.types.Modifier> modifiers = EnumSet.noneOf( choral.types.Modifier.class );
		for( String modifier : Modifier.toString( modifierBits ).split(" ") ) {
			try {
				modifiers.add( choral.types.Modifier.valueOf( modifier.toUpperCase() ) );
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
