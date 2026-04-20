package choral.MustFail.Typer.StaticOverridesInstance;

class Base@( A ) {
    public void foo() {}
}

class StaticOverridesInstance@( A ) extends Base@( A ) { //! cannot override instance method
    public static void foo() {}
}

