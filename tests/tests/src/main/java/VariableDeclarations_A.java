import choral.annotations.Choreography;

@Choreography( role = "A", name = "VariableDeclarations" )
class VariableDeclarations_A {
	void method() {
		String a;
		String b = "test";
		Integer c, d, e, f;
		c = d = e = f = 42;
		Integer g, h = 13, i = 2, j, k, l = 1337;
	}

}
