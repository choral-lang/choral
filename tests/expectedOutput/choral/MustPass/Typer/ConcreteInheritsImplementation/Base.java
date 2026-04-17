package choral.MustPass.Typer.ConcreteInheritsImplementation;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Base" )
class Base {
	public String greet() {
		return "hello";
	}

}
