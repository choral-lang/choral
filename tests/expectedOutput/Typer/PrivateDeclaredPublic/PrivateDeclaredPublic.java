package Typer.PrivateDeclaredPublic;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "PrivateDeclaredPublic" )
class PrivateDeclaredPublic {
	void test( C c ) {
		c.m();
	}

}
