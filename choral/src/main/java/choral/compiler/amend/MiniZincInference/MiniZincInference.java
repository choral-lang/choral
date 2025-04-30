package choral.compiler.amend.MiniZincInference;

import static choral.ast.body.ConstructorModifier.valueOf;

import java.util.Map;

import choral.ast.CompilationUnit;
import choral.ast.visitors.PrettyPrinterVisitor;
import choral.types.Member.HigherCallable;

public class MiniZincInference {

    public static CompilationUnit inferComs( CompilationUnit cu ){
        Map<HigherCallable, MiniZincInput> inputs = new GenerateMiniZincInputs().generateInputs( cu );
        Map<MiniZincInput, MiniZincOutput> outputs = new GenerateMiniZincOutputs( inputs ).generateOutputs( cu );
        CompilationUnit inferredCompilationUnit = new InsertMiniZincCommunications( inputs, outputs ).insertComs( cu );
        PrettyPrinterVisitor ppv = new PrettyPrinterVisitor();
        System.out.println( "Inferred CompilationUnit:" );
        System.out.println( ppv.visit(inferredCompilationUnit) );

        return inferredCompilationUnit;
    }
    
}
