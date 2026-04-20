package choral.MustPass.Typer.ClassBeatsDefault;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Greeter" )
interface Greeter {
	default String greet() {
		return "default";
	}
}
