package choral.compiler.amend;

import choral.ast.CompilationUnit;

/**
 * Variable replacement for data communications. replace a dependency with a new variable containing 
 * the communicated dependency.
 * <p>
 * For example, the code
 * <pre>
 * {@code
 * SymChannel@( A, B )<Integer> ch;
 *Integer@A a = 0@A;
 *Integer@B b = a + a + 1@B;
 * }
 * </pre>
 * Would become 
 * <pre>
 * {@code
 * SymChannel@( A, B )<Integer> ch;
 *Integer@A a = 0@A;
 *Integer@B aatB = ch.<Integer>com( a );
 *Integer@B b = aatB + aatB + 1@B;
 * }
 * </pre>
 * Besides inferring data communications, this will also insert the provided selections.
 * <p>
 * This expects that there are no dependencies on literals and that the resulting {@code CompilationUnit} 
 * will be typed again, since most or all typeannotations will be lost
 */
public class VariableReplacement{

    /** An object containing all selections to be inserted */
    Selections selections;

    public VariableReplacement( Selections selections ){
		this.selections = selections;
	}

    public CompilationUnit inferComms( CompilationUnit cu ){
		
		return cu; 
	}

}