package choral.MustFail.Typer.ConflictingDefaults;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Right" )
interface Right {
	default void foo() {
		
	}
}
