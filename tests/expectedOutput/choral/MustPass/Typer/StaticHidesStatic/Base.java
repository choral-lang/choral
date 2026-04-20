package choral.MustPass.Typer.StaticHidesStatic;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Base" )
class Base {
	public static String foo() {
		return "base";
	}

}
