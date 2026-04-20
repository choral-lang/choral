package choral.MustPass.Typer.ProtectedMethodInherited;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "ProtectedMethodInherited" )
class ProtectedMethodInherited extends Base {
	void test() {
		m();
	}

}
