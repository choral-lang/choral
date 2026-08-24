package Misc.IfDesugarTest;

import choral.annotations.Choreography;
import java.util.Random;

@Choreography( role = "P", name = "IfDesugarTest" )
class IfDesugarTest {
	void main() {
		Random random = new Random();
		if( random.nextBoolean() ){
			System.out.println( "Even" );
		} else { 
			System.out.println( "Odd" );
		}
	}

}
