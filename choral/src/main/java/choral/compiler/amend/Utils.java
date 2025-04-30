package choral.compiler.amend;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import choral.ast.CompilationUnit;
import choral.ast.statement.Statement;
import choral.types.GroundDataType;
import choral.types.GroundInterface;
import choral.types.Member.HigherCallable;
import choral.types.Member.HigherMethod;
import choral.types.World;
import choral.utils.Pair;

public class Utils {

    /**
	 * Retreives all methods from the {@code CompilationUnit} including constructors.
     * <p>
     * Returns a List of Pair of the methods typeannotation and the first statement in its body.
	 */
    public static List<Pair<HigherCallable, Statement>> getMethods( CompilationUnit cu ){
		return Stream.concat( 
			cu.classes().stream()
				.flatMap( cls -> cls.methods().stream() )
				.map( method -> 
					new Pair<HigherCallable, Statement>(
						method.signature().typeAnnotation().get(), // we assume that methods are type-annotated
						method.body().orElse(null)) ), 
			cu.classes().stream()
				.flatMap(cls -> cls.constructors().stream()
				.map( method -> 
					new Pair<HigherCallable, Statement>(
						method.signature().typeAnnotation().get(), 
						method.blockStatements()) )
				)).toList();
	}

    /**
	 * Retreives all methods from the {@code CompilationUnit} including constructors.
     * <p>
     * Returns a list of the methods' typeannotations.
	 */
    public static List<HigherCallable> getJustMethods( CompilationUnit cu ){
		return Stream.concat( 
			cu.classes().stream()
				.flatMap( cls -> cls.methods().stream() )
				.map( method -> method.signature().typeAnnotation().get() ), // we assume that methods are type-annotated
			cu.classes().stream()
				.flatMap(cls -> cls.constructors().stream()
				.map( method -> method.signature().typeAnnotation().get() )
				)).toList();
	}

	/**
	 * Searches through the methods of {@code channels} and returns the first viable com 
	 * method based on the input. 
	 * <p>
	 * A viable method means:
	 * <ul>
	 * <li>{@code dependencyType} is a subtype of the channel's type</li>
	 * <li>The method identifer is "com"</li>
	 * <li>It takes a parameter at world {@code sender}</li>
	 * <li>It returns something at world {@code recipient}</li>
	 * </ul>
	 * Returns null if no such method is found.
	 */
	public static Pair<Pair<String, GroundInterface>, HigherMethod> findComMethod(
		World recipient, 
		World sender, 
		GroundDataType dependencyType, 
		List<Pair<String, GroundInterface>> channels){
		
		for( Pair<String, GroundInterface> channelPair : channels ){

			// Data channels might not return the same datatype at the receiver as 
			// the datatype from the sender. Since we only store one type for the 
			// dependency we assume that all types in a channel are the same.
			GroundInterface channel = channelPair.right();
			if( channel.typeArguments().stream().anyMatch( typeArg -> dependencyType.typeConstructor().isSubtypeOf( typeArg ) ) ){
				
				Optional<? extends HigherMethod> comMethodOptional = 
					channelPair.right().methods()
						.filter( method ->
							method.identifier().equals("com") && // it is a com method (only checked through name)
							method.innerCallable().signature().parameters().get(0).type().worldArguments().equals(List.of(sender)) && // its parameter's worlds are equal to our dependency's world(s)
							method.innerCallable().returnType() instanceof GroundDataType && // probably redundant check, returntype should not be able to be void
							((GroundDataType)method.innerCallable().returnType()).worldArguments().get(0).equals(recipient) ) // its returntype's world is equal to our dependency recipient
						.findAny();
				
				if( comMethodOptional.isPresent() ){
					return new Pair<>( channelPair, comMethodOptional.get());
				}
			}
		}
		return null;
	}
	
	/**
	 * Searches through the methods of {@code channels} and returns the first viable selection 
	 * method based on the input. 
	 * <p>
	 * A viable method means:
	 * <ul>
	 * <li>{@code dependencyType} is a subtype of the channel's type</li>
	 * <li>The method identifer is "select"</li>
	 * <li>It takes a parameter at world {@code sender}</li>
	 * <li>It returns something at world {@code recipient}</li>
	 * </ul>
	 * Returns null if no such method is found.
	 */
	public static SelectionMethod findSelectionMethod(
		World recipient, 
		World sender, 
		List<Pair<String, GroundInterface>> channels){

		for( Pair<String, GroundInterface> channelPair : channels ){
            
			Optional<? extends HigherMethod> selectMethodOptional = 
				channelPair.right().methods()
					.filter( method ->
						method.identifier().equals("select") && // it is a selection method (only checked through name)
						method.innerCallable().signature().parameters().get(0).type().worldArguments().equals(List.of(sender)) && // its parameter's worlds are equal to our sender
						method.innerCallable().returnType() instanceof GroundDataType && // probably redundant check, returntype should not be able to be void
						((GroundDataType)method.innerCallable().returnType()).worldArguments().get(0).equals(recipient) ) // its returntype's world is equal to our recipient
					.findAny();
		
			if( selectMethodOptional.isPresent() ){
				return new SelectionMethod(
					channelPair.left(),
					channelPair.right(),
					selectMethodOptional.get(),
					sender
				);
			}
		}
		// no viable selectionmethod was found
		return null;
	}
}
