package Typer.ConflictingDefaults;

// Two unrelated interfaces with identical default methods -- class must override.
interface Left@( A ) {
    default void foo() {}
}

interface Right@( A ) {
    default void foo() {}
}

class ConflictingDefaults@( A ) implements Left@( A ), Right@( A ) { //! Duplicate default methods inherited
    // Neither Left nor Right is more specific, and foo() is not overridden here.
}
