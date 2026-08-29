package Typer.VarInference;

import choral.annotations.Choreography;
import java.math.BigInteger;

@Choreography( role = "A", name = "VarInference" )
class VarInference {
	void run() {
		int number = 5;
		String text = "hello";
		BigInteger integer = new BigInteger( "1999" );
		int maximum = Math.max( number, 7 );
		String copy = text;
		final int constant = maximum;
		System.out.println( integer );
		System.out.println( copy );
		System.out.println( constant );
	}

}
