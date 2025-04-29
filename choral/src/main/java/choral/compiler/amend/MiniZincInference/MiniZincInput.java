package choral.compiler.amend.MiniZincInference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import choral.ast.Name;
import choral.ast.expression.Expression;
import choral.ast.expression.FieldAccessExpression;
import choral.ast.expression.MethodCallExpression;
import choral.ast.expression.ScopedExpression;
import choral.ast.statement.Statement;
import choral.ast.type.TypeExpression;
import choral.ast.type.WorldArgument;
import choral.exceptions.CommunicationInferenceException;
import choral.types.GroundClass;
import choral.types.GroundClassOrInterface;
import choral.types.GroundDataType;
import choral.types.GroundDataTypeOrVoid;
import choral.types.GroundInterface;
import choral.types.GroundReferenceType;
import choral.types.GroundTypeParameter;
import choral.types.Member.HigherMethod;
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
    public List<String> dependencyStrings = new ArrayList<>();
    public List<World> dep_from = new ArrayList<>();
    public List<World> dep_to = new ArrayList<>();
    public List<Integer> dep_def_at = new ArrayList<>();
    public List<Dep_use> dep_used_at = new ArrayList<>();

    public List<Dependency> dependencies = new ArrayList<>();
    public Map<Statement, List<Integer>> statementIndices = new HashMap<>();

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
            .collect(Formatting.joining(",", "{", "};", "{};"));
        inputString += "\n";
        
        inputString += "num_deps = " + num_deps + ";\n";
        inputString += "dependencies = ";
        inputString += dependencyStrings.stream()
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

    public static class Dependency{
		private Expression originalExpression;
		private HigherMethod comMethod;
		private String channelIdentifier;
		private GroundInterface channel;
		private GroundDataType type;
        private World sender;
        private World recipient;

		public Dependency( Expression originalExpression, World sender, World recipient ){
			this.originalExpression = originalExpression;
			GroundDataTypeOrVoid t = originalExpression.typeAnnotation().get();
			if( t.isVoid() )
				throw new CommunicationInferenceException( "Dependency cannot be of type void: " + originalExpression );
			this.type = (GroundDataType)t;
			this.recipient = recipient;
			this.sender = sender;
		}

		public Expression originalExpression(){
			return originalExpression;
		}

		public GroundDataType type(){
			return type;
		}

		public HigherMethod comMethod(){
			return comMethod;
		}

		public String channelIdentifier(){
			return channelIdentifier;
		}

		public GroundInterface channel(){
			return channel;
		}

        public World recipient(){
			return recipient;
		}

		public World sender(){
			return sender;
		}

		/**
		 * Creates the {@code Expression} containing the communiction of the dependency. Note
		 * that the argument of the communication method ({@code visitedExpression}) must 
		 * be visited before creating the comExpression.
		 * <p>
		 * This expression needs
		 * <p>
		 * 1. A name
		 * 		- The name of out communication method (com)
		 * <p>
		 * 2. Arguments 
		 * 		- Our {@code visitedDependency} expression. This is expected to be a 
		 * 		visited version of {@code originalExpression}. Note that this must be 
		 * 		visited before calling {@code createComExpression}. This is because 
		 * 		we use java's {@code Object.equals()} to check if  an expression is a 
		 * 		dependency. If {@code createComExpression} is called before visiting 
		 * 		{@code originalExpression} then dependencies inside
		 * 		{@code originalExpression} (nested dependencies) will not be caught. 
		 * <p>
		 * 3. type argumetns
		 * 		- com methods always need the type of the data they are communicating. 
		 * 		This is stored as a {@code TypeExpression}. These TypeExpressions contain
		 * 		the unqualified name of the type (e.g. not "java.lang.Object", only 
		 * 		"Object") and composite types (types containing other types (like 
		 * 		{@code List})) also have a list of {@code TypeExpression}s representing 
		 * 		its inner types.
		 * @param visitedDependency - Must be visited before calling this method, 
		 * 		otherwise nested dependencies will not be caught.
		 */
		public Expression createComExpression( Expression visitedDependency ){

			TypeExpression typeExpression;
            if( originalExpression.typeAnnotation().get() instanceof GroundTypeParameter ){
				typeExpression = getTypeExpression((GroundTypeParameter)originalExpression.typeAnnotation().get());
            } else{ 
				typeExpression = getTypeExpression((GroundClassOrInterface)originalExpression.typeAnnotation().get());
			}

			final List<Expression> arguments = List.of( visitedDependency );
			final Name name = new Name(comMethod.identifier());
			final List<TypeExpression> typeArguments = List.of( typeExpression );
			
			MethodCallExpression scopedExpression = new MethodCallExpression(name, arguments, typeArguments, visitedDependency.position());
			
			// The comMethod is a method inside a channel, so we need to make the channel its scope
			FieldAccessExpression scope = new FieldAccessExpression(new Name(channelIdentifier), visitedDependency.position());
			
			// Something like channel.< Type >com( visitedDependency )
			return new ScopedExpression(scope, scopedExpression);
		}

		private TypeExpression getTypeExpression( GroundClassOrInterface type ){
			return new TypeExpression(
				new Name(type.typeConstructor().identifier()),
				Collections.emptyList(), 
				type.typeArguments().stream().map( typeArg -> getTypeExpression(typeArg.applyTo(type.worldArguments())) ).toList());
		}

		private TypeExpression getTypeExpression( GroundReferenceType type ){
			if( type instanceof GroundClass ){ // I think this is only not true for primitive types, which cannot be communicated
				GroundClass typeGC = (GroundClass)type;
				return new TypeExpression(
					new Name(typeGC.typeConstructor().identifier()),
					Collections.emptyList(), 
					typeGC.typeArguments().stream().map( typeArg -> getTypeExpression(typeArg.applyTo(type.worldArguments())) ).toList());
			}
			if( type instanceof GroundTypeParameter ){
				GroundTypeParameter typeGTP = (GroundTypeParameter)type;
				return new TypeExpression(
					new Name(typeGTP.typeConstructor().identifier()),
					Collections.emptyList(), 
					Collections.emptyList());
			}
			
			throw new CommunicationInferenceException( "ERROR! Not a GroundClass or GroundTypeParameter. Found " + type.getClass() ); 
		}

        /**
         * returns the type of the dependency's original expression as a TypeExpression
         * @return
         */
        public TypeExpression getType(){
            if( originalExpression.typeAnnotation().get() instanceof GroundTypeParameter ){
				return getType((GroundTypeParameter)originalExpression.typeAnnotation().get());
            } else{ 
				return getType((GroundClassOrInterface)originalExpression.typeAnnotation().get());
			}
        }

        private TypeExpression getType( GroundClassOrInterface type ){
            return new TypeExpression(
				new Name(type.typeConstructor().identifier()),
				List.of( new WorldArgument(new Name(recipient.identifier()), null) ), 
				type.typeArguments().stream().map( typeArg -> getTypeExpression(typeArg.applyTo(type.worldArguments())) ).toList());
		}

		private TypeExpression getType( GroundReferenceType type ){
            if( type instanceof GroundClass ){ // I think this is only not true for primitive types, which cannot be communicated
				GroundClass typeGC = (GroundClass)type;
				return new TypeExpression(
					new Name(typeGC.typeConstructor().identifier()),
					List.of( new WorldArgument(new Name(recipient.identifier()), null) ), 
					typeGC.typeArguments().stream().map( typeArg -> getTypeExpression(typeArg.applyTo(type.worldArguments())) ).toList());
			}
			if( type instanceof GroundTypeParameter ){
				GroundTypeParameter typeGTP = (GroundTypeParameter)type;
				return new TypeExpression(
					new Name(typeGTP.typeConstructor().identifier()),
					List.of( new WorldArgument(new Name(recipient.identifier()), null) ), 
					Collections.emptyList());
			}
			
			throw new CommunicationInferenceException( "ERROR! Not a GroundClass or GroundTypeParameter. Found " + type.getClass() ); 
		}

    /**
	 * Find and set a valid channel and communication method.
	 * <p>
	 * If no viable channel can be found, an exception is thrown.
	 */
	public void setComMethod(List<Pair<String, GroundInterface>> channels){
		
		for( Pair<String, GroundInterface> channelPair : channels ){

			// Data channels might not return the same datatype at the receiver as 
			// the datatype from the sender. Since we only store one type for the 
			// dependency we assume that all types in a channel are the same.
			GroundInterface channel = channelPair.right();
			if( channel.typeArguments().stream().anyMatch( typeArg -> type.typeConstructor().isSubtypeOf( typeArg ) ) ){
				
				Optional<? extends HigherMethod> comMethodOptional = 
					channelPair.right().methods()
						.filter( method ->
							method.identifier().equals("com") && // it is a com method (only checked through name)
							method.innerCallable().signature().parameters().get(0).type().worldArguments().equals(List.of(sender)) && // its parameter's worlds are equal to our dependency's world(s)
							method.innerCallable().returnType() instanceof GroundDataType && // probably redundant check, returntype should not be able to be void
							((GroundDataType)method.innerCallable().returnType()).worldArguments().get(0).equals(recipient) ) // its returntype's world is equal to our dependency recipient
						.findAny();
				
				if( comMethodOptional.isPresent() ){
					channelIdentifier = channelPair.left();
			        this.channel = channel;
                    comMethod = comMethodOptional.get();
					return;
				}
			}
		}
		throw new CommunicationInferenceException( "No viable communication method was found for the dependency " + originalExpression() );
	}

	}
}
