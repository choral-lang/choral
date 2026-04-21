package choral.MustPass.Typer.StaticOverridesStaticInferface;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Foo" )
interface Foo {
	static int hello() {
		return 1;
	}
}
