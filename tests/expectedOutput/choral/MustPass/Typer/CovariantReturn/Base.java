package choral.MustPass.Typer.CovariantReturn;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Base" )
class Base {
	public Object foo() {
		return "hello";
	}

}
