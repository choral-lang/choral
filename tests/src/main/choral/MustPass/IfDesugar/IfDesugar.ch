package choral.MustPass.IfDesugar;

import java.utils.Random;

class IfDesugarTest@( P ) {

	void main() {
		if( Random@( P ).nextBoolean() ){
			System@( P ).out.println( "Even"@P );
		} else {
			System@( P ).out.println( "Odd"@P );
		}
	}

}
