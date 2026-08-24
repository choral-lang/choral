package choral.compiler.typer.scope;

import choral.ast.body.VariableDeclaration;
import choral.exceptions.StaticVerificationException;
import choral.types.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Scope for the parameters and local variables in a method or constructor body.
 * Its parent is always a {@link CallableScope}, which is the scope for the callable's type parameters.
 */
public final class CallableBodyScope extends ChildScope
		implements VariableDeclarationScope {

	private final CallableScope parent;
	private final Map< String, VariableDeclaration > variables = new HashMap<>();

	public CallableBodyScope( CallableScope parent ) {
		this.parent = parent;
	}

	@Override
	public CallableScope parent() {
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
		return Optional.ofNullable( variables.get( identifier ) );
	}

	@Override
	public Optional< ? extends GroundDataType > lookupVariableOrField( String identifier ) {
		Optional< ? extends GroundDataType > result =
				lookupVariable( identifier ).flatMap( VariableDeclaration::typeAnnotation );
		if( result.isEmpty() ) {
			return parent().callable.declarationContext().field( identifier )
					.map( Member.Field::type );
		} else {
			return result;
		}
	}

	@Override
	public GroundClassOrInterface lookupThis() {
		return parent.callable.declarationContext();
	}

	@Override
	public GroundClass lookupSuper() {
		if( lookupThis() instanceof GroundClass c ) {
			return c.extendedClass().orElseThrow(
					() -> new UnresolvedSymbolException( "super" ) );
		} else {
			throw new UnresolvedSymbolException( "super" );
		}
	}

	public BlockScope newBlockScope() {
		return new BlockScope( this );
	}

}
