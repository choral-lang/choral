package choral.compiler.amend;

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
}
