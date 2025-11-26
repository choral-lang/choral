package lsp.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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

        try {
            CompilationUnit compUnit = Parser.parseString(content);
            
            List<CompilationUnit> headerUnits = HeaderLoader.loadStandardProfile().toList();
            System.out.println("Finished parsing");

            Collection<CompilationUnit> typedUnits = Typer.annotate(Arrays.asList(compUnit), headerUnits);

            System.out.println("Finished typing");
        } catch (ChoralCompoundException e) {
            List<choral.ast.Position> positions = e.getPositions();
            int positionCounter = 0;
            
            for (ChoralException cause : e.getCauses()){
                Diagnostic diagnostic = new Diagnostic();
                
                                
                choral.ast.Position position = positions.get(positionCounter);
                Range range = new Range(new Position(position.line(), position.column()), 
                                        new Position(position.line(), position.column()));
                diagnostic.setRange(range);
                diagnostic.setSeverity(DiagnosticSeverity.Error);
                diagnostic.setMessage(cause.getMessage());
                diagnostic.setSource("choral-compiler");

                diagnostics.add(diagnostic);

                positionCounter++;
            } 
        } catch (AstPositionedException e) {
            System.out.println("Ast catch");

            Diagnostic diagnostic = new Diagnostic();

            choral.ast.Position position = e.position();
            Range range = new Range(new Position(position.line(), position.column()), 
                                    new Position(position.line(), position.column()));
            diagnostic.setRange(range);
            diagnostic.setSeverity(DiagnosticSeverity.Error);
            diagnostic.setMessage(e.getMessage());
            diagnostic.setSource("choral-compiler");

            diagnostics.add(diagnostic);            
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Default catch");
        }

        return diagnostics;
    }
}
