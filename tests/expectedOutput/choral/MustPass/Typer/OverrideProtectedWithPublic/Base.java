package choral.MustPass.Typer.OverrideProtectedWithPublic;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Base" )
class Base {
	protected String greet() {
		return "base";
	}

}
