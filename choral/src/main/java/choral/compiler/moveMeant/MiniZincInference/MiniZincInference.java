package choral.compiler.moveMeant.MiniZincInference;

import choral.ast.CompilationUnit;
import choral.ast.visitors.PrettyPrinterVisitor;
import choral.types.Member.HigherCallable;
import java.util.Map;

public class MiniZincInference {

  public static CompilationUnit inferComs(CompilationUnit cu) {
    Map<HigherCallable, MiniZincInput> inputs = new GenerateMiniZincInputs().generateInputs(cu);
    Map<MiniZincInput, MiniZincOutput> outputs =
        new GenerateMiniZincOutputs(inputs).generateOutputs(cu);
    CompilationUnit inferredCompilationUnit =
        new InsertMiniZincCommunications(inputs, outputs).insertComs(cu);
    PrettyPrinterVisitor ppv = new PrettyPrinterVisitor();
    // System.out.println( "Inferred CompilationUnit:" );
    // System.out.println( ppv.visit(inferredCompilationUnit) );

    return inferredCompilationUnit;
  }
}
