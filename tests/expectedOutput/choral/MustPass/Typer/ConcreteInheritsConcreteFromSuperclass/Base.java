package choral.MustPass.Typer.ConcreteInheritsConcreteFromSuperclass;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Base" )
class Base {
	public String greet() {
		return "hello";
	}

}
