package choral.MustPass.Typer.DefaultOverridesDefault;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "I" )
interface I {
	default void greet() {
		
	}
}
