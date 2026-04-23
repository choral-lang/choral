package Typer.InterfaceInheritsAbstractsIncompatibleReturn;

// JLS 9.4.1.3: When an interface inherits multiple override-equivalent abstract
// methods, "one of the inherited methods must be return-type-substitutable for
// every other inherited method; otherwise, a compile-time error occurs."
interface ReturnsString@( A ) {
    String@A foo();
}

interface ReturnsInt@( A ) {
    int@A foo();
}

interface InterfaceInheritsAbstractsIncompatibleReturn@( A ) extends ReturnsString@( A ), ReturnsInt@( A ) { //! incompatible return type
}
