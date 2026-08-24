package Typer.AbstractSuperclassTrumpsDefault;

// JLS 8.4.8.4: "It is a compile-time error if a class C inherits a default method
// whose signature is override-equivalent with another method inherited by C, unless
// there exists an abstract method declared in a superclass of C and inherited by C
// that is override-equivalent with the two methods."
// Here the abstract superclass method neutralizes the default. The class is abstract,
// so it need not implement the method.
interface HasDefault@( A ) {
    default String@A greet() { return "default"@A; }
}

abstract class AbstractBase@( A ) {
    public abstract String@A greet();
}

abstract class AbstractSuperclassTrumpsDefault@( A ) extends AbstractBase@( A ) implements HasDefault@( A ) {
}
