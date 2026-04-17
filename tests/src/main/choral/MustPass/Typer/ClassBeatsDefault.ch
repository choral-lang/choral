package choral.MustPass.Typer.ClassBeatsDefault;

// A concrete method in a class should take priority over a default method from an interface.
interface Greeter@( A ) {
    default String@A greet() { return "default"@A; }
}

class ClassBeatsDefault@( A ) implements Greeter@( A ) {
    public String@A greet() { return "class"@A; }
}
