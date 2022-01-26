package choral.examples.Foo;

import choral.annotations.Choreography;
import choral.lang.Unit;

@Choreography( role = "B", name = "Foo" )
public abstract class Foo_B< T > {
	public abstract < S extends T > Unit m( T x );

}
