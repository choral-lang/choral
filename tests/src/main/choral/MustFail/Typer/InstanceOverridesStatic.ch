package Typer.InstanceOverridesStatic;

class Base@( A ) {
    public static void foo() {}
}

class InstanceOverridesStatic@( A ) extends Base@( A ) { //! cannot override static method
    public void foo() {}
}

