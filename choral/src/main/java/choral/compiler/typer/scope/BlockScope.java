package choral.compiler.typer.scope;

import choral.exceptions.StaticVerificationException;
import choral.types.GroundClass;
import choral.types.GroundDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Scope for a statement block ({@code { ... }}) inside a method or constructor body.
 */
public class BlockScope extends ChildScope
		implements VariableDeclarationScope {

	private final VariableDeclarationScope parent;
	private final Map< String, GroundDataType > variables = new HashMap<>();

	public BlockScope( VariableDeclarationScope parent ) {
		this.parent = parent;
	}

	@Override
	public VariableDeclarationScope parent() {
		return parent;
	}

	@Override
	public Map< String, GroundDataType > variables() {
		return variables;
	}

	@Override
	public void declareVariable( String identifier, GroundDataType type ) {
		if( lookupVariable( identifier ).isEmpty() ) {
			variables.put( identifier, type );
		} else {
			throw new StaticVerificationException( "variable '" + identifier
					+ "' already defined in the scope" );
		}
	}

	@Override
	public Optional< ? extends GroundDataType > lookupVariable( String identifier ) {
		GroundDataType result = variables.get( identifier );
		if( result == null ) {
			return parent().lookupVariable( identifier );
		} else {
			return Optional.of( result );
		}
	}

	@Override
	public Optional< ? extends GroundDataType > lookupVariableOrField( String identifier ) {
		GroundDataType result = variables.get( identifier );
		if( result == null ) {
			return parent().lookupVariableOrField( identifier );
		} else {
			return Optional.of( result );
		}
	}

	@Override
	public GroundClass lookupThis() {
		return parent().lookupThis();
	}

	@Override
	public GroundClass lookupSuper() {
		return parent().lookupSuper();
	}

	public BlockScope newBlockScope() {
		return new BlockScope( this );
	}

}
