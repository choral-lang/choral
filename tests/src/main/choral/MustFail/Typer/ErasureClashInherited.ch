package Typer.ErasureClashInherited;

// (JLS 8.4.8.3) Cross-class erasure clash: ErasureClashInherited declares a generic method
// whose erasure matches the inherited process(String) from Base, but the generic method's
// signature is NOT a subsignature of process(String). This is a compile-time error.
class Base@( A ) {
    public void process( String@A x ) {}
}

class ErasureClashInherited@( A ) extends Base@( A ) { //! same erasure
    public <T@A extends String@A> void process( T@A x ) {}
}

