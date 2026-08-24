package Typer.ReturnTypeSubstitutable;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "ReturnTypeSubstitutable" )
class ReturnTypeSubstitutable implements Interface {
	public Child m() {
		return new Child();
	}

}
