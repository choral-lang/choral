package choral.compiler.typer.scope;

import choral.types.HigherTypeParameter;
import choral.types.World;

import java.util.Optional;

/**
 * The scope of a specific type parameter T@A inside a generic class or method. Provides a method
 * {@link #lookupWorldParameter} for fetching the world parameter A of T@A.
 */
public final class TypeParameterScope
		extends ChildScope {

	private final HigherTypeParameter typeParameter;
	private final TypeParameterDeclarationScope parent;

	public TypeParameterScope(
			TypeParameterDeclarationScope parent, HigherTypeParameter typeParameter
	) {
		this.parent = parent;
		this.typeParameter = typeParameter;
	}

	@Override
	protected TypeParameterDeclarationScope parent() {
		return parent;
	}

	@Override
	public Optional< ? extends World > lookupWorldParameter( String query ) {
		return typeParameter.worldParameter( query );
	}

}
