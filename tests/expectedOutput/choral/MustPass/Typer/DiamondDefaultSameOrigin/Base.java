package choral.MustPass.Typer.DiamondDefaultSameOrigin;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Base" )
interface Base {
	default void foo() {
		
	}
}
