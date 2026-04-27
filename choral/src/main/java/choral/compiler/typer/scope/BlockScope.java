package choral.compiler.typer.scope;

import choral.ast.body.VariableDeclaration;
import choral.exceptions.StaticVerificationException;
import choral.types.GroundClass;
import choral.types.GroundClassOrInterface;
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
	private final Map< String, VariableDeclaration > variables = new HashMap<>();

	public BlockScope( VariableDeclarationScope parent ) {
		this.parent = parent;
	}

	@Override
	public VariableDeclarationScope parent() {
		return parent;
	}

	@Override
	public Map< String, VariableDeclaration > variables() {
		return variables;
	}

	@Override
	public void declareVariable( VariableDeclaration declaration ) {
		String identifier = declaration.name().identifier();
		if( lookupVariable( identifier ).isEmpty() ) {
			variables.put( identifier, declaration );
		} else {
			throw new StaticVerificationException( "variable '" + identifier
					+ "' already defined in the scope" );
		}
	}

	@Override
	public Optional< VariableDeclaration > lookupVariable( String identifier ) {
		VariableDeclaration result = variables.get( identifier );
		if( result == null ) {
			return parent().lookupVariable( identifier );
		} else {
			return Optional.of( result );
		}
	}

	@Override
	public Optional< ? extends GroundDataType > lookupVariableOrField( String identifier ) {
		Optional< ? extends GroundDataType > result =
				lookupVariable( identifier ).flatMap( VariableDeclaration::typeAnnotation );
		if( result.isEmpty() ) {
			return parent().lookupVariableOrField( identifier );
		} else {
			return result;
		}
	}

	@Override
	public GroundClassOrInterface lookupThis() {
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
