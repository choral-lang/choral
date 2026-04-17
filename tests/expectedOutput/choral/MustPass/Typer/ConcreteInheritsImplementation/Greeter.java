package choral.MustPass.Typer.ConcreteInheritsImplementation;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Greeter" )
interface Greeter {
	String greet();
}
