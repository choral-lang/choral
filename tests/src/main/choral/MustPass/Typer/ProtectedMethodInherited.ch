package Typer.ProtectedMethodInherited;

// A package-private class extends Base, which has a protected method m.
// Protected methods are inherited, so calling m() in the child is allowed.
class Base@( A ) {
    protected void m() {}
}

class ProtectedMethodInherited@( A ) extends Base@( A ) {
    void test() {
        m();
    }
}

