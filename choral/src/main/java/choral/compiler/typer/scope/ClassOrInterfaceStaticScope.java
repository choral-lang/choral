package choral.compiler.typer.scope;

import choral.types.HigherClassOrInterface;
import choral.types.Member;
import choral.types.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Scope for static members of a class or interface. Allows you to look up world (role)
 * parameters (e.g. {@code A, B} in {@code class Foo@(A,B)}) using {@link #lookupWorldParameter},
 * as well as static methods using {@link #getScope(Member.HigherCallable)}.
 */
public final class ClassOrInterfaceStaticScope
		extends ChildScope
		implements ClassOrInterfaceScope {

	final HigherClassOrInterface type;
	private final CompilationUnitScope parent;
	private final ClassOrInterfaceInstanceScope instanceScope =
			new ClassOrInterfaceInstanceScope( this );
	private final Map< Member.HigherCallable, CallableScope > callableScopes = new HashMap<>();

	ClassOrInterfaceStaticScope( CompilationUnitScope parent, HigherClassOrInterface type ) {
		this.parent = parent;
		this.type = type;
	}

	@Override
	protected CompilationUnitScope parent() {
		return parent;
	}

	@Override
	public Optional< ? extends World > lookupWorldParameter( String query ) {
		return type.worldParameter( query );
	}

	public ClassOrInterfaceInstanceScope getInstanceScope() {
		return instanceScope;
	}

	public CallableScope getScope( Member.HigherCallable callable ) {
		CallableScope scope = callableScopes.get( callable );
		if( scope == null ) {
			scope = new CallableScope( this, callable );
			callableScopes.put( callable, scope );
		}
		return scope;
	}

}
