package Typer.StaticHidesStatic;

// JLS 8.4.8.2: "If a class C declares or inherits a static method m, then m is
// said to hide any method m' [...] in the supertypes of C" where m is a
// subsignature of m'. Static hiding static is legal.
class Base@( A ) {
    public static String@A foo() { return "base"@A; }
}

class StaticHidesStatic@( A ) extends Base@( A ) {
    public static String@A foo() { return "sub"@A; }
}
