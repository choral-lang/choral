package choral.MustPass.MoveMeant.SimpleReturns;

import choral.annotations.Choreography;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "B", name = "SimpleReturns" )
class SimpleReturns_B {
	public void fun( SymChannel_B < Object > ch_AB ) {
		Integer i_B = 0;
		out_A( Unit.id );
		out_A( Unit.id );
		ch_AB.< Integer >com( i_B );
		out_A( Unit.id );
		Integer msg1 = ch_AB.< Integer >com( out_A( Unit.id ) );
		Integer i2_B = msg1;
		Integer msg2 = ch_AB.< Integer >com( out_A( Unit.id ) );
		i2_B = msg2;
		Integer msg3 = ch_AB.< Integer >com( out_A( Unit.id ) );
		i2_B = msg3;
	}
	
	private Unit out_A( Unit in_A ) {
		return Unit.id;
	}

}
