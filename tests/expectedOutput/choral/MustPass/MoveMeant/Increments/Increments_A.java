package choral.MustPass.MoveMeant.Increments;

import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.lang.Unit;

@Choreography( role = "A", name = "Increments" )
class Increments_A {
	public void fun( SymChannel_A < Object > ch_AB ) {
		Integer a1 = 1;
		Integer msg0 = ch_AB.< Integer >com( Unit.id );
		Integer a2 = 5 + msg0;
		a1 += 4;
		ch_AB.< Integer >com( a1 );
		Boolean msg2 = ch_AB.< Boolean >com( Unit.id );
		Boolean a3 = msg2 && false;
		ch_AB.< Boolean >com( a3 );
		a3 |= false || msg2;
	}

}
