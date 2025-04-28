package choral.compiler.amend.MiniZincInference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

import choral.Choral;
import choral.ast.CompilationUnit;
import choral.types.Member.HigherCallable;

public class InsertMiniZincCommunications {

    public static CompilationUnit insertCommunications( 
        CompilationUnit cu, 
        Map<HigherCallable, MiniZincInput> inputs 
    ){
        System.out.println( "-=Insert communications=-" );

       
        try {
            String makespanFilename = "makespan_full.mzn";
            String makespanFullName = "MiniZinc/InferCommunications/" + makespanFilename;
            InputStream makespan = Choral.class.getClassLoader().getResourceAsStream( makespanFullName );
            BufferedReader makespanReader = new BufferedReader( new InputStreamReader( makespan ) );

            File makespanFile = new File( makespanFilename );
            makespanFile.createNewFile();
            FileWriter makespanWriter = new FileWriter(makespanFilename);
            
            for( String line : makespanReader.lines().toList() ){
                makespanWriter.write(line + "\n");
            }
            makespanWriter.close();
            
        } catch (Exception e) {
            System.out.println( e.getLocalizedMessage() );
            e.printStackTrace();
        }
        for( MiniZincInput input : inputs.values() ){
            try {
                String filename = "input.dzn";
                File inputFile = new File( filename );
                inputFile.createNewFile();
                FileWriter fileWriter = new FileWriter(filename);
                fileWriter.write( input.toString() );
                fileWriter.close();
                String command = "minizinc makespan_full.mzn " + filename;
                System.out.println( "command: " + command );
                Process p = Runtime.getRuntime().exec( command);
                p.waitFor();
                if( p.exitValue() != 0 ){
                    BufferedReader out = new BufferedReader( new InputStreamReader( p.getErrorStream() ) );
                    out.lines().forEach( System.out::println );
                } else {
                    BufferedReader out = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
                    out.lines().forEach( System.out::println );
                }
                

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        

        return cu;
    }
    
}
