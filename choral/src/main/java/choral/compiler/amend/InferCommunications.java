package choral.compiler.amend;

import java.util.Collection;
import java.util.List;

import choral.ast.CompilationUnit;

public class InferCommunications {

    public InferCommunications(){}

    public CompilationUnit inferCommunications( 
        CompilationUnit cu, 
        Collection<CompilationUnit> headers ){

        CompilationUnit dataComCu = new VariableReplacement( new Selections() ).inferComms(cu);
        // Since dataComCu is now without type annotations, we need to re-annotate them again
        RelaxedTyper.annotate( List.of(dataComCu), headers, true ); // Since coms are methods overloaded on roles we need to ignore these

        Selections selections = new BasicKOCInference().inferKOC( dataComCu );
        CompilationUnit fullComCu = new InsertSelections( selections ).insertSelections( dataComCu );

        return fullComCu;
        
    }
}
