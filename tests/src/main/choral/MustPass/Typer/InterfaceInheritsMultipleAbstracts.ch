package choral.MustPass.Typer.InterfaceInheritsMultipleAbstracts;

// JLS 9.4.1.3: When all inherited override-equivalent methods are abstract, the
// interface inherits all of them with no error. "If [...] none of the inherited
// methods is default," the interface simply inherits all abstract methods.
interface A@( W ) {
    String@W foo();
}

interface B@( W ) {
    String@W foo();
}

interface InterfaceInheritsMultipleAbstracts@( W ) extends A@( W ), B@( W ) {
}
