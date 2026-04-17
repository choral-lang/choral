package choral.MustPass.Typer.ClassBeatsDefault;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "ClassBeatsDefault" )
class ClassBeatsDefault implements Greeter {
	public String greet() {
		return "class";
	}

}
