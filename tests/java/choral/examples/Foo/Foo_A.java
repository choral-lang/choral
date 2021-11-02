package choral.examples.Foo;
import choral.annotations.Choreography;
import choral.lang.Unit;

@Choreography( role = "A", name = "Foo" )
public abstract class Foo_A< T > {
	public abstract < S extends T > T m( Unit x );
	
	public abstract < S extends T > T m();

}
