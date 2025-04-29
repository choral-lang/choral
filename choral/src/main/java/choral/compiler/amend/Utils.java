package choral.compiler.amend;

import java.util.List;
import java.util.stream.Stream;

import choral.ast.CompilationUnit;
import choral.ast.statement.Statement;
import choral.types.Member.HigherCallable;
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
    
}
