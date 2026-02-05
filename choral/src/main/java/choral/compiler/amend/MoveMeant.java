package choral.compiler.amend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import choral.ast.CompilationUnit;
import choral.compiler.amend.MiniZincInference.*;

/**
 * The main entry point for communication inference.
 */
public class MoveMeant {

    public static List< CompilationUnit > infer(
        Collection< CompilationUnit > cus, 
        Collection< CompilationUnit > headers,
        boolean ignoreOverloads ){

        int inferenceModel = 2;


        List<CompilationUnit> fullComCus = new ArrayList<>();
        switch( inferenceModel ){
            case 2:{ // Variable replacement
                System.out.println( "Variable replacement" );
                Collection<CompilationUnit> dataComCus = cus.stream().map( cu -> new VariableReplacement( new Selections() ).inferComms(cu) ).toList();
                // Since dataComCu is now without type annotations, we need to re-annotate them again
                RelaxedTyper.annotate( dataComCus, headers, ignoreOverloads );

                for( CompilationUnit dataComCu : dataComCus ){
                    Selections selections = new BasicKOCInference().inferKOC( dataComCu );
                    CompilationUnit fullComCu = new InsertSelections( selections ).insertSelections( dataComCu );
                    fullComCus.add(fullComCu);
                }
                break;
            }
            case 3:{ // MiniZinc inference
                System.out.println( "MiniZinc inference" );
                for( CompilationUnit cu : cus ){
                    CompilationUnit newCu = MiniZincInference.inferComs(cu);
                    fullComCus.add(newCu);
                }
                break;
            }
        }
        
        return fullComCus;
        
    }
}
