package Typer.ConcreteInheritsImplementation;

// JLS 8.1.5 / 8.4.3.1: A non-abstract class must implement all abstract methods
// from its superinterfaces. Here, the concrete superclass provides the implementation
// that satisfies the interface's abstract method via inheritance.
interface Greeter@( A ) {
    String@A greet();
}

class Base@( A ) {
    public String@A greet() { return "hello"@A; }
}

class ConcreteInheritsImplementation@( A ) extends Base@( A ) implements Greeter@( A ) {
}
