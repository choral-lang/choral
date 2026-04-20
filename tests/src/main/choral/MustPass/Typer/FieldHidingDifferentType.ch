package choral.MustPass.Typer.FieldHidingDifferentType;

// JLS 8.3: "If the class declares a field with a certain name, then the declaration
// of that field is said to hide any and all accessible declarations of fields with
// the same name in superclasses, and superinterfaces of the class."
// A hiding field need not have the same type as the hidden field.
class Base@( A ) {
    public int@A value;
}

class FieldHidingDifferentType@( A ) extends Base@( A ) {
    public String@A value;
}
