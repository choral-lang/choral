package choral.MustPass.SwitchTest;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "SwitchTest" )
public class SwitchTest_A {
	int m1( Choice c ) {
		switch( c ){
			case FIRST -> {
				return 1;
			}
			case SECOND -> {
				return 2;
			}
			case THIRD -> {
				return 3;
			}
		}
		return 0;
	}
	
	int m2( Choice c ) {
		switch( c ){
			case FIRST -> {
				return 1;
			}
			case SECOND -> {
				return 2;
			}
			default -> {
				return 3;
			}
		}
	}
	
	int m3( Choice c ) {
		switch( c ){
			case FIRST -> {
				return 1;
			}
			default -> {
				return 0;
			}
		}
	}

}
