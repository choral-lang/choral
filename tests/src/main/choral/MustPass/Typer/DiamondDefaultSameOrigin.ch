package Typer.DiamondDefaultSameOrigin;

// Diamond inheritance where the same default method is inherited via two paths.
// This should be fine because both paths lead to the same origin.
interface Base@( A ) {
    default void foo() {}
}

interface Left@( A ) extends Base@( A ) {}
interface Right@( A ) extends Base@( A ) {}

class DiamondDefaultSameOrigin@( A ) implements Left@( A ), Right@( A ) {
    // Inherits foo() from Base via both Left and Right -- no conflict.
}
