package choral.MustFail.Typer.WeakerAccess2;

// Case 2: superMethod.isPublic() && !declaredMethod.isPublic()
// Super is public, override is package-private (not private, so case 1 does not fire).
class Base@( A ) {
    public void foo() {}
}

class WeakerAccess2@( A ) extends Base@( A ) { //! attempting to assign weaker access privileges
    void foo() {}
}

