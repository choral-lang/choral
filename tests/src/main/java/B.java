import choral.annotations.Choreography;

@Choreography( role = "P", name = "B" )
class B {
	Boolean b1;
	Boolean b2;
	Integer i;
	Double f;
	String s;

	static void main() {
		b1 = true;
		i = 42;
		f = 3.14;
		s = "Ciao";
	}

}
