package choral.compiler.typer;

import choral.ast.Position;
import choral.compiler.TyperOptions;
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
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

class LiftException extends Exception {
  LiftException(String message) {
    super(message);
  }

  static LiftException array() {
    return new LiftException("array types are not supported");
  }

  static LiftException wildcard() {
    return new LiftException("wildcard types are not supported");
  }

  static LiftException innerClass() {
    return new LiftException("inner classes are not supported");
  }

  static LiftException exoticType(java.lang.reflect.Type type) {
    return new LiftException(type.getClass().getName() + " is not a supported type kind");
  }

  static LiftException notFound(String query) {
    return new LiftException(query + " could not be found on the classpath");
  }

  static LiftException castFailed(String typeName, String expectedKind) {
    return new LiftException("expected " + typeName + " to be a " + expectedKind);
  }
}

/**
 * ClassLifter is responsible for "lifting" Java class files into Choral's internal AST
 * representation. This allows Choral code to interact with existing Java classes without needing to
 * declare Choral headers for them manually.
 */
public class ClassLifter {

  private static final String WORLD_IDENTIFIER = "A";
  private final Universe universe;
  private final TaskQueue taskQueue;
  private final TyperOptions opts;

  /**
   * The position of the import or usage site that triggered the current lift operation. Set at the
   * start of {@link #lookup(String, Position)} and restored on exit. May be {@code null} when no
   * position is available.
   */
  private Position currentPosition;

  public ClassLifter(Universe universe, TaskQueue taskQueue, TyperOptions opts) {
    this.universe = universe;
    this.taskQueue = taskQueue;
    this.opts = opts;
  }

  /////////////////////////////////////////////////////////////////////
  ////////////////////// CLASS LIFTING METHODS  ///////////////////////
  /////////////////////////////////////////////////////////////////////

  /**
   * Looks up the given type on the classpath and lifts it into a Choral type - adding it to the
   * Universe as a side-effect. If warnings are emitted during lifting, they will be associated with
   * the given {@code position}.
   *
   * @param fullyQualifiedName The fully qualified name of type to be lifted.
   * @param position The position of the import or usage site that triggered this lookup (may be
   *     {@code null}).
   * @return A choral type representing the type, or nothing if classpath lookup failed.
   */
  public Optional<HigherClassOrInterface> lookup(String fullyQualifiedName, Position position) {
    Position previousPosition = this.currentPosition;
    this.currentPosition = position;
    try {
      return Optional.of(liftClassOrInterface(fullyQualifiedName));
    } catch (LiftException e) {
      return Optional.empty();
    } finally {
      this.currentPosition = previousPosition;
    }
  }

  private HigherClassOrInterface liftClassOrInterface(String fullyQualifiedName)
      throws LiftException {

    var specialType = universe.specialTypeTag(fullyQualifiedName).map(universe::specialType);
    if (specialType.isPresent()) {
      return specialType.get();
    }

    var cachedType = universe.rootPackage().declaredType(fullyQualifiedName);
    if (cachedType.isPresent()) {
      return cachedType.get();
    }

    java.lang.Class<?> clazz;
    try {
      clazz = java.lang.Class.forName(fullyQualifiedName);
    } catch (ClassNotFoundException e) {
      throw LiftException.notFound(fullyQualifiedName);
    }

    if (clazz.isMemberClass()) {
      throw LiftException.innerClass();
    } else if (clazz.isEnum()) {
      return liftEnum(clazz);
    } else if (clazz.isInterface()) {
      return liftInterface(clazz);
    } else {
      return liftClass(clazz);
    }
  }

  private HigherClassOrInterface liftClass(java.lang.Class<?> clazz) throws LiftException {

    Package pkg = universe.rootPackage().declarePackage(clazz.getPackageName());
    EnumSet<choral.types.Modifier> modifiers = parseModifiers(clazz.getModifiers());

    HigherClass higherClass =
        new HigherClass(
            pkg,
            modifiers,
            clazz.getSimpleName(),
            List.of(new World(universe, WORLD_IDENTIFIER)),
            liftTypeParameters(clazz.getTypeParameters()));

    ClassOrInterfaceInstanceScope scope =
        new CompilationUnitScope(pkg, List.of(), this).getScope(higherClass).getInstanceScope();

    addSuperClass(clazz, higherClass, scope); // Throws on failure
    addSuperInterfaces(clazz, higherClass, scope);
    higherClass.innerType().finaliseInheritance();

    addTypeBounds(clazz, higherClass, scope); // Throws on failure
    addFields(clazz.getDeclaredFields(), scope, higherClass);
    addMethods(clazz, higherClass, scope);
    addConstructors(clazz, higherClass, scope);

    taskQueue.enqueue(
        new TaskQueue.MemberTask(
            Phase.MEMBER_DECLARATIONS,
            higherClass,
            () -> higherClass.innerType().finaliseInterface()));
    return higherClass;
  }

  private HigherClassOrInterface liftInterface(java.lang.Class<?> clazz) throws LiftException {

    Package pkg = universe.rootPackage().declarePackage(clazz.getPackageName());
    EnumSet<choral.types.Modifier> modifiers = parseModifiers(clazz.getModifiers());

    HigherInterface higherInterface =
        new HigherInterface(
            pkg,
            modifiers,
            clazz.getSimpleName(),
            List.of(new World(universe, WORLD_IDENTIFIER)),
            liftTypeParameters(clazz.getTypeParameters()));

    ClassOrInterfaceInstanceScope scope =
        new CompilationUnitScope(pkg, List.of(), this).getScope(higherInterface).getInstanceScope();

    addSuperInterfaces(clazz, higherInterface, scope);
    higherInterface.innerType().finaliseInheritance();

    addTypeBounds(clazz, higherInterface, scope);
    addMethods(clazz, higherInterface, scope);

    taskQueue.enqueue(
        new TaskQueue.MemberTask(
            Phase.MEMBER_DECLARATIONS,
            higherInterface,
            () -> higherInterface.innerType().finaliseInterface()));
    return higherInterface;
  }

  private HigherClassOrInterface liftEnum(java.lang.Class<?> clazz) {

    Package pkg = universe.rootPackage().declarePackage(clazz.getPackageName());
    EnumSet<choral.types.Modifier> modifiers = parseModifiers(clazz.getModifiers());

    HigherEnum higherEnum =
        new HigherEnum(
            pkg, modifiers, clazz.getSimpleName(), new World(universe, WORLD_IDENTIFIER));

    ClassOrInterfaceInstanceScope scope =
        new CompilationUnitScope(pkg, List.of(), this).getScope(higherEnum).getInstanceScope();

    higherEnum.innerType().setExtendedClass();
    addSuperInterfaces(clazz, higherEnum, scope);
    higherEnum.innerType().finaliseInheritance();

    for (java.lang.reflect.Field field : clazz.getFields()) {
      if (field.isEnumConstant()) {
        higherEnum.innerType().addCase(field.getName());
      }
    }
    addMethods(clazz, higherEnum, scope);

    taskQueue.enqueue(
        new TaskQueue.MemberTask(
            Phase.MEMBER_DECLARATIONS,
            higherEnum,
            () -> higherEnum.innerType().finaliseInterface()));
    return higherEnum;
  }

  private void addSuperClass(
      Class<?> clazz, HigherClass higherClass, ClassOrInterfaceInstanceScope scope)
      throws LiftException {
    Type genericSuperClass = clazz.getGenericSuperclass();
    if (genericSuperClass != null) {
      higherClass.innerType().setExtendedClass(liftClass(genericSuperClass, scope));
    }
  }

  /**
   * Adds each of the given Java class's generic super-interfaces to the inner type of the given
   * Choral type. Super-interfaces that fail to lift are skipped with a warning.
   */
  private void addSuperInterfaces(
      java.lang.Class<?> clazz,
      HigherClassOrInterface higherType,
      ClassOrInterfaceInstanceScope scope) {
    String fullyQualifiedName = clazz.getCanonicalName();
    for (java.lang.reflect.Type genericSuperInterface : clazz.getGenericInterfaces()) {
      GroundInterface liftedSuperInterface;
      try {
        liftedSuperInterface = liftInterface(genericSuperInterface, scope);
      } catch (LiftException e) {
        warn(fullyQualifiedName, e);
        continue;
      }
      higherType.innerType().addExtendedInterface(liftedSuperInterface);
    }
  }

  private void addTypeBounds(
      Class<?> clazz, HigherClassOrInterface higherType, ClassOrInterfaceInstanceScope scope)
      throws LiftException {
    var choralParams = higherType.typeParameters();
    var clazzParams = clazz.getTypeParameters();
    for (int i = 0; i < clazzParams.length; i++) {
      // If we can't lift the type parameters, pass the exception up to the caller.
      addBounds(choralParams.get(i), clazzParams[i].getBounds(), scope);
    }
  }

  private void addMethods(
      java.lang.Class<?> clazz,
      HigherClassOrInterface higherType,
      ClassOrInterfaceInstanceScope scope) {
    for (Method method : clazz.getDeclaredMethods()) {
      if (Modifier.isPrivate(method.getModifiers())) continue;
      if (method.isBridge()) continue;
      Member.HigherMethod higherMethod;
      try {
        higherMethod = liftMethod(method, higherType.innerType(), scope);
      } catch (LiftException e) {
        warn(clazz.getCanonicalName() + "#" + method.getName(), e);
        continue;
      }
      higherType.innerType().addMethod(higherMethod);
    }
  }

  private void addConstructors(
      Class<?> clazz, HigherClass higherClass, ClassOrInterfaceInstanceScope scope) {
    for (Constructor<?> constructor : clazz.getConstructors()) {
      if (Modifier.isPrivate(constructor.getModifiers())) continue;
      Member.HigherConstructor higherConstructor;
      try {
        higherConstructor = liftConstructor(constructor, higherClass.innerType(), scope);
      } catch (LiftException e) {
        warn(clazz.getCanonicalName() + "#" + constructor.getName(), e);
        continue;
      }
      higherClass.innerType().addConstructor(higherConstructor);
    }
  }

  private void addFields(
      Field[] fields, ClassOrInterfaceInstanceScope scope, HigherClassOrInterface higherClass) {
    for (Field field : fields) {
      // private fields will never be accessed
      if (Modifier.isPrivate(field.getModifiers())) continue;

      EnumSet<choral.types.Modifier> fieldModifiers = parseModifiers(field.getModifiers());
      GroundDataType fieldType;
      try {
        fieldType = liftDataType(field.getGenericType(), scope);
      } catch (LiftException e) {
        warn(field.getClass().getCanonicalName() + "#" + field.getName(), e);
        continue;
      }
      Member.Field choralField =
          new Member.Field(higherClass.innerType(), field.getName(), fieldModifiers, fieldType);
      higherClass.innerType().addField(choralField);
    }
  }

  /////////////////////////////////////////////////////////////////////
  ///////////////////////// METHOD-LIFTING  ///////////////////////////
  /////////////////////////////////////////////////////////////////////

  private Member.HigherMethod liftMethod(
      Method method, GroundClassOrInterface declarationContext, ClassOrInterfaceInstanceScope scope)
      throws LiftException {
    EnumSet<choral.types.Modifier> methodModifiers = parseModifiers(method.getModifiers());
    // Default not part of modifier bits
    if (method.isDefault()) methodModifiers.add(choral.types.Modifier.valueOf("DEFAULT"));

    Member.HigherMethod higherMethod =
        new Member.HigherMethod(
            declarationContext,
            method.getName(),
            methodModifiers,
            liftTypeParameters(method.getTypeParameters()));

    CallableScope methodScope = scope.getScope(higherMethod);

    // add bounds to type parameters
    var choralParams = higherMethod.typeParameters();
    var clazzParams = method.getTypeParameters();
    for (int i = 0; i < clazzParams.length; i++) {
      addBounds(choralParams.get(i), clazzParams[i].getBounds(), scope);
    }

    int i = 0;
    for (java.lang.reflect.Type formalParam : method.getGenericParameterTypes()) {
      higherMethod
          .innerCallable()
          .signature()
          .addParameter("x" + i++, liftDataType(formalParam, methodScope));
    }
    GroundDataTypeOrVoid returnType = liftType(method.getGenericReturnType(), methodScope);
    higherMethod.innerCallable().setReturnType(returnType);

    higherMethod.innerCallable().finalise();
    return higherMethod;
  }

  private Member.HigherConstructor liftConstructor(
      Constructor<?> constructor,
      GroundClass declarationContext,
      ClassOrInterfaceInstanceScope scope)
      throws LiftException {
    EnumSet<choral.types.Modifier> constructorModifiers =
        parseModifiers(constructor.getModifiers());
    Member.HigherConstructor higherConstructor =
        new Member.HigherConstructor(
            declarationContext,
            constructorModifiers,
            liftTypeParameters(constructor.getTypeParameters()));

    // add bounds to type parameters
    var choralParams = higherConstructor.typeParameters();
    var clazzParams = constructor.getTypeParameters();
    for (int i = 0; i < clazzParams.length; i++) {
      addBounds(choralParams.get(i), clazzParams[i].getBounds(), scope);
    }

    // add formal parameters
    int i = 0;
    for (java.lang.reflect.Type type : constructor.getGenericParameterTypes()) {
      GroundDataTypeOrVoid liftedTypeOrVoid = liftType(type, scope);
      if (liftedTypeOrVoid instanceof GroundDataType liftedType) {
        higherConstructor.innerCallable().signature().addParameter("arg" + i++, liftedType);
      } else {
        throw new RuntimeException(
            "constructor was found to contain void parameter: " + higherConstructor.identifier());
      }
    }

    higherConstructor.innerCallable().finalise();
    return higherConstructor;
  }

  /////////////////////////////////////////////////////////////////////
  //////////////////// S FOR LIFTING TYPES  /////////////////////
  /////////////////////////////////////////////////////////////////////

  private List<HigherTypeParameter> liftTypeParameters(
      java.lang.reflect.TypeVariable<?>[] typeParameters) {
    List<HigherTypeParameter> results = new ArrayList<>();
    for (java.lang.reflect.TypeVariable<?> typeParameter : typeParameters) {
      HigherTypeParameter higherTypeParameter =
          new HigherTypeParameter(
              universe, typeParameter.getName(), List.of(new World(universe, WORLD_IDENTIFIER)));

      results.add(higherTypeParameter);
    }
    return results;
  }

  private void addBounds(
      HigherTypeParameter typeParameter,
      java.lang.reflect.Type[] upperBounds,
      ClassOrInterfaceInstanceScope scope)
      throws LiftException {
    TypeParameterScope parameterScope = scope.getScope(typeParameter);
    for (java.lang.reflect.Type bound : upperBounds) {
      if (bound.equals(Object.class)) continue;
      GroundReferenceType liftedBound = liftReferenceType(bound, parameterScope);
      typeParameter.innerType().addUpperBound(liftedBound);
    }
    // Locks the type parameter so that no more bounds can be added
    // The type parameter is at this point considered finished.
    // Puts the type parameter into a "read-only" mode.
    typeParameter.innerType().finaliseBound();
  }

  /**
   * Generates the choral GroundDataTypeOrVoid from the given Java reflection Type. Does so
   * recursively if given Type is nested (or has type arguments).
   */
  private GroundDataTypeOrVoid liftType(java.lang.reflect.Type type, Scope scope)
      throws LiftException {
    List<World> worlds = List.of(scope.lookupWorldParameter(WORLD_IDENTIFIER).get());

    // Handle Class types (includes primitive types and regular classes)
    // We check the class name to avoid conflict with choral.ast.body.Class import
    if (type.getClass().getName().equals("java.lang.Class")) {
      java.lang.Class<?> clazz = (java.lang.Class<?>) type;

      // Handle array types
      if (clazz.isArray()) {
        throw LiftException.array();
      }

      // Handle inner classes
      if (clazz.isMemberClass()) {
        throw LiftException.innerClass();
      }

      if (clazz.isPrimitive()) {
        String typeName = clazz.getName(); // "int", "void", etc.
        // void should not have world arguments
        if (typeName.equals("void")) {
          return universe.voidType();
        }
        HigherPrimitiveDataType primitiveType =
            universe.primitiveDataType(PrimitiveTypeTag.valueOf(typeName.toUpperCase(Locale.ROOT)));
        return primitiveType.applyTo(worlds);
      } else {
        String typeName = clazz.getCanonicalName();
        return liftClassOrInterface(typeName).applyTo(worlds);
      }
    }
    // Handle ParameterizedType (generic types like List<String>)
    else if (type instanceof java.lang.reflect.ParameterizedType paramType) {
      java.lang.reflect.Type rawType = paramType.getRawType();

      // rawType should be a Class
      java.lang.Class<?> rawClass = (java.lang.Class<?>) rawType;

      // Check if the raw class is an inner class
      if (rawClass.isMemberClass()) {
        throw LiftException.innerClass();
      }

      List<HigherReferenceType> liftedTypeArguments = new ArrayList<>();
      java.lang.reflect.Type[] typeArgs = paramType.getActualTypeArguments();

      for (java.lang.reflect.Type typeArg : typeArgs) {
        // Check for wildcards
        if (typeArg instanceof java.lang.reflect.WildcardType) {
          throw LiftException.wildcard();
        }
        GroundDataTypeOrVoid liftedTypeArgument = liftType(typeArg, scope);
        if (liftedTypeArgument instanceof GroundReferenceType liftedHigherReferenceType) {
          liftedTypeArguments.add(liftedHigherReferenceType.typeConstructor());
        } else {
          throw new RuntimeException(
              "Type argument returned isn't a GroundReferenceType: "
                  + "'"
                  + typeArg.getTypeName()
                  + "'");
        }
      }

      String typeName = rawClass.getCanonicalName();
      return liftClassOrInterface(typeName).applyTo(worlds, liftedTypeArguments);
    }
    // Handle TypeVariable (type parameters like T, E, K, V)
    else if (type instanceof java.lang.reflect.TypeVariable<?> typeVar) {
      HigherTypeParameter higherTypeParameter = scope.assertLookupTypeParameter(typeVar.getName());
      return higherTypeParameter.applyTo(worlds);
    }
    // Handle GenericArrayType (generic array types like T[])
    else if (type instanceof java.lang.reflect.GenericArrayType) {
      throw LiftException.array();
    }
    // Handle WildcardType (wildcard types like ? extends T, ? super T)
    else if (type instanceof java.lang.reflect.WildcardType) {
      throw LiftException.wildcard();
    } else {
      throw LiftException.exoticType(type);
    }
  }

  private GroundClass liftClass(java.lang.reflect.Type type, Scope scope) throws LiftException {
    GroundDataTypeOrVoid lifted = liftType(type, scope);
    if (lifted instanceof GroundClass gc) return gc;
    throw LiftException.castFailed(type.getTypeName(), "class");
  }

  private GroundInterface liftInterface(java.lang.reflect.Type type, Scope scope)
      throws LiftException {
    GroundDataTypeOrVoid lifted = liftType(type, scope);
    if (lifted instanceof GroundInterface gi) return gi;
    throw LiftException.castFailed(type.getTypeName(), "interface");
  }

  private GroundDataType liftDataType(java.lang.reflect.Type type, Scope scope)
      throws LiftException {
    GroundDataTypeOrVoid lifted = liftType(type, scope);
    if (lifted instanceof GroundDataType gd) return gd;
    throw LiftException.castFailed(type.getTypeName(), "data type");
  }

  private GroundReferenceType liftReferenceType(java.lang.reflect.Type type, Scope scope)
      throws LiftException {
    GroundDataTypeOrVoid lifted = liftType(type, scope);
    if (lifted instanceof GroundReferenceType gd) return gd;
    throw LiftException.castFailed(type.getTypeName(), "reference type");
  }

  /////////////////////////////////////////////////////////////////////
  ////////////////////// DEPENDENCY MANAGEMENT  ///////////////////////
  /////////////////////////////////////////////////////////////////////

  /** Parses modifiers found by the Java reflection API into modifiers used by choral internals. */
  private static EnumSet<choral.types.Modifier> parseModifiers(int modifierBits) {
    EnumSet<choral.types.Modifier> modifiers = EnumSet.noneOf(choral.types.Modifier.class);
    for (String modifier : Modifier.toString(modifierBits).split(" ")) {
      try {
        modifiers.add(choral.types.Modifier.valueOf(modifier.toUpperCase()));
      } catch (IllegalArgumentException e) {
        continue;
      }
    }
    return modifiers;
  }

  private void warn(String id, LiftException e) {
    opts.info(currentPosition, "ClassLifter failed to lift " + id + ": " + e.getMessage());
  }
}
