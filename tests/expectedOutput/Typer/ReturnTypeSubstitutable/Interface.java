package Typer.ReturnTypeSubstitutable;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Interface" )
interface Interface {
	default Parent m() {
		return new Parent();
	}
}
