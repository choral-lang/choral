package Typer.DefaultOverriddenByMoreSpecific;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Sub" )
interface Sub extends Base {
	default String greet() {
		return "sub";
	}
}
