package choral.MustFail.Typer.InterfaceInheritsConflictingDefaults;

// JLS 9.4.1.3: "It is a compile-time error if an interface I inherits a default
// method whose signature is override-equivalent with another method inherited by I."
// Here Left and Right are unrelated, so neither overrides the other.
interface Left@( A ) {
    default String@A greet() { return "left"@A; }
}

interface Right@( A ) {
    default String@A greet() { return "right"@A; }
}

interface InterfaceInheritsConflictingDefaults@( A ) extends Left@( A ), Right@( A ) { //! Duplicate default methods inherited
}
