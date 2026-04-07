package choral.compiler.typer.scope;

import choral.exceptions.StaticVerificationException;

/**
 * Thrown when a name cannot be resolved in any enclosing scope.
 */
public class UnresolvedSymbolException
		extends StaticVerificationException {
	public UnresolvedSymbolException( String symbol ) {
		super( "cannot resolve symbol '" + symbol + "'" );
	}
}
