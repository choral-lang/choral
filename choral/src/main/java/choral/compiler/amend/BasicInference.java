package choral.compiler.amend;

import choral.ast.CompilationUnit;
import choral.ast.body.Class;
import choral.ast.body.ClassMethodDefinition;
import choral.types.GroundDataType;
import choral.types.GroundInterface;
import choral.types.HigherInterface;
import choral.types.HigherTypeParameter;
import choral.types.World;
import choral.types.Member.HigherMethod;
import choral.types.HigherClassOrInterface;
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
				HigherMethod higherMethod = method.signature().typeAnnotation().get();
				for( Entry<World, List<Expression>> entryset : higherMethod.worldDependencies().entrySet() ){
					World world = entryset.getKey();
					for( Expression dependency : entryset.getValue() ){
						List<? extends World> fromWorld = ((GroundDataType)dependency.typeAnnotation().get()).worldArguments();
						System.out.println( "Role " + world + " needs " + dependency + " of type " + dependency.typeAnnotation().get() + " from role " + fromWorld );
						for( GroundInterface channel : higherMethod.channels() ){
							System.out.println( "Potential channel: " + channel + "\n\tmethods: " + 
								channel.methods().filter( innerMethod -> 
									innerMethod.identifier().equals("com") && // it is a com method (only checked through name)
									innerMethod.innerCallable().signature().parameters().get(0).type().worldArguments().equals(fromWorld) && // its parameter's worlds are equal to our dependency's world(s)
									innerMethod.innerCallable().returnType() instanceof GroundDataType &&
									((GroundDataType)innerMethod.innerCallable().returnType()).worldArguments().get(0).equals(world) ) // its return type's world is equal to our dependency recipient
									.findAny() );
						}
					}
				}
			}

		}
	}
}
