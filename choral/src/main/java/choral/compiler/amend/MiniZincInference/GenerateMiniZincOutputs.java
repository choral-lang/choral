package choral.compiler.amend.MiniZincInference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import choral.Choral;
import choral.ast.CompilationUnit;
import choral.ast.statement.Statement;
import choral.compiler.amend.Utils;
import choral.types.GroundInterface;
import choral.types.Member.HigherCallable;
import choral.utils.Pair;

public class GenerateMiniZincOutputs {

    private final String MZN_FILENAME = "makespan_full.mzn";
    private final String MZN_FULLNAME = "MiniZinc/InferCommunications/" + MZN_FILENAME;
    private final String INPUT_FILENAME = "input.dzn";
    

    Map<HigherCallable, MiniZincInput> inputs;
    Map<MiniZincInput, MiniZincOutput> outputs = new HashMap<>();

    public GenerateMiniZincOutputs( Map<HigherCallable, MiniZincInput> inputs ){
        this.inputs = inputs;
    }

    public Map<MiniZincInput, MiniZincOutput> generateOutputs( CompilationUnit cu ){
        
        callMiniZinc( cu, inputs );

        return outputs;
    }

    private void callMiniZinc( CompilationUnit cu, Map<HigherCallable, MiniZincInput> inputs ){
        
        try {
            createMiniZincFile();
        } catch (IOException e) {
            System.out.println( "An error occured while trying to create the MiniZinc file" );
        }
        
        for( Pair<HigherCallable, Statement> methodPair : Utils.getMethods(cu) ){
            HigherCallable method = methodPair.left();
            MiniZincInput input = inputs.get(method);
            // run minizinc on every method in the CompilationUnit
            try {
                createInputFile(input);
                
                String command = "minizinc " + MZN_FILENAME + " " + INPUT_FILENAME;
                Process p = Runtime.getRuntime().exec( command);
                p.waitFor();
                if( p.exitValue() != 0 ){
                    throw new Exception();
                } else {
                    BufferedReader out = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
                    createOutput(out, methodPair.right(), input, method.channels());
                }
            } catch (Exception e) {
                System.out.println( "An error occured while running the MiniZinc program" );
            }
        }
    }

    private void createMiniZincFile() throws IOException { 
        InputStream minizinc = Choral.class.getClassLoader().getResourceAsStream( MZN_FULLNAME );
        BufferedReader minizincReader = new BufferedReader( new InputStreamReader( minizinc ) );

        File minizincFile = new File( MZN_FILENAME );
        minizincFile.createNewFile();
        FileWriter minizincWriter = new FileWriter( MZN_FILENAME );
        
        for( String line : minizincReader.lines().toList() )
            minizincWriter.write(line + "\n");
        minizincWriter.close();
    }

    private void createInputFile( MiniZincInput input ) throws IOException { 
        File inputFile = new File( INPUT_FILENAME );
        inputFile.createNewFile();
        FileWriter fileWriter = new FileWriter( INPUT_FILENAME );
        fileWriter.write( input.toString() );
        fileWriter.close();
    }

    private void createOutput( 
		BufferedReader out, 
		Statement statement, 
		MiniZincInput input,
		List<Pair<String, GroundInterface>> channels 
	){
        MiniZincOutput output = new MiniZincOutput();
        List<String> outString = out.lines().toList();
        System.out.println( "Full out: \n" );
        for( String s : outString )
            System.out.println( s ); 
		
		// parse data communications
        String dataComs = outString.get(1);
        String[] dataComIdx = dataComs.split(" ");
        for( int dep_idx = 0; dep_idx < input.num_deps; dep_idx++ ){
            Integer com_at_idx = Integer.parseInt(dataComIdx[dep_idx]);
            output.insertDataCom(com_at_idx, input.dependencies.get(dep_idx));
        }

		// parse selections
        List<String> allSelections = outString.subList(3, 3 + input.num_ifs);
        for( int if_ = 0; if_ < input.num_ifs; if_++ ){
            String[] ifSelections = allSelections.get(if_).split("|");
            for( int branch = 0; branch < 2; branch++ ){
				String[] branchSelections = ifSelections[branch].split(" ");
				for( int recipient = 0; recipient < input.roles.size(); recipient++ ){
					if( !branchSelections[recipient].equals("<>") ){
						output.insertSelection(
							input.roles.get(recipient), 
							input.if_roles.get(if_), 
							channels, 
							Integer.valueOf(branchSelections[recipient]) );
					}
				}
            }
        }
		outputs.put(input, output);
    }

	
    
}
