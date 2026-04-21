package choral.MustPass.Typer.OverrideInSubclass;

// (JLS 8.4.8.1) The method "eat" inherited from Animal overrides
// the abstract method "CanEat.eat", so OverrideInSubclass compiles
// without declaring its own eat().
interface CanEat@( A ) {
    void eat();
}

class Animal@( A ) {
    public void eat() {}
}

public class OverrideInSubclass@( A ) extends Animal@( A ) implements CanEat@( A ) {
}

