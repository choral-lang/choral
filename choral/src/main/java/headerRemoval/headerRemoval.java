package headerRemoval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import choral.ast.CompilationUnit;
import choral.ast.Name;
import choral.ast.Position;
import choral.ast.body.Annotation;
import choral.ast.body.Class;
import choral.ast.body.ClassMethodDefinition;
import choral.ast.body.ClassMethodModifier;
import choral.ast.body.ClassModifier;
import choral.ast.body.Field;
import choral.ast.body.FieldModifier;
import choral.ast.body.MethodSignature;
import choral.ast.expression.LiteralExpression;
import choral.ast.expression.LiteralExpression.BooleanLiteralExpression;
import choral.ast.expression.LiteralExpression.DoubleLiteralExpression;
import choral.ast.expression.LiteralExpression.IntegerLiteralExpression;
import choral.ast.expression.LiteralExpression.StringLiteralExpression;
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
import io.github.classgraph.ClassRefTypeSignature;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.FieldInfoList;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.MethodInfoList;
import io.github.classgraph.MethodParameterInfo;
import io.github.classgraph.MethodTypeSignature;
import io.github.classgraph.ScanResult;
import io.github.classgraph.TypeArgument;
import io.github.classgraph.TypeSignature;

public class headerRemoval {

    // public static void premain(String args, Instrumentation inst) {
    //     // addTransformer expects a ClassFileTransformer
    //     inst.addTransformer(new ClassFileTransformer() {
    //         @Override
    //         public byte[] transform(
    //             ClassLoader loader,              // The classloader loading this class
    //             String className,                // Name of class being loaded (in internal form: java/lang/String)
    //             Class<?> classBeingRedefined,   // If redefining, the class being redefined (usually null)
    //             ProtectionDomain protectionDomain, // Security domain
    //             byte[] classfileBuffer           // The actual class file bytes
    //         ) {
    //             if (className.startsWith("java/") || className.startsWith("javax/")) {
    //                 System.out.println("Loaded: " + className.replace('/', '.'));
    //             }
    //             // Return null means "don't transform, use original bytes"
    //             // Return modified bytes to instrument/transform the class
    //             return null;
    //         }
    //     });
    // }

    private static final Position NO_POSITION = new Position("NO_POSITION", 0, 0);
    private static final FormalWorldParameter DEFAULT_WORLD_PARAMETER = new FormalWorldParameter(new Name("A", NO_POSITION), NO_POSITION);
    private static final WorldArgument DEFAULT_WORLD_ARGUMENT = new WorldArgument(new Name("A", NO_POSITION), NO_POSITION);

    @Deprecated
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

    private static <E extends Enum<E>> EnumSet<E> parseModifiers(java.lang.Class<E> enumClass, String modifiersStr) {
        EnumSet<E> modifiers = EnumSet.noneOf(enumClass);
        String[] modifierStrings = modifiersStr.split(" ");
        for (String modifierString : modifierStrings) {
            modifiers.add(Enum.valueOf(enumClass, modifierString.toUpperCase()));
        }
        return modifiers;
    }

    /**
     * Generates the TypeExpression from the given TypeSignature.
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
                    typeExpressions.add(getTypeExpressions(argType));
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
                List.of(DEFAULT_WORLD_ARGUMENT), 
                typeExpressions,
                NO_POSITION);
        } else { // implement other typesignatures? (might not be necessary)
            throw new UnsupportedOperationException("This type of signature is not yet supported: "
            + typeSig);
        }
    } 

    private static void getClassGraphPackage(String packageName){
        try (ScanResult scanResult = new ClassGraph()//.verbose()
                            .enableAllInfo()
                            .acceptPackages("headerRemoval")
                            .scan()){
            ClassInfo classInfo = scanResult.getClassInfo(packageName);
            int actualNameStart = classInfo.getName().lastIndexOf(".");
            String actualName = classInfo.getName().substring(actualNameStart + 1);
            String compUnitName = classInfo.getName();

            Name className = new Name(actualName, NO_POSITION);
            FieldInfoList fieldInfoList = classInfo.getFieldInfo();
            List<Field> choralFields = new ArrayList<>();
        
            for (FieldInfo fieldInfo : fieldInfoList){

                EnumSet<FieldModifier> modifiers = parseModifiers(FieldModifier.class, fieldInfo.getModifiersStr());

                TypeSignature typeSig = fieldInfo.getTypeSignatureOrTypeDescriptor();
                TypeExpression typeExpression = getTypeExpressions(typeSig);

                Field field = new Field(
                    new Name(fieldInfo.getName()), 
                    typeExpression, 
                    Collections.emptyList(), // ignore annotations for now 
                    modifiers, 
                    NO_POSITION);
                choralFields.add(field);
            }

            EnumSet<ClassModifier> modifiers = parseModifiers(ClassModifier.class, classInfo.getModifiersStr());

            MethodInfoList methodInfoList = classInfo.getMethodInfo();
            List<ClassMethodDefinition> methods = new ArrayList<>();
            for (MethodInfo methodInfo : methodInfoList){
                
                EnumSet<ClassMethodModifier> methodModifiers = parseModifiers(ClassMethodModifier.class, methodInfo.getModifiersStr());

                MethodParameterInfo[] methodParams = methodInfo.getParameterInfo();
                for (MethodParameterInfo param : methodParams){
                    System.out.println(methodInfo.getName() + ": " + param.toString());
                }

                MethodTypeSignature methodTypeSig = methodInfo.getTypeDescriptor();
                System.out.println(methodTypeSig);

                MethodSignature methodSig = new MethodSignature(
                    new Name(methodInfo.getName(), NO_POSITION), 
                    null, 
                    null, 
                    null,
                    NO_POSITION);

                ClassMethodDefinition method = new ClassMethodDefinition(
                    null, 
                    null, 
                    Collections.emptyList(), // ignore annotations for now
                    methodModifiers, 
                    NO_POSITION);
            }

            choral.ast.body.Class choralClass = new Class(
                className, 
                List.of(DEFAULT_WORLD_PARAMETER), 
                null, 
                null, 
                null, 
                choralFields, 
                null, 
                null, 
                Collections.emptyList(), // ignore annotations for now 
                modifiers, 
                NO_POSITION);

            CompilationUnit compUnit = new CompilationUnit(
                null, 
                null, 
                null, 
                List.of(choralClass), 
                null, 
                compUnitName);
            System.out.println(compUnit.primaryType());
        }
    }

    public static void main(String[] args) {    
        getClassGraphPackage("headerRemoval.HelloWorld");
    }
}
