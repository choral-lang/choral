package choral.examples.TestSwitch;

import choral.channels.*;

enum C@A{ LEFT, RIGHT }

class TestSwitch@( A,B ) {
	int@B m( SymChannel@(A,B)< Object > c) {
		if( true@A ) {
			c.< C >select( C@A.LEFT );
			return 5@B;
		} else {
			switch( c.< C >select( C@A.LEFT ) ) {
				case RIGHT -> {
					return 6@B;
				}
				default -> {
					return 7@B;
				}
			}
		}
	}
}
