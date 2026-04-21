package choral.MustPass.Typer.PublicMethodAccessibleFromOutside;

// A package-private class C extends Base, which has a public method m.
// Public methods are inherited and accessible from outside C.
class Base@( A ) {
    public void m() {}
}

class C@( A ) extends Base@( A ) {}

class PublicMethodAccessibleFromOutside@( A ) {
    void test( C@( A ) c ) {
        c.m();
    }
}

