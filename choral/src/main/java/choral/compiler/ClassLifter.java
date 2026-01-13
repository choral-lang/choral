package choral.compiler;

import java.util.*;

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
import choral.ast.type.FormalWorldParameter;
import choral.ast.type.TypeExpression;
import choral.ast.type.WorldArgument;
import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.AnnotationInfoList;
import io.github.classgraph.AnnotationParameterValue;
import io.github.classgraph.AnnotationParameterValueList;
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
import io.github.classgraph.ScanResult;
import io.github.classgraph.TypeArgument;
import io.github.classgraph.TypeParameter;
import io.github.classgraph.TypeSignature;

/**
 * ClassLifter is responsible for "lifting" Java class files into Choral's internal AST
 * representation. This allows Choral code to interact with existing Java classes without
 * needing to declare Choral headers for them manually.
 */
public class ClassLifter {

    private static final Position NO_POSITION = new Position("NO_POSITION", 0, 0);
    private static final FormalWorldParameter DEFAULT_WORLD_PARAMETER = new FormalWorldParameter(new Name("A", NO_POSITION), NO_POSITION);
    private static final WorldArgument DEFAULT_WORLD_ARGUMENT = new WorldArgument(new Name("A", NO_POSITION), NO_POSITION);

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

    // Inner types of a nested type should not have any world arguments
    private static TypeExpression getTypeExpressionsHelper(TypeSignature typeSig){
        List<TypeExpression> typeExpressions = new ArrayList<>();
        if (typeSig instanceof ClassRefTypeSignature classref){ // for nested types
            String baseClassName = classref.getBaseClassName();
            List<TypeArgument> typeArguments = classref.getTypeArguments();
            if (typeArguments != null && !typeArguments.isEmpty()){
                for (int i = 0; i < typeArguments.size(); i++){
                    TypeArgument arg = typeArguments.get(i);
                    TypeSignature argType = arg.getTypeSignature();
                    typeExpressions.add(getTypeExpressionsHelper(argType));
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
        } else { // implement other typesignatures? (might not be necessary)
            throw new UnsupportedOperationException("This type of signature is not yet supported: "
            + typeSig);
        }
    }

    /**
     * Generates the choral TypeExpression from the given ClassGraph TypeSignature.
     * Does so recursively if given TypeSignature is nested. 
     * @param typeSig 
     * @return
     */
    private static TypeExpression getTypeExpressions(TypeSignature typeSig){
        List<TypeExpression> typeExpressions = new ArrayList<>();
        if (typeSig instanceof ClassRefTypeSignature classref){ // for nested types
            String baseClassName = classref.getBaseClassName();
            List<TypeArgument> typeArguments = classref.getTypeArguments();
            if (typeArguments != null && !typeArguments.isEmpty()){
                for (int i = 0; i < typeArguments.size(); i++){
                    TypeArgument arg = typeArguments.get(i);
                    TypeSignature argType = arg.getTypeSignature();
                    typeExpressions.add(getTypeExpressionsHelper(argType));
                }
            }
            return new TypeExpression(
                new Name(baseClassName, NO_POSITION), 
                List.of(DEFAULT_WORLD_ARGUMENT), 
                typeExpressions,
                NO_POSITION);
        } else if (typeSig instanceof BaseTypeSignature baseRef) { // for primitive types
            return new TypeExpression(
                new Name(baseRef.getTypeStr(), NO_POSITION),
                // choral compiler complains if void has any worldArguments or typeArguments 
                baseRef.getTypeStr().equals("void") ? Collections.emptyList() : List.of(DEFAULT_WORLD_ARGUMENT), 
                typeExpressions,
                NO_POSITION);
        } else { // implement other typesignatures? (might not be necessary)
            throw new UnsupportedOperationException("This type of signature is not yet supported: "
            + typeSig);
        }
    } 

    /**
     * Translates method parameters from ClassGraph to Choral's internal representation.
     * @param methodParams
     * @return
     */
    private static List<FormalMethodParameter> getMethodParameters(MethodParameterInfo[] methodParams){
        List<FormalMethodParameter> parameters = new ArrayList<>();
        for (MethodParameterInfo param : methodParams){
            parameters.add(new FormalMethodParameter(
                // param.getName() will very likely return null as most parameters found by ClassGraph are unnamed
                new Name(param.getName(), NO_POSITION),
                getTypeExpressions(param.getTypeSignatureOrTypeDescriptor()), 
                Collections.emptyList(), // ignore annotations for now 
                NO_POSITION));
        }
        return parameters;
    }

    // private static CompilationUnit liftClass(ClassInfo classInfo){

    // }

    private static CompilationUnit liftInterface(ClassInfo interfaceInfo){
        EnumSet<InterfaceModifier> interfaceModifiers = parseModifiers(InterfaceModifier.class, interfaceInfo.getModifiersStr());
        MethodInfoList interfaceMethods = interfaceInfo.getMethodInfo();
        List<InterfaceMethodDefinition> choralInterfaceMethods = new ArrayList<>();
        for (MethodInfo interfaceMethod : interfaceMethods){

            EnumSet<InterfaceMethodModifier> interfaceMethodModifiers = parseModifiers(InterfaceMethodModifier.class, interfaceMethod.getModifiersStr());

            MethodTypeSignature interfaceMethodTypeSignature = interfaceMethod.getTypeSignatureOrTypeDescriptor();
            TypeExpression returnType = getTypeExpressions(interfaceMethodTypeSignature.getResultType());

            MethodParameterInfo[] methodParams = interfaceMethod.getParameterInfo();
            List<FormalMethodParameter> inferfaceMethodParameters = getMethodParameters(methodParams);

            MethodSignature interfaceMethodSignature = new MethodSignature(
                new Name(interfaceMethod.getName(), NO_POSITION), 
                Collections.emptyList(), // ignore type parameters for now 
                inferfaceMethodParameters, 
                returnType, 
                NO_POSITION);

            InterfaceMethodDefinition choralInterfaceMethod = new InterfaceMethodDefinition(
                interfaceMethodSignature, 
                Collections.emptyList(), // ignore annotations for now 
                interfaceMethodModifiers, 
                NO_POSITION);
            choralInterfaceMethods.add(choralInterfaceMethod);
        }

        // find interfaces the current interface extends
        ClassTypeSignature interfaceTypeSignature = interfaceInfo.getTypeSignatureOrTypeDescriptor();
        List<ClassRefTypeSignature> extendedInterfaceSignatures = interfaceTypeSignature.getSuperinterfaceSignatures();
        extendedInterfaceSignatures.forEach(System.out::println);
        List<TypeExpression> choralExtendedInterfaces = new ArrayList<>();
        for (ClassRefTypeSignature extendedInterfaceSignature : extendedInterfaceSignatures){
            TypeExpression interfaceExpression = getTypeExpressions(extendedInterfaceSignature);
            choralExtendedInterfaces.add(interfaceExpression);
        }

        Interface choralInterface = new Interface(
            new Name(interfaceInfo.getSimpleName(), NO_POSITION), 
            List.of(DEFAULT_WORLD_PARAMETER),
            Collections.emptyList(), // ignore type parameters for now
            choralExtendedInterfaces, // might fail since ClassLifter is not yet recursive
            choralInterfaceMethods, 
            Collections.emptyList(), // ignore annotations for now 
            interfaceModifiers, 
            NO_POSITION);

        return new CompilationUnit(
            Optional.of(interfaceInfo.getPackageName()),
            // No imports, because classfiles use fully qualified names
            Collections.emptyList(),
            // TODO If we're lifting an interface instead of a class, I guess we should fill this in?
            // TODO Make a test case for lifting an interface, ideally something already in the Java standard library
            List.of(choralInterface),
            Collections.emptyList(),
            Collections.emptyList(),
            interfaceInfo.getName());
    }

    private static CompilationUnit liftEnum(ClassInfo enumInfo){
        // TRANSLATE ENUMS
        ClassInfoList javaEnums = enumInfo.getInnerClasses().filter(ClassInfo::isEnum);
        List<choral.ast.body.Enum> choralEnums = new ArrayList<>();
        for (ClassInfo javaEnum : javaEnums){
            EnumSet<ClassModifier> enumModifiers = parseModifiers(ClassModifier.class, javaEnum.getModifiersStr());
            System.out.println("Enum modifiers: ");
            enumModifiers.forEach(System.out::println);

            FieldInfoList enumConstants = javaEnum.getFieldInfo().filter(FieldInfo::isEnum);
            List<EnumConstant> choralEnumConstants = new ArrayList<>();
            for (FieldInfo constant : enumConstants){
                EnumConstant newConstant = new EnumConstant(
                    new Name(constant.getName(), NO_POSITION), 
                    Collections.emptyList(), // ignore annotations for now 
                    NO_POSITION);
                choralEnumConstants.add(newConstant);
            }

            choral.ast.body.Enum choralEnum = new choral.ast.body.Enum(
                new Name(javaEnum.getSimpleName(), NO_POSITION), // maybe change to getName 
                DEFAULT_WORLD_PARAMETER, 
                choralEnumConstants, 
                Collections.emptyList(), // ignore annotations for now 
                enumModifiers, 
                NO_POSITION);
            choralEnums.add(choralEnum);
        }

        // return new CompilationUnit
        return new CompilationUnit(
                Optional.of(enumInfo.getPackageName()),
                // No imports, because classfiles use fully qualified names
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                choralEnums,
                enumInfo.getName());
    }

    /**
     * Finds a given java package and translates it into a choral CompilationUnit
     * @param fullyQualifiedName The fully qualified name of class to be lifted. If lifting an inner class, remember the '$' character. 
     * Example: java.lang.Thread$State
     * @return
     */
    public static CompilationUnit liftPackage(String fullyQualifiedName){
        int lastSeparator = fullyQualifiedName.lastIndexOf(".");
        String packageName2 = fullyQualifiedName.substring(0, lastSeparator);
        try (ScanResult scanResult = new ClassGraph()//.verbose()
                            .enableAllInfo()
                            .enableSystemJarsAndModules()
                            .acceptPackages(packageName2)
                            .scan()){
            ClassInfo classInfo = scanResult.getClassInfo(fullyQualifiedName);

            if (classInfo == null){
                throw new RuntimeException("Could not find class: " + fullyQualifiedName);
            }

            if (classInfo.isEnum()){
                return liftEnum(classInfo);
            } else if (classInfo.isInterface()){
                return liftInterface(classInfo);
            }

            // TRANSLATE FIELDS
            FieldInfoList fieldInfoList = classInfo.getFieldInfo();
            List<Field> choralFields = new ArrayList<>();
            for (FieldInfo fieldInfo : fieldInfoList){

                EnumSet<FieldModifier> modifiers = parseModifiers(FieldModifier.class, fieldInfo.getModifiersStr());

                TypeSignature fieldTypeSig = fieldInfo.getTypeSignatureOrTypeDescriptor();
                TypeExpression fielTypeExpression = getTypeExpressions(fieldTypeSig);

                Field field = new Field(
                    new Name(fieldInfo.getName()), 
                    fielTypeExpression, 
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

                MethodTypeSignature methodTypeSig = methodInfo.getTypeSignatureOrTypeDescriptor();

                if (methodTypeSig.getTypeParameters().size() > 0) continue; // ignore type parameters for now

                MethodParameterInfo[] methodParams = methodInfo.getParameterInfo();
                List<FormalMethodParameter> choralParameters = getMethodParameters(methodParams);

                TypeExpression returnType = getTypeExpressions(methodTypeSig.getResultType());

                MethodSignature methodSig = new MethodSignature(
                    new Name(methodInfo.getName(), NO_POSITION), 
                    Collections.emptyList(), // ignore type parameters for now
                    choralParameters, 
                    returnType,
                    NO_POSITION);

                ClassMethodDefinition method = new ClassMethodDefinition(
                    methodSig, 
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
                
                MethodParameterInfo[] methodParams = constructor.getParameterInfo();
                List<FormalMethodParameter> choralParameters = getMethodParameters(methodParams);
                
                ConstructorSignature constructorSignature = new ConstructorSignature(
                    new Name(classInfo.getName(), NO_POSITION),  
                    Collections.emptyList(), // ignore type parameters for now 
                    choralParameters, 
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

            EnumSet<ClassModifier> classModifiers = parseModifiers(ClassModifier.class, classInfo.getModifiersStr());

            choral.ast.body.Class choralClass = new Class(
                new Name(classInfo.getSimpleName(), NO_POSITION), 
                List.of(DEFAULT_WORLD_PARAMETER), 
                Collections.emptyList(), // ignore type parameters for now 
                null,
                // TODO List of parent interfaces goes here
                List.of(),
                choralFields, 
                methods, 
                choralConstructors, 
                Collections.emptyList(), // ignore annotations for now 
                classModifiers, 
                NO_POSITION);

            CompilationUnit compUnit = new CompilationUnit(
                Optional.of(classInfo.getPackageName()),
                // No imports, because classfiles use fully qualified names
                Collections.emptyList(),
                // TODO If we're lifting an interface instead of a class, I guess we should fill this in?
                // TODO Make a test case for lifting an interface, ideally something already in the Java standard library
                Collections.emptyList(),
                List.of(choralClass),
                Collections.emptyList(),
                classInfo.getName());

            return compUnit;
        }
    }
}
