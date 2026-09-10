package Typer.VarInference;

import java.math.BigInteger;

class VarInference@( A ) {
	void run() {
		var number = 5@A;
		var text = "hello"@A;
		var integer = new BigInteger@A( "1999"@A );
		var maximum = Math@A.max( number, 7@A );
		var copy = text;
		final var constant = maximum;
		System@A.out.println( integer );
		System@A.out.println( copy );
		System@A.out.println( constant );
	}
}
