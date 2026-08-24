package choral.compiler.typer.scope;

import choral.types.*;

import java.util.Optional;

/**
 * A scope that has an enclosing "parent" scope. Lookups that fail in this scope will be delegated to the parent scope.
 */
public abstract class ChildScope extends BaseScope {

	protected abstract Scope parent();

	@Override
	public Optional< ? extends HigherDataType > lookupDataType( String query ) {
		return parent().lookupDataType( query );
	}

	@Override
	public Optional< ? extends HigherReferenceType > lookupReferenceType( String query ) {
		return parent().lookupReferenceType( query );
	}

	@Override
	public Optional< ? extends HigherClassOrInterface > lookupClassOrInterface(
			String query
	) {
		return parent().lookupClassOrInterface( query );
	}

	@Override
	public Optional< ? extends HigherTypeParameter > lookupTypeParameter( String query ) {
		return parent().lookupTypeParameter( query );
	}

	@Override
	public Optional< ? extends World > lookupWorldParameter( String query ) {
		return parent().lookupWorldParameter( query );
	}

}
