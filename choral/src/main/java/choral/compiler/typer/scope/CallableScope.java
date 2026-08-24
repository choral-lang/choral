package choral.compiler.typer.scope;

import choral.types.HigherDataType;
import choral.types.HigherReferenceType;
import choral.types.HigherTypeParameter;
import choral.types.Member;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Scope for the type parameters declared by a method or constructor.
 * This scope encloses {@link CallableBodyScope}, which is the scope for formal parameters and local variables.
 */
public final class CallableScope
		extends ChildScope
		implements TypeParameterDeclarationScope {

	final Member.HigherCallable callable;
	private final ClassOrInterfaceScope parent;
	private final Map< HigherTypeParameter, TypeParameterScope > typeParameterScopes = new HashMap<>();
	private CallableBodyScope bodyScope;

	public CallableScope( ClassOrInterfaceScope parent, Member.HigherCallable callable ) {
		this.parent = parent;
		this.callable = callable;
	}

	@Override
	protected ClassOrInterfaceScope parent() {
		return parent;
	}

	@Override
	public Optional< ? extends HigherDataType > lookupDataType( String query ) {
		Optional< ? extends HigherDataType > result = callable.typeParameter( query );
		if( result.isEmpty() ) {
			return super.lookupDataType( query );
		}
		return result;
	}

	@Override
	public Optional< ? extends HigherReferenceType > lookupReferenceType( String query ) {
		Optional< ? extends HigherReferenceType > result = callable.typeParameter( query );
		if( result.isEmpty() ) {
			return super.lookupReferenceType( query );
		}
		return result;
	}

	@Override
	public Optional< ? extends HigherTypeParameter > lookupTypeParameter( String query ) {
		Optional< ? extends HigherTypeParameter > result = callable.typeParameter( query );
		if( result.isEmpty() && !callable.isStatic() ) {
			return super.lookupTypeParameter( query );
		}
		return result;
	}

	@Override
	public TypeParameterScope getScope( HigherTypeParameter parameter ) {
		TypeParameterScope scope = typeParameterScopes.get( parameter );
		if( scope == null ) {
			scope = new TypeParameterScope( this, parameter );
			typeParameterScopes.put( parameter, scope );
		}
		return scope;
	}

	public CallableBodyScope getScope() {
		if( bodyScope == null ) {
			bodyScope = new CallableBodyScope( this );
		}
		return bodyScope;
	}
}
