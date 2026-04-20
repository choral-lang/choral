package choral.MustFail.Typer.OverrideFinal;

class Base@( A ) {
    public final void foo() {}
}

class OverrideFinal@( A ) extends Base@( A ) { //! cannot override final method
    public void foo() {}
}

