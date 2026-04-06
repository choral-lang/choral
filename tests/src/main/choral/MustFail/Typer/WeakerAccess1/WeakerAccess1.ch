package choral.MustFail.Typer.WeakerAccess1;

// Case 1: declaredMethod.isPrivate()
// Super is package-private (not public/protected), override is private.
// Only the first branch of the access check fires.
class Base@( A ) {
    void foo() {}
}

class WeakerAccess1@( A ) extends Base@( A ) { //! attempting to assign weaker access privileges
    private void foo() {}
}

