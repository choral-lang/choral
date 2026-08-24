package choral.compiler.typer.scope;

import choral.types.HigherTypeParameter;

/**
 * Marker interface for scopes that declare generic type parameters.
 */
public interface TypeParameterDeclarationScope
		extends Scope {
	TypeParameterScope getScope( HigherTypeParameter p );
}
