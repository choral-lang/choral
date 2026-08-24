package Typer.OverrideWithWeakerAccessInherited;

// A class inherits a public method from an interface and tries to override it with package-private.
interface Iface@( A ) {
    void foo();
}

class OverrideWithWeakerAccessInherited@( A ) implements Iface@( A ) { //! attempting to assign weaker access privileges
    void foo() {}
}
