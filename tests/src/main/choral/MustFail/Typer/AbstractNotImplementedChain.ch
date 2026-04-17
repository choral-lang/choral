package choral.MustFail.Typer.AbstractNotImplementedChain;

// Abstract method inherited through a chain of classes, not implemented in the concrete leaf.
abstract class GrandParent@( A ) {
    public abstract void foo();
}

abstract class Parent@( A ) extends GrandParent@( A ) {
    // Still abstract, foo() not implemented.
}

class AbstractNotImplementedChain@( A ) extends Parent@( A ) { //! must either be declared as abstract or implement abstract method
    // foo() still not implemented.
}
