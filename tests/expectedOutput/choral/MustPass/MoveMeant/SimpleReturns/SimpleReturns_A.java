package choral.MustPass.MoveMeant.SimpleReturns;

import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.lang.Unit;

@Choreography( role = "A", name = "SimpleReturns" )
class SimpleReturns_A {
	public void fun( SymChannel_A < Object > ch_AB ) {
		Integer i_A = 0;
		Integer i2_A = out_A( 1 );
		i2_A = out_A( i_A );
		Integer msg0 = ch_AB.< Integer >com( Unit.id );
		i2_A = out_A( msg0 );
		ch_AB.< Integer >com( out_A( 1 ) );
		ch_AB.< Integer >com( out_A( i_A ) );
		ch_AB.< Integer >com( out_A( msg0 ) );
	}
	
	private Integer out_A( Integer in_A ) {
		return in_A;
	}

}
