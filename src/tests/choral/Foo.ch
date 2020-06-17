package foo;

abstract class C1@A implements I1@A {
    abstract void m(int@A x );
}

class C2@A extends C1@A {
    public void m () {}
    void m(int@A x ) {}
}


interface I1@A {
    void m ();
}