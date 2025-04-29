package choral.compiler.amend.MiniZincInference;

import java.util.ArrayList;
import java.util.List;

import choral.types.World;
import choral.utils.Formatting;
import choral.utils.Pair;

/**
 * An object to store all data needed to create an input for the MiniZinc model. 
 * <p>
 * Generate the input as a string using {@code toString()}
 */
public class MiniZincInput {
    public int in_size = 0;
    public List<String> statements = new ArrayList<>();
    public List<World> statements_roles = new ArrayList<>();
    public List<Integer> statements_blocks = new ArrayList<>();

    public int num_blocks = 0;
    public List<Block> blocks = new ArrayList<>();
    
    public List<World> roles = new ArrayList<>();

    public int num_ifs = 0;
    public List<Pair<Integer,Integer>> if_blocks = new ArrayList<>();
    public List<World> if_roles = new ArrayList<>();

    public int num_deps = 0;
    public List<String> dependencies = new ArrayList<>();
    public List<World> dep_from = new ArrayList<>();
    public List<World> dep_to = new ArrayList<>();
    public List<Integer> dep_def_at = new ArrayList<>();
    public List<Dep_use> dep_used_at = new ArrayList<>();

    public String toString(){
        String inputString = "";

        inputString += "in_size = " + in_size + ";\n";
        inputString += "statements = ";
        inputString += statements.stream()
            .map( stm -> "\"" + stm + "\"" )
            .collect(Formatting.joining(",\n", "[\n", "];", "[];"));
        inputString += "\n";
        inputString += "statements_roles = ";
        inputString += statements_roles.stream()
            .map( r -> r == null ? "NULL" : "r(" + r.identifier() + ")" )
            .collect(Formatting.joining(",", "[", "];", "[];"));
        inputString += "\n";

        inputString += "num_blocks = " + num_blocks + ";\n";
        inputString += "blocks = ";
        inputString += blocks.stream()
            .map( block -> (block.start) + "," + (block.end) + "," + (block.parent) )
            .collect(Formatting.joining("|\n", "[|\n", "|];", "[];"));
        inputString += "\n";

        inputString += "statements_blocks = ";
        inputString += statements_blocks.stream()
            .map( i -> i.toString() )
            .collect(Formatting.joining(",", "[", "];", "[];"));
        inputString += "\n";

        inputString += "num_ifs = " + num_ifs + ";\n";
        inputString += "if_blocks = ";
        inputString += if_blocks.stream()
            .map( blocks -> blocks.left() + "," + blocks.right() )
            .collect(Formatting.joining("|\n", "[|\n", "|];", "[];"));
        inputString += "\n";
        inputString += "if_roles = ";
        inputString += if_roles.stream()
            .map( role -> role.identifier() )
            .collect(Formatting.joining(",", "[", "];", "[];"));
        inputString += "\n";

        inputString += "roles = ";
        inputString += roles.stream()
            .map( role -> role.identifier() )
            .collect(Formatting.joining(",", "[", "];", "[];"));
        inputString += "\n";
        
        inputString += "num_deps = " + num_deps + ";\n";
        inputString += "dependencies = ";
        inputString += dependencies.stream()
            .map( dep -> "\"" + dep + "\"" )
            .collect(Formatting.joining(",", "array1d( DEPS, [", "]);", "array1d( DEPS, []);"));
        inputString += "\n";
        inputString += "dep_from = ";
        inputString += dep_from.stream()
            .map( role -> role.identifier() )
            .collect(Formatting.joining(",", "array1d( DEPS, [", "]);", "array1d( DEPS, []);"));
        inputString += "\n";
        inputString += "dep_to = ";
        inputString += dep_to.stream()
            .map( role -> role.identifier() )
            .collect(Formatting.joining(",", "array1d( DEPS, [", "]);", "array1d( DEPS, []);"));
        inputString += "\n";
        inputString += "dep_def_at = ";
        inputString += dep_def_at.stream()
            .map( i -> i.toString() )
            .collect(Formatting.joining(",", "array1d( DEPS, [", "]);", "array1d( DEPS, []);"));
        inputString += "\n";
        inputString += "dep_used_at = ";
        inputString += dep_used_at.stream()
            .map( dep_use -> "DEPS[" + dep_use.dependency + "], " + 
                (dep_use.nested_dependency ? 
                    "DEPS[" + (dep_use.used_at) + "]":
                    (dep_use.used_at)) )
            .collect(Formatting.joining("|\n", "[|\n", "|];", "[];"));
        inputString += "\n";
        

        return inputString;
    }


    public static class Block{
        int start;
        int end;
        int parent;
        
        public Block( int start, int end, int parent ){
            this.start = start;
            this.end = end;
            this.parent = parent;
        }
        
    }

    public static class Dep_use{
        int dependency;
        int used_at;
        boolean nested_dependency = false;
        
        public Dep_use( int dep ){
            this.dependency = dep;
        }
        
    }
}
