package choral.compiler.typer.scope;

import choral.types.HigherDataType;
import choral.types.HigherReferenceType;
import choral.types.HigherTypeParameter;
import choral.types.Member;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Scope for instance members of a class or interface. Allows you to look up the scope of type parameters
 * and instance methods declared by the class or interface. Enclosed within
 * {@link ClassOrInterfaceStaticScope}, which allows you to look up static members and world parameters.
 */
public final class ClassOrInterfaceInstanceScope
		extends ChildScope
		implements ClassOrInterfaceScope, TypeParameterDeclarationScope {

	private final ClassOrInterfaceStaticScope parent;
	private final Map< HigherTypeParameter, TypeParameterScope > typeParameterScopes = new HashMap<>();
	private final Map< Member.HigherCallable, CallableScope > callableScopes = new HashMap<>();

	public ClassOrInterfaceInstanceScope( ClassOrInterfaceStaticScope parent ) {
		this.parent = parent;
	}

	@Override
	protected ClassOrInterfaceStaticScope parent() {
		return parent;
	}

	@Override
	public Optional< ? extends HigherDataType > lookupDataType( String query ) {
		Optional< ? extends HigherDataType > result = parent.type.typeParameter( query );
		if( result.isEmpty() ) {
			result = super.lookupDataType( query );
		}
		return result;
	}

	@Override
	public Optional< ? extends HigherReferenceType > lookupReferenceType( String query ) {
		Optional< ? extends HigherReferenceType > result = parent.type.typeParameter( query );
		if( result.isEmpty() ) {
			result = super.lookupReferenceType( query );
		}
		return result;
	}

	@Override
	public Optional< ? extends HigherTypeParameter > lookupTypeParameter( String query ) {
		return parent.type.typeParameter( query );
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

	@Override
	public CallableScope getScope( Member.HigherCallable callable ) {
		CallableScope scope = callableScopes.get( callable );
		if( scope == null ) {
			scope = new CallableScope( this, callable );
			callableScopes.put( callable, scope );
		}
		return scope;
	}
}
