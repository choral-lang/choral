package choral.MustPass.Typer.ReturnTypeSubstitutable;

class Parent@( A ) {
}
class Child@( A ) extends Parent@( A ) {
}
interface Interface@( A ) {
    default Parent@A m() { return new Parent@A(); }
}

class ReturnTypeSubstitutable@A implements Interface@A {
    public Child@A m() { return new Child@A(); }
}
