package Typer.MultiInterfaceInheritance;

// A class implementing multiple interfaces with non-overlapping abstract methods.
interface Fooable@( A ) {
    void foo();
}

interface Barable@( A ) {
    void bar();
}

class MultiInterfaceInheritance@( A ) implements Fooable@( A ), Barable@( A ) {
    public void foo() {}
    public void bar() {}
}
