package Typer.WeakerAccess3;

// Case 3: superMethod.isProtected() && declaredMethod.isPackagePrivate()
// Super is protected, override is package-private (not private, and super is not public,
// so only the third branch of the access check fires).
class Base@( A ) {
    protected void foo() {}
}

class WeakerAccess3@( A ) extends Base@( A ) { //! attempting to assign weaker access privileges
    void foo() {}
}

