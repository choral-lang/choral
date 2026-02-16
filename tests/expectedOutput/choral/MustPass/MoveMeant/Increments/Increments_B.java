package choral.MustPass.MoveMeant.Increments;

import choral.annotations.Choreography;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "B", name = "Increments" )
class Increments_B {
	public void fun( SymChannel_B < Object > ch_AB ) {
		Integer b1 = 2 + 3;
		ch_AB.< Integer >com( b1 );
		Integer msg1 = ch_AB.< Integer >com( Unit.id );
		Integer b2 = msg1 + 5 * b1;
		b2 -= msg1;
		Boolean b3 = true;
		ch_AB.< Boolean >com( b3 );
		Boolean msg3 = ch_AB.< Boolean >com( Unit.id );
		b3 &= msg3 || true;
	}

}
