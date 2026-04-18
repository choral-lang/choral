package choral.MustFail.Typer.ConflictingDefaults;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Left" )
interface Left {
	default void foo() {
		
	}
}
