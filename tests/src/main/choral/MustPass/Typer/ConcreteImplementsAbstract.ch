package choral.MustPass.Typer.ConcreteImplementsAbstract;

// A concrete class implements all inherited abstract methods.
abstract class Base@( A ) {
    public abstract void foo();
    public abstract String@A bar();
}

class ConcreteImplementsAbstract@( A ) extends Base@( A ) {
    public void foo() {}
    public String@A bar() { return "hello"@A; }
}
