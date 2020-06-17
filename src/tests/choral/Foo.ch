package foo;

class Foo@A {

    Foo() { this(5@A); }

    Foo(int@A x) { this(x,x); }

    Foo(int@A x, int@A y) { this(); }

}