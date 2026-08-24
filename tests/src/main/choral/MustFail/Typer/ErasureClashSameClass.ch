package Typer.ErasureClashSameClass;

// (JLS 8.4.8.3) Two methods in the same class with different signatures but the same erasure.
// The erasure of <T extends String> process(T) is process(String), clashing with process(String).
class ErasureClashSameClass@( A ) {
    void process( String@A x ) {}
    <T@A extends String@A> void process( T@A x ) {} //! same erasure
}

