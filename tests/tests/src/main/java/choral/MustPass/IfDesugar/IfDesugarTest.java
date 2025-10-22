package choral.MustPass.IfDesugar;

import java.utils.Random;
import choral.annotations.Choreography;

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
