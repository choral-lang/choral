package Typer.IncompatibleReturnType;

abstract class Base@( A ) {
    public abstract String@A foo();
}

abstract class IncompatibleReturnType@( A ) extends Base@( A ) { //! attempting to use incompatible return type
    public abstract Integer@A foo();
}

