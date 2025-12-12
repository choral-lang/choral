package choral.MustPass.IfDesugar;

import java.util.Random;

class IfDesugarTest@( P ) {

	void main() {
		Random@P random = new Random@P();
		if( random.nextBoolean() ){
			System@( P ).out.println( "Even"@P );
		} else {
			System@( P ).out.println( "Odd"@P );
		}
	}

}
