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
     * Takes a Collection of CompilationUnits and a symbol and returns the CompilationUnit 
     * matching the symbol as well as any imported CompilationUnits
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
