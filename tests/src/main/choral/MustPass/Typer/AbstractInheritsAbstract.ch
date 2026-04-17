package choral.MustPass.Typer.AbstractInheritsAbstract;

// An abstract class can inherit abstract methods without implementing them.
abstract class Base@( A ) {
    public abstract void foo();
    public abstract String@A bar();
}

abstract class AbstractInheritsAbstract@( A ) extends Base@( A ) {
    // Inherits foo() and bar() without implementing them -- should be fine.
    public abstract void baz();
}
