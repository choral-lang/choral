package choral.MustPass.Typer.OverrideProtectedWithPublic;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "OverrideProtectedWithPublic" )
class OverrideProtectedWithPublic extends Base {
	public String greet() {
		return "sub";
	}

}
