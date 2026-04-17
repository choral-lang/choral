package choral.MustPass.Typer.CovariantReturn;

// Overriding a method with a covariant return type should be allowed.
class Base@( A ) {
    public Object@A foo() { return "hello"@A; }
}

class CovariantReturn@( A ) extends Base@( A ) {
    public String@A foo() { return "world"@A; }
}
