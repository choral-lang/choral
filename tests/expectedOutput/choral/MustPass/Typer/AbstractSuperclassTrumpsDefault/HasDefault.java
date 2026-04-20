package choral.MustPass.Typer.AbstractSuperclassTrumpsDefault;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "HasDefault" )
interface HasDefault {
	default String greet() {
		return "default";
	}
}
