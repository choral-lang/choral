package choral.MustPass.Typer.StaticOverridesStaticInferface;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "StaticOverridesStaticInferface" )
class StaticOverridesStaticInferface implements Foo {
	static int hello() {
		return 0;
	}

}
