package choral.MustPass.Typer.PrivateDeclaredPublic;

// Base has a private method m (not inherited by C).
// C declares its own public method m with the same signature.
// Accessing m from outside C is allowed because C's own public m is visible.
class Base@( A ) {
    private void m() {}
}

class C@( A ) extends Base@( A ) {
    public void m() {}
}

class PrivateDeclaredPublic@( A ) {
    void test( C@( A ) c ) {
        c.m();
    }
}

