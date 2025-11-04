package choral.MustPass.LoggerExample;

import choral.annotations.Choreography;

@Choreography( role = "P", name = "LoggerExample" )
class LoggerExample {
	static Boolean b1;
	static Boolean b2;
	static Integer i;
	static Double f;
	static String s;

	static void main() {
		b1 = true;
		i = 42;
		f = 3.14;
		s = "Ciao";
	}

}
