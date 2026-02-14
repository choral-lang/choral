package choral.compiler.moveMeant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import choral.ast.CompilationUnit;
import choral.compiler.Typer;
import choral.compiler.moveMeant.MiniZincInference.*;
import choral.compiler.TyperOptions;

/**
 * The main entry point for communication inference.
 */
public class MoveMeant {

    /** Indicates how we're inferring communications. Options are "simple" or "minizinc". */
    public static String INFERENCE_MODEL = "simple";

    /**
     * Modifies the source CompilationUnits by inferring communications and selections, and
     * inserting them into method bodies.
     */
    public static List< CompilationUnit > infer(
        Collection< CompilationUnit > sources,
        Collection< CompilationUnit > headers,
        TyperOptions opts
    ) {
        List<CompilationUnit> fullComCus = new ArrayList<>();

        if (INFERENCE_MODEL.equals("simple")) {
            Collection<CompilationUnit> dataComCus = sources.stream().map( cu ->
                    new VariableReplacement( new Selections() ).inferComms(cu)
            ).toList();
            // Since dataComCu is now without type annotations, we need to re-annotate them again
            Typer.annotate( dataComCus, headers, opts.normalMode() );

            for( CompilationUnit dataComCu : dataComCus ){
                Selections selections = new BasicKOCInference().inferKOC( dataComCu );
                CompilationUnit fullComCu = new InsertSelections( selections ).insertSelections( dataComCu );
                fullComCus.add(fullComCu);
            }
        }
        else if (INFERENCE_MODEL.equals("minizinc")) {
            for( CompilationUnit cu : sources ){
                CompilationUnit newCu = MiniZincInference.inferComs(cu);
                fullComCus.add(newCu);
            }
        }
        else {
            throw new IllegalArgumentException("Can't infer communications. Invalid inference model: " + INFERENCE_MODEL);
        }

        return fullComCus;
        
    }
}
