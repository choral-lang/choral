package Typer.IncompatibleAbstractMethods;

// JLS 8.1.1.1: An abstract class with two abstract methods that have the same
// signature but incompatible return types is an error, because no concrete subclass
// could implement both. See also JLS 8.4.8.4: "one of the inherited methods must
// be return-type-substitutable for every other inherited method."
interface A@( W ) {
    String@W foo();
}

interface B@( W ) {
    int@W foo();
}

abstract class IncompatibleAbstractMethods@( W ) implements A@( W ), B@( W ) { //! incompatible return type
}
