package choral.MustPass.Typer.DualJavaImport;

import choral.annotations.Choreography;
import java.util.ArrayDeque;
import java.util.Deque;

@Choreography( role = "A", name = "DualJavaImport" )
class DualJavaImport_A {
	public void test() {
		String test = "test";
		Deque < String > d = new ArrayDeque < String >();
		d.addFirst( "a" );
		d.addLast( "b" );
	}

}
