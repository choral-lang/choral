package choral.examples.Foo;
import org.choral.lang.Unit;

public abstract class Foo_A< T > {
	public abstract < S extends T > T m( Unit x );
	
	public abstract < S extends T > T m();

}
