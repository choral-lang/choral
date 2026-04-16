package choral.MustPass.Typer.StandardLibraryReduction;

import choral.annotations.Choreography;
import java.lang.Math;
import java.math.BigInteger;
import java.util.Optional;

@Choreography( role = "A", name = "StandardLibraryReduction" )
class StandardLibraryReduction_A {
	public void test() {
		int a = 3;
		int b = 7;
		Math.max( a, b );
		BigInteger x = new BigInteger( "1999" );
		Optional < String > none = Optional.< String >of( "S" );
	}

}
