package choral.MustPass.Typer.PeerOverriding;

// (JLS 8.4.8.1) Peer overriding: the concrete process(String) method in Base
// overrides the abstract process(T=String) method after type substitution,
// so PeerOverriding does not need to declare its own process().
abstract class Base@( A )< T@X > {
    public abstract void process( T@A input );
    public void process( String@A input ) {}
}

public class PeerOverriding@( A ) extends Base@( A )< String > {
}

