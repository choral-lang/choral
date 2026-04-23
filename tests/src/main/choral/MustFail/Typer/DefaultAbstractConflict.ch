package Typer.DefaultAbstractConflict;

// JLS 8.4.8.4: "It is a compile-time error if a class C inherits a default method
// whose signature is override-equivalent with another method inherited by C, unless
// there exists an abstract method declared in a superclass of C and inherited by C
// that is override-equivalent with the two methods."
// Here no such superclass abstract method exists, so this should be an error.
interface WithDefault@( A ) {
    default String@A greet() { return "default"@A; }
}

interface WithAbstract@( A ) {
    String@A greet();
}

class DefaultAbstractConflict@( A ) implements WithDefault@( A ), WithAbstract@( A ) { //! Duplicate default methods inherited
}
