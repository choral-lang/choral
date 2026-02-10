package choral.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import choral.ast.CompilationUnit;
import choral.ast.Name;
import choral.ast.Position;
import choral.ast.body.Annotation;
import choral.ast.body.Class;
import choral.ast.body.ClassMethodDefinition;
import choral.ast.body.ClassMethodModifier;
import choral.ast.body.ClassModifier;
import choral.ast.body.ConstructorDefinition;
import choral.ast.body.ConstructorModifier;
import choral.ast.body.ConstructorSignature;
import choral.ast.body.EnumConstant;
import choral.ast.body.Field;
import choral.ast.body.FieldModifier;
import choral.ast.body.FormalMethodParameter;
import choral.ast.body.Interface;
import choral.ast.body.InterfaceMethodDefinition;
import choral.ast.body.InterfaceMethodModifier;
import choral.ast.body.InterfaceModifier;
import choral.ast.body.MethodSignature;
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
import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.AnnotationInfoList;
import io.github.classgraph.AnnotationParameterValue;
import io.github.classgraph.AnnotationParameterValueList;
import io.github.classgraph.ArrayTypeSignature;
import io.github.classgraph.BaseTypeSignature;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ClassRefTypeSignature;
import io.github.classgraph.ClassTypeSignature;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.FieldInfoList;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.MethodInfoList;
import io.github.classgraph.MethodParameterInfo;
import io.github.classgraph.MethodTypeSignature;
import io.github.classgraph.ReferenceTypeSignature;
import io.github.classgraph.ScanResult;
import io.github.classgraph.TypeArgument;
import io.github.classgraph.TypeArgument.Wildcard;
import io.github.classgraph.TypeParameter;
import io.github.classgraph.TypeSignature;
import io.github.classgraph.TypeVariableSignature;

class LiftException extends Exception {
    LiftException(String message) {
        super(message);
    }

    static LiftException array() {
        return new LiftException("array");
    }

    static LiftException wildcard() {
        return new LiftException("wildcard");
    }
}

/**
 * ClassLifter is responsible for "lifting" Java class files into Choral's internal AST
 * representation. This allows Choral code to interact with existing Java classes without
 * needing to declare Choral headers for them manually.
 */
public class ClassLifter {

    private static final Position NO_POSITION = new Position(null, 0, 0);
    private static final FormalWorldParameter DEFAULT_WORLD_PARAMETER = new FormalWorldParameter(new Name("A", NO_POSITION), NO_POSITION);
    private static final WorldArgument DEFAULT_WORLD_ARGUMENT = new WorldArgument(new Name("A", NO_POSITION), NO_POSITION);

    private static final Logger logger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

    // private static Set<String> trackedCompilationUnits = new HashSet<>();
    private static Set<String> trackedCompilationUnits = new HashSet<>(List.of("java.lang.Object", "java.io.Serializable"));

    /**
     * Finds a given java package and translates it into a choral CompilationUnit
     * @param fullyQualifiedName The fully qualified name of class to be lifted. 
     * @return
     */
    public static Stream<CompilationUnit> liftPackage(String fullyQualifiedName){
        List<CompilationUnit> compilationUnitAccumulator = new ArrayList<>();
        liftPackageHelper(fullyQualifiedName, compilationUnitAccumulator);
        return compilationUnitAccumulator.stream();
    }

    // Helper method to avoid passing empty mutable list to `liftPackage()` method
    private static void liftPackageHelper(String fullyQualifiedName, List<CompilationUnit> compilationUnitAccumulator){
        int lastSeparator = fullyQualifiedName.lastIndexOf(".");
        String packageName = fullyQualifiedName.substring(0, lastSeparator);
                
        try (ScanResult scanResult = new ClassGraph()
                            //.verbose()
                            .enableAllInfo()
                            .enableInterClassDependencies()
                            .enableExternalClasses()
                            .enableSystemJarsAndModules()
                            .acceptPackages(packageName)
                            .scan())
        {
            ClassInfo classInfo = scanResult.getClassInfo(fullyQualifiedName);

            if (classInfo == null){
                System.err.println("WARNING: Could not find class: " + fullyQualifiedName);
                System.err.println("Package scanned: " + packageName);
                System.err.println("All classes found in scan: " + scanResult.getAllClasses().size());
                throw new RuntimeException("Could not find class: " + fullyQualifiedName);
            }

            if (classInfo.isInnerClass()){
                logger.warn("Inner class detected: " + fullyQualifiedName + ". Choral does not support inner classes, aborting lift");
                return;
            }

            logger.setLevel(Level.ERROR);
            boolean verbose = Boolean.parseBoolean(System.getProperty("liftVerbose"));
            if (verbose) logger.setLevel(Level.WARN);

            trackedCompilationUnits.add(classInfo.getName());
            if (classInfo.isEnum()){
                liftEnum(classInfo, compilationUnitAccumulator);
            } else if (classInfo.isInterface()){
                liftInterface(classInfo, compilationUnitAccumulator);
            } else {
                liftClass(classInfo, compilationUnitAccumulator);
            }
        }
    }

    private static void liftClass(ClassInfo classInfo, List<CompilationUnit> compilationUnitAccumulator){
        // TRANSLATE FIELDS
        FieldInfoList fieldInfoList = classInfo.getFieldInfo();
        List<Field> choralFields = new ArrayList<>();
        for (FieldInfo fieldInfo : fieldInfoList){

            EnumSet<FieldModifier> modifiers = parseModifiers(FieldModifier.class, fieldInfo.getModifiersStr());

            TypeSignature fieldTypeSig = fieldInfo.getTypeSignatureOrTypeDescriptor();
            TypeExpression fieldTypeExpression; 
            try {
                fieldTypeExpression = getTypeExpressions(fieldTypeSig);
            } catch (LiftException e){
                warn(fieldInfo.getName(), e);
                continue;
            }

            Field field = new Field(
                new Name(fieldInfo.getName()),
                fieldTypeExpression,
                Collections.emptyList(), // ignore annotations for now
                modifiers,
                NO_POSITION);
            choralFields.add(field);
        }

        // TRANSLATE METHODS
        MethodInfoList methodInfoList = classInfo.getMethodInfo();
        List<ClassMethodDefinition> methods = new ArrayList<>();
        for (MethodInfo methodInfo : methodInfoList){

            EnumSet<ClassMethodModifier> methodModifiers = parseModifiers(ClassMethodModifier.class, methodInfo.getModifiersStr());
            
            MethodSignature methodSignature; 
            try {
                methodSignature = getMethodSignature(methodInfo);
            } catch (LiftException e){
                warn(methodInfo.getName(), e);
                continue;
            }

            ClassMethodDefinition method = new ClassMethodDefinition(
                methodSignature, 
                new NilStatement(NO_POSITION), // Ignore method body
                Collections.emptyList(), // ignore annotations for now
                methodModifiers, 
                NO_POSITION);
            methods.add(method);
        }

        // TRANSLATE CONSTRUCTORS
        MethodInfoList constructors = classInfo.getConstructorInfo();
        List<ConstructorDefinition> choralConstructors = new ArrayList<>();
        for (MethodInfo constructor : constructors){
            EnumSet<ConstructorModifier> modifiersConstructor = parseModifiers(ConstructorModifier.class, constructor.getModifiersStr());
            
            LiftedSignatureData liftedSignatureData;
            try {
                liftedSignatureData = liftSignatureData(constructor);
            } catch (LiftException e) {
                warn(constructor.getName(), e);
                continue;
            }

            ConstructorSignature constructorSignature = new ConstructorSignature(
                new Name(classInfo.getName(), NO_POSITION),  
                liftedSignatureData.typeParameters(), 
                liftedSignatureData.parameters(), 
                NO_POSITION);

            ConstructorDefinition constructorChoral = new ConstructorDefinition(
                constructorSignature, 
                null, // not supported by ClassGraph 
                // ^Represents calling `this()` or `super()` at the start of a constructor
                new NilStatement(NO_POSITION), // ignore constructor body
                Collections.emptyList(), // ignore annotations for now 
                modifiersConstructor, 
                NO_POSITION);
            
            choralConstructors.add(constructorChoral);
        }

        // TRANSLATE TYPE PARAMETERS
        ClassTypeSignature classTypeSignature = classInfo.getTypeSignatureOrTypeDescriptor();
        List<FormalTypeParameter> choralTypeParameters; 
        try {
            choralTypeParameters = liftTypeParameters(classTypeSignature.getTypeParameters());
        } catch (LiftException e){
            warn(classTypeSignature.toString(), e);
            return;
        }

        // TRANSLATE SUPERINTERFACES
        ClassRefTypeSignature extendedClassTypeSignature = classTypeSignature.getSuperclassSignature();
        List<ClassRefTypeSignature> implementedInterfaceTypeSignatures = classTypeSignature.getSuperinterfaceSignatures();
        List<TypeExpression> parentInterfaces = new ArrayList<>();
        for (ClassRefTypeSignature implementedTypeSignature : implementedInterfaceTypeSignatures){
            TypeExpression interfaceExpression;
            try {
                interfaceExpression = getTypeExpressions(implementedTypeSignature);
            } catch (LiftException e) {
                warn(implementedTypeSignature.getBaseClassName(), e);
                continue;
            } 
            parentInterfaces.add(interfaceExpression);
        }

        // TRANSLATE SUPERCLASS
        TypeExpression extendedExpression = null;
        if (extendedClassTypeSignature != null){
            try {
                extendedExpression = getTypeExpressions(extendedClassTypeSignature);
            } catch (LiftException e) {
                warn(extendedClassTypeSignature.getBaseClassName(), e);
            } 
        }

        EnumSet<ClassModifier> classModifiers = parseModifiers(ClassModifier.class, classInfo.getModifiersStr());

        choral.ast.body.Class choralClass = new Class(
            new Name(classInfo.getSimpleName(), NO_POSITION), 
            List.of(DEFAULT_WORLD_PARAMETER), 
            choralTypeParameters, 
            extendedExpression,
            parentInterfaces,
            choralFields, 
            methods, 
            choralConstructors, 
            Collections.emptyList(), // ignore annotations for now 
            classModifiers, 
            NO_POSITION);

        CompilationUnit compilationUnit = new CompilationUnit(
            Optional.of(classInfo.getPackageName()),
            // No imports, because classfiles use fully qualified names
            Collections.emptyList(),
            Collections.emptyList(),
            List.of(choralClass),
            Collections.emptyList(),
            null);

        compilationUnitAccumulator.add(compilationUnit);  
        
        // recursively visit referenced classfiles
        ClassInfoList dependencies = classInfo.getClassDependencies();
        for (ClassInfo dependency : dependencies){
            if (trackedCompilationUnits.add(dependency.getName())){
                liftPackageHelper(dependency.getName(), compilationUnitAccumulator);
            }
        }

        // recursively visit super class
        if (extendedClassTypeSignature != null){
            if (trackedCompilationUnits.add(extendedClassTypeSignature.getBaseClassName())){ 
                // getBaseClassName returns fully qualified name of class, similarly to getName
                liftPackageHelper(extendedClassTypeSignature.getBaseClassName(), compilationUnitAccumulator);
            }
        }
    }

    private static void liftInterface(ClassInfo interfaceInfo, List<CompilationUnit> compilationUnitAccumulator){
        // TRANSLATE METHODS
        MethodInfoList interfaceMethods = interfaceInfo.getMethodInfo();
        List<InterfaceMethodDefinition> choralInterfaceMethods = new ArrayList<>();
        for (MethodInfo interfaceMethod : interfaceMethods){

            EnumSet<InterfaceMethodModifier> interfaceMethodModifiers = parseModifiers(InterfaceMethodModifier.class, interfaceMethod.getModifiersStr());
            MethodSignature interfaceMethodSignature; 
            try {
                interfaceMethodSignature = getMethodSignature(interfaceMethod);
            } catch (LiftException e) {
                warn(interfaceMethod.getName(), e);
                continue;
            } 

            InterfaceMethodDefinition choralInterfaceMethod = new InterfaceMethodDefinition(
                interfaceMethodSignature, 
                Collections.emptyList(), // ignore annotations for now 
                interfaceMethodModifiers, 
                NO_POSITION);
            choralInterfaceMethods.add(choralInterfaceMethod);
        }

        // find super interfaces
        ClassTypeSignature interfaceTypeSignature = interfaceInfo.getTypeSignatureOrTypeDescriptor();
        List<ClassRefTypeSignature> extendedInterfaceSignatures = interfaceTypeSignature.getSuperinterfaceSignatures();
        
        List<TypeExpression> choralExtendedInterfaces = new ArrayList<>();
        List<String> extendedInterfaceNames = new ArrayList<>();
        for (ClassRefTypeSignature extendedInterfaceSignature : extendedInterfaceSignatures){
            extendedInterfaceNames.add(extendedInterfaceSignature.getBaseClassName());
            TypeExpression interfaceExpression; 
            try {
                interfaceExpression = getTypeExpressions(extendedInterfaceSignature);
            } catch (LiftException e) {
                warn(extendedInterfaceSignature.getBaseClassName(), e);
                continue;
            } 
            choralExtendedInterfaces.add(interfaceExpression);
        }
        
        // TRANSLATE TYPE PARAMETERS
        List<FormalTypeParameter> choralTypeParameters; 
        try {
            choralTypeParameters = liftTypeParameters(interfaceTypeSignature.getTypeParameters());
        } catch (LiftException e){
            warn(interfaceTypeSignature.toString(), e);
            return;
        } 

        EnumSet<InterfaceModifier> interfaceModifiers = parseModifiers(InterfaceModifier.class, interfaceInfo.getModifiersStr());

        Interface choralInterface = new Interface(
            new Name(interfaceInfo.getSimpleName(), NO_POSITION), 
            List.of(DEFAULT_WORLD_PARAMETER),
            choralTypeParameters, 
            choralExtendedInterfaces, 
            choralInterfaceMethods, 
            Collections.emptyList(), // ignore annotations for now 
            interfaceModifiers, 
            NO_POSITION);

        CompilationUnit compilationUnit =  new CompilationUnit(
            Optional.of(interfaceInfo.getPackageName()),
            // No imports, because classfiles use fully qualified names
            Collections.emptyList(),
            List.of(choralInterface),
            Collections.emptyList(),
            Collections.emptyList(),
            null);

        compilationUnitAccumulator.add(compilationUnit);

        // recursively visit referenced classfiles
        ClassInfoList dependencies = interfaceInfo.getClassDependencies();
        for (ClassInfo dependency : dependencies){
            if (trackedCompilationUnits.add(dependency.getName())){
                liftPackageHelper(dependency.getName(), compilationUnitAccumulator);
            }
        }

        // recursively visit super interfaces
        for (String name : extendedInterfaceNames){
            if (trackedCompilationUnits.add(name)){
                liftPackageHelper(name, compilationUnitAccumulator);
            }
        }
    }

    private static void liftEnum(ClassInfo enumInfo, List<CompilationUnit> compilationUnitAccumulator){
        // TRANSLATE CONSTANTS
        FieldInfoList enumConstants = enumInfo.getFieldInfo().filter(FieldInfo::isEnum);
        List<EnumConstant> choralEnumConstants = new ArrayList<>();
        for (FieldInfo constant : enumConstants){
            EnumConstant newConstant = new EnumConstant(
                new Name(constant.getName(), NO_POSITION), 
                Collections.emptyList(), // ignore annotations for now 
                NO_POSITION);
            choralEnumConstants.add(newConstant);
        }

        EnumSet<ClassModifier> enumModifiers = parseModifiers(ClassModifier.class, enumInfo.getModifiersStr());
        // Enum for enum modifiers in choral internals is the same Enum used for class modifiers
        // but enums are not allowed to be abstract 
        enumModifiers.remove(ClassModifier.ABSTRACT);

        choral.ast.body.Enum choralEnum = new choral.ast.body.Enum(
            new Name(enumInfo.getSimpleName(), NO_POSITION), 
            DEFAULT_WORLD_PARAMETER, 
            choralEnumConstants, 
            Collections.emptyList(), // ignore annotations for now 
            enumModifiers, 
            NO_POSITION);

        CompilationUnit compilationUnit = new CompilationUnit(
            Optional.of(enumInfo.getPackageName()),
            // No imports, because classfiles use fully qualified names
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            List.of(choralEnum),
            null);

        compilationUnitAccumulator.add(compilationUnit);
    }

    /**
     * Parses modifiers found by ClassGraph, into modifiers used by choral internals. 
     * @param <E>
     * @param enumClass
     * @param modifiersStr
     * @return 
     */
    private static <E extends Enum<E>> EnumSet<E> parseModifiers(java.lang.Class<E> enumClass, String modifiersStr) {
        EnumSet<E> modifiers = EnumSet.noneOf(enumClass);
        String[] modifierStrings = modifiersStr.split(" ");
        for (String modifierString : modifierStrings) {
            modifiers.add(Enum.valueOf(enumClass, modifierString.toUpperCase()));
        }
        return modifiers;
    }

    private static List<FormalTypeParameter> liftTypeParameters(List<TypeParameter> typeParameters)
        throws LiftException {
        List<FormalTypeParameter> choralTypeParameters = new ArrayList<>();
        for (TypeParameter typeParameter : typeParameters){
            // choral does not support lower bounds, so only upper bounds are found
            List<TypeExpression> upperBounds = new ArrayList<>();
            
            ReferenceTypeSignature classBound = typeParameter.getClassBound();
            if (containsWildcards(classBound)){
                throw LiftException.wildcard();
            }

            TypeExpression classBoundExpression; 
            if (classBound != null){
                classBoundExpression = getTypeExpressions(classBound);
                upperBounds.add(classBoundExpression);
            }

            List<ReferenceTypeSignature> interfaceBounds = typeParameter.getInterfaceBounds();
            for (ReferenceTypeSignature interfaceBound : interfaceBounds) {
                if (containsWildcards(interfaceBound)) {
                    throw LiftException.wildcard();
                } 
                upperBounds.add(getTypeExpressions(interfaceBound));
            }

            FormalTypeParameter choralTypeParameter = new FormalTypeParameter(
                new Name(typeParameter.getName(), NO_POSITION), 
                List.of(DEFAULT_WORLD_PARAMETER), 
                upperBounds, 
                Collections.emptyList(), // ignore annotations for now 
                NO_POSITION);
            choralTypeParameters.add(choralTypeParameter);
        }
        return choralTypeParameters;
    }

    // recursively checks whether a type parameter contains a wild card. 
    // recursion is done to account for nested types
    private static boolean containsWildcards(TypeSignature typeSignature){
        if (typeSignature instanceof ClassRefTypeSignature classRef){
            List<TypeArgument> typeArguments = classRef.getTypeArguments();
            for (TypeArgument typeArgument : typeArguments){
                if (typeArgument.getWildcard() != Wildcard.NONE){
                    return true;
                }
                TypeSignature argumentTypeSignature = typeArgument.getTypeSignature();
                if (argumentTypeSignature != null && containsWildcards(argumentTypeSignature)) {
                    return true;
                }
            }
        }
        return false;
    }   

    /**
     * Generates the choral TypeExpression from the given ClassGraph TypeSignature.
     * Does so recursively if given TypeSignature is nested. 
     * @param typeSig 
     * @return
     */
    private static TypeExpression getTypeExpressions(TypeSignature typeSig) 
    throws LiftException {
        List<TypeExpression> typeExpressions = new ArrayList<>();
        if (typeSig instanceof ClassRefTypeSignature classref) { // for nested types
            String baseClassName = classref.getBaseClassName();
            String simpleName = baseClassName.substring(baseClassName.lastIndexOf('.') + 1);
            
            List<TypeArgument> typeArguments = classref.getTypeArguments();
            if (typeArguments != null){
                for (int i = 0; i < typeArguments.size(); i++){
                    TypeArgument arg = typeArguments.get(i);
                    TypeSignature argType = arg.getTypeSignature();
                    if (argType == null) { 
                        throw LiftException.wildcard();
                    }   
                    TypeExpression typeExpression = getTypeExpressionsHelper(argType);
                    typeExpressions.add(typeExpression);
                }
            }
            return new TypeExpression(
                new Name(simpleName, NO_POSITION), 
                List.of(DEFAULT_WORLD_ARGUMENT), 
                typeExpressions,
                NO_POSITION);
        } else if (typeSig instanceof BaseTypeSignature baseRef) { // for primitive types
            return new TypeExpression(
                new Name(baseRef.getTypeStr(), NO_POSITION),
                // void should not have world arguments 
                baseRef.getTypeStr().equals("void") ? Collections.emptyList() : List.of(DEFAULT_WORLD_ARGUMENT), 
                Collections.emptyList(),
                NO_POSITION);
        } else if (typeSig instanceof TypeVariableSignature typeVar) { // for type parameters
            return new TypeExpression(
                new Name(typeVar.getName(), NO_POSITION), 
                List.of(DEFAULT_WORLD_ARGUMENT), 
                Collections.emptyList(), 
                NO_POSITION);
        } else if (typeSig instanceof ArrayTypeSignature arrTypeVar) { // for array types
            throw LiftException.array();
        } else { 
            throw new UnsupportedOperationException("This type of signature is not yet supported: "
            + typeSig + ". Type of signature: " + typeSig.getClass().getName());
        }
    }
    
    // This helper method exists because inner types of a nested type should not have any world arguments
    private static TypeExpression getTypeExpressionsHelper(TypeSignature typeSig) 
    throws LiftException {
        List<TypeExpression> typeExpressions = new ArrayList<>();
        if (typeSig instanceof ClassRefTypeSignature classref){ // for nested types
            String baseClassName = classref.getBaseClassName();
            List<TypeArgument> typeArguments = classref.getTypeArguments();
            if (typeArguments != null && !typeArguments.isEmpty()){
                for (int i = 0; i < typeArguments.size(); i++){
                    TypeArgument arg = typeArguments.get(i);
                    TypeSignature argType = arg.getTypeSignature();
                    if (argType == null) throw LiftException.wildcard();
                    TypeExpression typeExpression = getTypeExpressionsHelper(argType); 
                    typeExpressions.add(typeExpression);
                }
            }
            return new TypeExpression(
                new Name(baseClassName, NO_POSITION), 
                Collections.emptyList(), 
                typeExpressions,
                NO_POSITION);
        } else if (typeSig instanceof BaseTypeSignature baseRef) { // for primitive types
            return new TypeExpression(
                new Name(baseRef.getTypeStr(), NO_POSITION),
                Collections.emptyList(), 
                typeExpressions,
                NO_POSITION);
        } else if (typeSig instanceof TypeVariableSignature typeVar){ // for type parameters
            return new TypeExpression(
                new Name(typeVar.getName(), NO_POSITION), 
                Collections.emptyList(), 
                Collections.emptyList(), 
                NO_POSITION);
        } else if (typeSig instanceof ArrayTypeSignature){ // for array types
            throw LiftException.array();
        } else { // implement other typesignatures? (might not be necessary)
            throw new UnsupportedOperationException("This type of signature is not yet supported: "
            + typeSig + ". Type of signature: " + typeSig.getClass().getName());
        }
    }

    private static MethodSignature getMethodSignature(MethodInfo methodInfo) throws LiftException{
        MethodTypeSignature methodTypeSignature = methodInfo.getTypeSignatureOrTypeDescriptor();
        TypeExpression returnType = getTypeExpressions(methodTypeSignature.getResultType());

        LiftedSignatureData liftedSignatureData = liftSignatureData(methodInfo);

        return new MethodSignature(
            new Name(methodInfo.getName(), NO_POSITION), 
            liftedSignatureData.typeParameters(), 
            liftedSignatureData.parameters(), 
            returnType, 
            NO_POSITION);
    }

    /**
     * Translates method parameters from ClassGraph to Choral's internal representation.
     * @param methodParams
     * @return
     */
    private static List<FormalMethodParameter> getMethodParameters(MethodParameterInfo[] methodParams) 
    throws LiftException {
        List<FormalMethodParameter> parameters = new ArrayList<>();
        for (MethodParameterInfo param : methodParams){
            TypeExpression type = getTypeExpressions(param.getTypeSignatureOrTypeDescriptor());
            parameters.add(new FormalMethodParameter(
                // param.getName() will very likely return null as most parameters found by ClassGraph are unnamed
                new Name(param.getName(), NO_POSITION),
                type,
                Collections.emptyList(), // ignore annotations for now 
                NO_POSITION));
        }
        return parameters;
    }

    // A record to hold data shared by MethodSignature and ConstructorSignature
    private record LiftedSignatureData(
        List<FormalTypeParameter> typeParameters,
        List<FormalMethodParameter> parameters
    ) {}

    private static LiftedSignatureData liftSignatureData(MethodInfo methodInfo) throws LiftException {
        MethodTypeSignature methodTypeSignature = methodInfo.getTypeSignatureOrTypeDescriptor();
        return new LiftedSignatureData(
            liftTypeParameters(methodTypeSignature.getTypeParameters()),
            getMethodParameters(methodInfo.getParameterInfo())
        );
    }

    private static void warn(String id, LiftException e) {
       logger.warn("Failed to lift {} because {} types are not supported", id, e.getMessage());
    }

    /**
     * Clears the set of tracked compilation units that have already been lifted. 
     * This method should only be called for testing purposes. 
     */
    public static void clearTrackedCompilationUnits(){
        trackedCompilationUnits.clear();;
    }

    @Deprecated // comment or annotation
    private static List<Annotation> translateAnnotations(AnnotationInfoList annotationInfoList){
        List<Annotation> annotations = new ArrayList<>();
        
        for (AnnotationInfo annotationInfo : annotationInfoList){
            String annotationName = annotationInfo.getName();
            AnnotationParameterValueList annotationParameterValueList = annotationInfo.getParameterValues();
            Map<Name, LiteralExpression> annotationValues = new HashMap<>();

            for (AnnotationParameterValue annotationParameterValue : annotationParameterValueList){
                Object annotationValue = annotationParameterValue.getValue();
                if (annotationValue instanceof String s){
                    annotationValues.put(new Name(annotationParameterValue.getName(), NO_POSITION), new StringLiteralExpression(s, DEFAULT_WORLD_ARGUMENT));
                } else if (annotationValue instanceof Integer i) {
                    annotationValues.put(new Name(annotationParameterValue.getName(), NO_POSITION), new IntegerLiteralExpression(i, DEFAULT_WORLD_ARGUMENT));
                } else if (annotationValue instanceof Boolean b){
                    annotationValues.put(new Name(annotationParameterValue.getName(), NO_POSITION), new BooleanLiteralExpression(b, DEFAULT_WORLD_ARGUMENT));
                } else if (annotationValue instanceof Double d){
                    annotationValues.put(new Name(annotationParameterValue.getName(), NO_POSITION), new DoubleLiteralExpression(d, DEFAULT_WORLD_ARGUMENT));
                } else {
                    throw new RuntimeException("ClassGraph found value in annotation parameter list not compatible with choral: " 
                    + annotationValue.toString() + annotationValue.getClass().descriptorString());
                }
            } 
            Annotation annotation = new Annotation(new Name(annotationName), annotationValues);
            annotations.add(annotation);
        }
        return annotations;
    }    
}
