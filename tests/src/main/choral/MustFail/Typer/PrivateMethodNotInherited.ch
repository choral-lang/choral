package Typer.PrivateMethodNotInherited;

// A package-private class extends Base, which has a private method m.
// Private methods are not inherited, so calling m() in the child is an error.
class Base@( A ) {
    private void m() {}
}

class PrivateMethodNotInherited@( A ) extends Base@( A ) {
    void test() {
        m(); //! Cannot resolve method 'm'
    }
}


