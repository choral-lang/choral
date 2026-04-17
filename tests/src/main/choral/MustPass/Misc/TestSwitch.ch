package choral.MustPass.TestSwitch;

import choral.channels.*;

enum C@A{ LEFT, RIGHT }

class TestSwitch@( A,B ) {
	int@B m( SymChannel@(A,B)< Object > c ) {
		if( true@A ) {
			C@A.LEFT >> c::< C >select;
			return 5@B;
		} else {
			C@A.RIGHT >> c::< C >select;
			switch( C@A.LEFT ) {
				case RIGHT -> {
					C@A.RIGHT >> c::< C >select;
					return 6@B;
				}
				default -> {
					C@A.LEFT >> c::< C >select;
					return 7@B;
				}
			}
		}
	}
}
