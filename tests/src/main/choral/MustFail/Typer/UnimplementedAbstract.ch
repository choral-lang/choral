package choral.MustFail.Typer.UnimplementedAbstract;

// A concrete class that doesn't implement an inherited abstract method.
abstract class Base@( A ) {
    public abstract void foo();
}

class UnimplementedAbstract@( A ) extends Base@( A ) { //! must either be declared as abstract or implement abstract method
    // Missing implementation of foo()
}
