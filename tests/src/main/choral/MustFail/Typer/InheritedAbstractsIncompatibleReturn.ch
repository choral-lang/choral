package choral.MustFail.Typer.InheritedAbstractsIncompatibleReturn;

// JLS 8.4.8.4: When a class inherits multiple override-equivalent abstract methods,
// "one of the inherited methods must be return-type-substitutable for every other
// inherited method; otherwise, a compile-time error occurs."
interface ReturnsString@( A ) {
    String@A foo();
}

interface ReturnsInt@( A ) {
    int@A foo();
}

abstract class InheritedAbstractsIncompatibleReturn@( A ) implements ReturnsString@( A ), ReturnsInt@( A ) { //! incompatible return type
}
