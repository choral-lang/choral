package Typer.InheritedDefaultDefaultWithAbstractSuperclass;

// (JLS 8.4.8.4 Rule B exception) Two override-equivalent default methods are inherited,
// but an abstract method from a *superclass* (AbstractBase) that is OE with them is also
// inherited. Per JLS 8.4.8.4, this suppresses the conflict error. C must be abstract.
interface I@( A ) {
    default void greet() {}
}

interface J@( A ) {
    default void greet() {}
}

abstract class AbstractBase@( A ) {
    public abstract void greet();
}

public abstract class InheritedDefaultDefaultWithAbstractSuperclass@( A )
        extends AbstractBase@( A ) implements I@( A ), J@( A ) {
}

