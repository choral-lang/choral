package lsp.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import choral.options.TyperOptions;
import choral.options.VerbosityOptions;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import choral.ast.CompilationUnit;
import choral.compiler.HeaderLoader;
import choral.compiler.Parser;
import choral.exceptions.ChoralCompoundException;
import choral.exceptions.ChoralException;
import choral.compiler.Typer;
import choral.exceptions.AstPositionedException;

public class DiagnosticsProvider {
    public List<Diagnostic> analyze(String uri, String content){
        List<Diagnostic> diagnostics = new ArrayList<>();

        try { // choral compiler errors are reported through exceptions
              // so try to parse and type program, then catch any exceptions
              // to pass along error messages to language client
            CompilationUnit compUnit = Parser.parseString(content);
            
            List<CompilationUnit> headerUnits = HeaderLoader.loadStandardProfile().toList();

            TyperOptions typerOptions = new TyperOptions( VerbosityOptions.VerbosityLevel.WARNINGS );
            Collection<CompilationUnit> typedUnits =
                    Typer.annotate( Arrays.asList(compUnit), headerUnits, typerOptions );

        } catch (ChoralCompoundException e) {
            for (ChoralException cause : e.getCauses()) {
                if ( cause instanceof AstPositionedException ape ) {
					Diagnostic diagnostic = errorDiagnostic(ape.position(), ape.getMessage());
                    diagnostics.add(diagnostic);
                } else {
                    Diagnostic diagnostic = errorDiagnostic( e.getMessage() );
                    diagnostics.add(diagnostic);
                }
            }
        } catch (AstPositionedException e) {
            Diagnostic diagnostic = errorDiagnostic( e.position(), e.getMessage() );
            diagnostics.add(diagnostic);

        } catch (Exception e){
            Diagnostic diagnostic = errorDiagnostic( "Internal compiler error: " + e.getMessage() );
            diagnostics.add(diagnostic);
        }

        for (Diagnostic d : diagnostics) {
            System.err.println("  - " + d.getMessage() + " at line " + d.getRange().getStart().getLine()
            + " and at column " + d.getRange().getStart().getCharacter());
        }

        return diagnostics;
    }

    private static Diagnostic errorDiagnostic( choral.ast.Position position, String message) {
        Diagnostic diagnostic = errorDiagnostic(message);
        // position.line() -1 to account for diff between 0-indexing and 1-indexing
        Range range = new Range(new Position(position.line() - 1, position.column()),
                                new Position(position.line() - 1, position.column()));
        diagnostic.setRange(range);
        return diagnostic;
    }

    private static Diagnostic errorDiagnostic( String message ) {
        Diagnostic diagnostic = new Diagnostic();
        diagnostic.setSeverity(DiagnosticSeverity.Error);
        diagnostic.setMessage(message);
        diagnostic.setSource("choral-compiler");
        return diagnostic;
    }
}
