package Typer.InheritedDefaultDefaultConflict;

// (JLS 8.4.8.4 Rule B) A class C inherits two override-equivalent default methods.
// This is a compile-time error since there is no abstract method from a superclass
// (as opposed to a superinterface) that is override-equivalent with them.
interface I@( A ) {
    default void greet() {}
}

interface J@( A ) {
    default void greet() {}
}

class InheritedDefaultDefaultConflict@( A ) implements I@( A ), J@( A ) { //! Duplicate default methods
}

