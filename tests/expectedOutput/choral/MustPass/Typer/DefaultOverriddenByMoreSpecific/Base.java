package choral.MustPass.Typer.DefaultOverriddenByMoreSpecific;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Base" )
interface Base {
	default String greet() {
		return "base";
	}
}
