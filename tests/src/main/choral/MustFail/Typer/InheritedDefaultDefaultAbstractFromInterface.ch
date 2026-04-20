package choral.MustFail.Typer.InheritedDefaultDefaultAbstractFromInterface;

// (JLS 8.4.8.4 Rule B) A class C inherits two override-equivalent default methods.
// There IS an abstract method OE with them (from K), but it is declared in an *interface*,
// not a superclass, so the exception does not apply and this is still a compile-time error.
interface I@( A ) {
    default void greet() {}
}

interface J@( A ) {
    default void greet() {}
}

interface K@( A ) {
    void greet();
}

class InheritedDefaultDefaultAbstractFromInterface@( A ) implements I@( A ), J@( A ), K@( A ) { //! Duplicate default methods
}

