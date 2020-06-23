package choral.examples.Foo;
import org.choral.annotations.Choreography;
import org.choral.lang.Unit;

@Choreography( role = "B", name = "Foo" )
public abstract class Foo_B< T > {
	public abstract < S extends T > Unit m( T x );

}
