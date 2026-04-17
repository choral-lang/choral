package choral.MustPass.Typer.CovariantReturn;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "CovariantReturn" )
class CovariantReturn extends Base {
	public String foo() {
		return "world";
	}

}
