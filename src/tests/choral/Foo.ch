package foo;

class Foo@A implements I@A<Integer> {

/*
    Foo() { this(5@A); }

    Foo(int@A x) { this(x,x); }

    Foo(int@A x, int@A y) { this(); }
*/

}

class Bar@A extends Foo@A implements I@A<String> {

}

interface I@A<T@X> {}