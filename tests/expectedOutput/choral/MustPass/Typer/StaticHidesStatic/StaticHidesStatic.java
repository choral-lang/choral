package choral.MustPass.Typer.StaticHidesStatic;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "StaticHidesStatic" )
class StaticHidesStatic extends Base {
	public static String foo() {
		return "sub";
	}

}
