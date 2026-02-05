package choral.MustPass.MoveMeant.SimpleArithmetic;

import choral.annotations.Choreography;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "B", name = "SimpleArithmetic" )
class SimpleArithmetic_B {
	public void calc( SymChannel_B < Object > ch_AB ) {
		Integer b1 = 2 + 3;
		ch_AB.< Integer >com( b1 );
		Integer msg1 = ch_AB.< Integer >com( Unit.id );
		Integer msg2 = ch_AB.< Integer >com( Unit.id );
		Integer b2 = msg1 + 5 - b1 * msg2;
	}

}
