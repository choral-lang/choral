package headerRemoval;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

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

    private static void getClassGraphPackage(String packageName, String... className){
        try (ScanResult scanResult = new ClassGraph().verbose().enableAllInfo().acceptPackages(packageName).scan()){
            ClassInfo classInfo;
            if (className.length != 0) {
                classInfo = scanResult.getClassInfo(className[0]);
                if (classInfo == null) System.err.println("Class " + className[0] + " does not exist in package " + packageName);
            }
            else {
                ClassInfoList classInfoList = scanResult.getAllStandardClasses();
                ClassInfoList interfaceInfoList = scanResult.getAllInterfaces();
                ClassInfoList enumInfoList = scanResult.getAllEnums();
                
            }
        }
    }

    public static void main(String[] args) {    
        
    }
}
