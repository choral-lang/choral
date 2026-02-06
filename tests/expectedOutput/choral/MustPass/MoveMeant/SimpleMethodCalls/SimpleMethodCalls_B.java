package choral.MustPass.MoveMeant.SimpleMethodCalls;

import choral.MustPass.MoveMeant.utils.Client;
import choral.annotations.Choreography;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "B", name = "SimpleMethodCalls" )
class SimpleMethodCalls_B {
	public void fun( Unit c_A, Client c_B, SymChannel_B < Object > ch_AB ) {
		fun( c_B, ch_AB );
	}
	
	private void helper( Unit in_A, Integer in_B ) {
		
	}
	
	public void fun( Client c_B, SymChannel_B < Object > ch_AB ) {
		Integer i_B = 0;
		ch_AB.< Integer >com( i_B );
		ch_AB.< Integer >com( c_B.fun_out() );
		Integer msg2 = ch_AB.< Integer >com( Unit.id );
		ch_AB.< Integer >com( c_B.fun_in_out( msg2 ) );
		ch_AB.< String >com( c_B.price.currency );
		helper( Unit.id, i_B );
		helper( Unit.id, 0 );
	}

}
