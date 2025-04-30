package choral.compiler.amend.MiniZincInference;

import java.util.Map;

import choral.ast.CompilationUnit;
import choral.types.Member.HigherCallable;

public class MiniZincInference {

    public static CompilationUnit inferComs( CompilationUnit cu ){
        Map<HigherCallable, MiniZincInput> inputs = new GenerateMiniZincInputs().generateInputs( cu );
        Map<MiniZincInput, MiniZincOutput> outputs = new GenerateMiniZincOutputs( inputs ).generateOutputs( cu );
        CompilationUnit inferredCompilationUnit = new InsertMiniZincCommunications( inputs, outputs ).insertComs( cu );

        return cu;
    }
    
}
