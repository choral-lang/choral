package choral.MustPass.MoveMeant.SimpleArithmetic;

import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.lang.Unit;

@Choreography( role = "A", name = "SimpleArithmetic" )
class SimpleArithmetic_A {
	public void calc( SymChannel_A < Object > ch_AB ) {
		Integer a1 = 1;
		Integer msg0 = ch_AB.< Integer >com( Unit.id );
		Integer a2 = 4 + msg0;
		ch_AB.< Integer >com( a1 );
		ch_AB.< Integer >com( a2 );
	}

}
