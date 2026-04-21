package choral.MustPass.Typer.DefaultOverridesDefault;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "J" )
interface J extends I {
	default void greet() {
		
	}
}
