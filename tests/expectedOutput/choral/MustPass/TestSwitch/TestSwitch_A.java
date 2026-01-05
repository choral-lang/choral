package choral.MustPass.TestSwitch;

import choral.annotations.Choreography;
import choral.channels.*;
import choral.lang.Unit;

@Choreography( role = "A", name = "TestSwitch" )
class TestSwitch_A {
	Unit m( SymChannel_A < Object > c ) {
		if( true ){
			c.< C >select( C.LEFT );
			return Unit.id;
		} else { 
			c.< C >select( C.RIGHT );
			switch( C.LEFT ){
				case RIGHT -> {
					c.< C >select( C.RIGHT );
					return Unit.id;
				}
				default -> {
					c.< C >select( C.LEFT );
					return Unit.id;
				}
			}
		}
	}

}
