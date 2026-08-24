package choral.compiler.typer.scope;

import choral.types.Member;

/**
 * Scope representing the body of a class or interface.
 */
public interface ClassOrInterfaceScope extends Scope {
	CallableScope getScope( Member.HigherCallable callable );
}
