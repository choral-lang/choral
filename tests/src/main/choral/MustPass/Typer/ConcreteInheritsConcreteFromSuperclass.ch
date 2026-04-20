package choral.MustPass.Typer.ConcreteInheritsConcreteFromSuperclass;

// JLS 8.4.8: A class C inherits from its direct superclass all concrete methods m
// (both static and instance) for which m is a member of C's direct superclass,
// m is public/protected/package-access in the same package, and no method declared
// in C has a signature that is a subsignature of m.
class Base@( A ) {
    public String@A greet() { return "hello"@A; }
}

class ConcreteInheritsConcreteFromSuperclass@( A ) extends Base@( A ) {
    public String@A use() { return greet(); }
}
