package choral.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import choral.ast.CompilationUnit;
import choral.ast.ImportDeclaration;

public class FilterSourceUnits {

    /**
     * Method made to filter source CompilationUnits. 
     * <p>
     * Takes a Collection of CompilationUnits and a symbol and returns the CompilationUnit matching 
     * the symbol as CompilationUnits matching any imports.
     * <p>
     * Say you have the following filesystem:
     * <pre>
     * src
     * ├─ proj1
     * │  └─ Project1.ch
     * ├─ proj2
     * │  └─ Project2.ch
     * └─ utils
     *    └─ Utils.ch
     * </pre>
     * And you want to compile Project1.ch which relies on Utils.ch. This means you would need to set 
     * the source folder to src. The way choral collects files to project is by finding all ".ch" files
     * inside the source folder, and parse and type all of them, then only projecting your desired 
     * file (here, Project1.ch).
     * <p>
     * This means, however, that Project2.ch would also be typed (despite not being imported by 
     * Project1.ch), and if it contains errors it would cause the compiler to throw an error and not
     * project Project1.ch.
     * <p>
     * With FilterSourceUnits we can filter out those CompilationUnits that our main CompilationUnit
     * (the one matching symbol) doesn't need.
     */
    public static Collection< CompilationUnit > filterSourceUnits(
        Collection< CompilationUnit > allSourceUnits,
        String symbol
    ){
        List< CompilationUnit > tempSources = new ArrayList<>();
        for( CompilationUnit sourceUnit : allSourceUnits ){
            if( sourceUnit.primaryType().equals(symbol) ){
                tempSources.add(sourceUnit);
            }
        }
        List< CompilationUnit > sourceUnits = new ArrayList<>();

        while( !tempSources.isEmpty() ){
            CompilationUnit source = tempSources.remove(0);
            for( ImportDeclaration imp : source.imports() ){ // Iterate through all imports
                int lastDot = imp.name().lastIndexOf(".");
                for( CompilationUnit potentialSource : allSourceUnits ){ // Iterate through all source units
                    if( potentialSource.packageDeclaration().isPresent() && potentialSource.packageDeclaration().get().equals( imp.name().substring(0, lastDot) ) ){
                        // if the source unit's package matches the import's package
                        if( imp.isOnDemand() ){ // a "*" import 
                            if( !tempSources.contains( potentialSource ) && !sourceUnits.contains( potentialSource ) )
                                tempSources.add( potentialSource );
                        } else {
                            String impClass = imp.name().substring(lastDot+1); // the class being imported
                            if( potentialSource.primaryType().equals( impClass ) ){
                                if( !tempSources.contains( potentialSource ) && !sourceUnits.contains( potentialSource ) )
                                    tempSources.add( potentialSource );
                            }
                        }
                    }
                    
                }
            }
            sourceUnits.add(source);
        }

        return sourceUnits;
    }
    
}
