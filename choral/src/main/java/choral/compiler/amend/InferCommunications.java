package choral.compiler.amend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import choral.ast.CompilationUnit;

public class InferCommunications {

    public InferCommunications(){}

    public List< CompilationUnit > inferCommunications( 
        Collection< CompilationUnit > cus, 
        Collection< CompilationUnit > headers,
        boolean ignoreOverloads ){

        for( CompilationUnit cu : cus ){
            new MiniZincInference().inferComms(cu);
        }
        
        
        Collection<CompilationUnit> dataComCus = cus.stream().map( cu -> new VariableReplacement( new Selections() ).inferComms(cu) ).toList();
        // Since dataComCu is now without type annotations, we need to re-annotate them again
        RelaxedTyper.annotate( dataComCus, headers, ignoreOverloads );

        List<CompilationUnit> fullComCus = new ArrayList<>();
        for( CompilationUnit dataComCu : dataComCus ){
            Selections selections = new BasicKOCInference().inferKOC( dataComCu );
            CompilationUnit fullComCu = new InsertSelections( selections ).insertSelections( dataComCu );
            fullComCus.add(fullComCu);
        }
        
        return fullComCus;
        
    }
}
