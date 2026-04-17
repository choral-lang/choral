package choral.MustPass.Typer.FieldInheritance;

// Fields from a superclass should be inherited by the subclass.
class Base@( A ) {
    public String@A name;
}

class FieldInheritance@( A ) extends Base@( A ) {
    // 'name' should be accessible as an inherited field.
    public void test() {
        String@A x = this.name;
    }
}
