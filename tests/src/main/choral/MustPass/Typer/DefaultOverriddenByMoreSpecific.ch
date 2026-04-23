package Typer.DefaultOverriddenByMoreSpecific;

// JLS 8.4.8.4: When the same default method is inherited via multiple paths, and
// a sub-interface overrides it with a more specific default, the most specific
// version wins. "If [...] one of the inherited methods [...] is NOT overridden by
// another inherited method" then a conflict arises — but here Sub overrides Base,
// so no conflict.
interface Base@( A ) {
    default String@A greet() { return "base"@A; }
}

interface Sub@( A ) extends Base@( A ) {
    default String@A greet() { return "sub"@A; }
}

class DefaultOverriddenByMoreSpecific@( A ) implements Base@( A ), Sub@( A ) {
}
