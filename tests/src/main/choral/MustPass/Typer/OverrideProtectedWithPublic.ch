package Typer.OverrideProtectedWithPublic;

// JLS 8.4.8.3: "The access modifier of an overriding or hiding method must
// provide at least as much access as the overridden or hidden method."
// Widening from protected to public is legal.
class Base@( A ) {
    protected String@A greet() { return "base"@A; }
}

class OverrideProtectedWithPublic@( A ) extends Base@( A ) {
    public String@A greet() { return "sub"@A; }
}
