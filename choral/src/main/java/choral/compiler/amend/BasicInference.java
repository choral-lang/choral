package choral.compiler.amend;

import choral.ast.CompilationUnit;
import choral.ast.body.Class;
import choral.ast.body.ClassMethodDefinition;
import choral.types.GroundDataType;
import choral.types.World;
import choral.ast.expression.Expression;

import java.util.*;
import java.util.Map.Entry;

/**
 * A basic communication inference. Replace a dependency with a communication of that dependency.
 * <p>
 * for example
 * <pre>
 * {@code
 * SymChannel@( A, B )<String> channel;
 *String@B b = "var_b"@B;
 *String@A a = "var_a,"@A + b;
 * }
 * </pre>
 * Would become 
 * <pre>
 * {@code
 * SymChannel@( A, B )<String> channel;
 *String@B b = "var_b"@B;
 *String@A a = "var_a,"@A + ch.<String>com( b );
 * }
 * </pre>
 */
public class BasicInference {
	/*
	 * Iterate through all dependencies
	 * Find a channel that can be used
	 * if not channel, throw error
	 * replace the problematic expression with a communication of that expression
	 */
	public static void inferComms( CompilationUnit cu ){
		for( Class cls : cu.classes() ){
			for( ClassMethodDefinition method : cls.methods() ){
				for( Entry<World, List<Expression>> entryset : method.signature().typeAnnotation().get().worldDependencies().entrySet() ){
					World world = entryset.getKey();
					for( Expression dependency : entryset.getValue() ){
						List<? extends World> fromWorld = ((GroundDataType)dependency.typeAnnotation().get()).worldArguments();
						System.out.println( "Role " + world + " needs " + dependency + " of type " + dependency.typeAnnotation().get() + " from role " + fromWorld );
					}
				}
			}

		}
	}
}
