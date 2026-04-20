package choral.MustPass.Typer.PublicMethodAccessibleFromOutside;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "PublicMethodAccessibleFromOutside" )
class PublicMethodAccessibleFromOutside {
	void test( C c ) {
		c.m();
	}

}
