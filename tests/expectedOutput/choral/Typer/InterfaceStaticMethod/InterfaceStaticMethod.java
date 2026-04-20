package choral.Typer.InterfaceStaticMethod;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "InterfaceStaticMethod" )
public interface InterfaceStaticMethod {
	static String greet( String name ) {
		return "Hello, " + name;
	}
}
