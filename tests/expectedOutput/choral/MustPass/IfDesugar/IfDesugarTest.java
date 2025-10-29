package choral.MustPass.IfDesugar;

import choral.annotations.Choreography;
import java.utils.Random;

@Choreography( role = "P", name = "IfDesugarTest" )
class IfDesugarTest {
	void main() {
		if( Random.nextBoolean() ){
			System.out.println( "Even" );
		} else { 
			System.out.println( "Odd" );
		}
	}

}
