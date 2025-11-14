package headerRemoval;

import java.lang.module.*;
import java.security.ProtectionDomain;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.*;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import choral.ast.CompilationUnit;
import choral.ast.body.Interface;
import choral.ast.type.FormalTypeParameter;

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

    public static void main(String[] args) {
        ModuleFinder finder = ModuleFinder.ofSystem();
        Set<ModuleReference> foundModules = finder.findAll();
    
        String javaHome = System.getProperty("java.home");
        System.out.println("Java Home: " + javaHome);
    
        FileSystem jrtFS = FileSystems.getFileSystem(URI.create("jrt:/"));

        for (ModuleReference mref : foundModules) {
            String moduleName = mref.descriptor().name();

            if (!moduleName.equals("java.logging")) continue;
            
            System.out.println("=== MODULE: " + moduleName + " ===\n");
            
            try (var reader = mref.open()) {
                reader.list()
                    .filter(name -> name.endsWith(".class"))
                    .filter(name -> !name.equals("module-info.class"))
                    .filter(name -> !name.contains("$"))  // Enable this line to skip inner classes
                    .limit(13)  // limit classes to avoid going through all the thousands of classes that can be found
                    .forEach(classFile -> {
                        String className = classFile
                            .replace('/', '.')
                            .substring(0, classFile.length() - 6);
                        
                        try {
                            Class<?> loadedClass = Class.forName(className);
                            System.out.println("Class: " + className);
                           
                            Path sourceFile = jrtFS.getPath("modules", moduleName, classFile);
                            System.out.println("sourceFile: " + sourceFile);
                            
                            List<Class<?>> enums = Stream.of(loadedClass.getClasses())
                                                    .filter( innerClass -> innerClass.isEnum() )
                                                    .collect(Collectors.toList());

                            // List<Class<?>> classes = Stream.of(loadedClass.getClasses())
                            //                         .filter( innerClass -> !innerClass.isEnum() && !innerClass.isInterface())
                            //                         .collect(Collectors.toList());

                            List<Class<?>> interfaces = Stream.of(loadedClass.getClasses())
                                                    .filter( innerClass -> innerClass.isInterface())
                                                    .collect(Collectors.toList());
                            System.out.println("Interfaces: " + interfaces.size());
                            List<Interface> interfaceObjects;
                            

                            // codeblock below should probably be its own method at this point
                            if (!interfaces.isEmpty()) {
                                Class<?> temp = interfaces.get(0);
                                System.out.println("Name: " + temp.getName());
                                
                                // 'FormalWorldParameter' is empty for now (should it be?)
                                
                                TypeVariable<? extends Class<?>>[] typeParams = temp.getTypeParameters();
                                System.out.println("do we have type params: " + (typeParams.length != 0));
                                List<FormalTypeParameter> formalTypeParams = new ArrayList<>();
                                for (TypeVariable<? extends Class<?>> typeParam : typeParams){
                                    System.out.println("typeParam: " + typeParam.toString());
                                    //Type[] bounds = typeParam.getBounds(); // skipping this, since no idea how to create 'TypeExpression'
                                    Annotation[] annotations = typeParam.getAnnotations(); // would need to be translate to choral.ast.Annotation
                                }
                            }
                            
                            
                            // for (Class<?> intface : interfaces){
                            //     String name = intface.getName();
                            // }
                            
                            String loadedClassPackage = loadedClass.getPackage().getName();
                            System.out.println("loadedClassPackage: " + loadedClassPackage);

                            CompilationUnit compUnit = new CompilationUnit(
                                            Optional.of(loadedClassPackage), 
                                            Collections.emptyList(), // no imports in .class files
                                            null, 
                                            null, 
                                            null, 
                                            sourceFile.toString()
                                            ); 
                            
                            System.out.println();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
