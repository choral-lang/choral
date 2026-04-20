package choral.MustPass.Typer.DefaultOverridesDefault;

interface I@( A ) {
    default void greet() {}
}

interface J@( A ) extends I@( A ) {
    default void greet() {}
}

public class DefaultOverridesDefault@( A ) implements J@( A ) {
}

