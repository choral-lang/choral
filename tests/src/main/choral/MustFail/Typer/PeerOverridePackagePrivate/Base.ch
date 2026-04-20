package choral.MustFail.Typer.PeerOverridePackagePrivateBase;

// The abstract process(T) is package-private (no modifier).
// The concrete process(String) is public.
public abstract class Base@( A )< T@X > {
    abstract void process( T@A input );
    public void process( String@A input ) {}
}

