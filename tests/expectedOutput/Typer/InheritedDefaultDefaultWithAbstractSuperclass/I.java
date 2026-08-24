package Typer.InheritedDefaultDefaultWithAbstractSuperclass;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "I" )
interface I {
	default void greet() {
		
	}
}
