package choral.MustPass.MoveMeant.SimpleMethodCalls;

import choral.MustPass.MoveMeant.utils.Client;
import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.lang.Unit;

@Choreography( role = "A", name = "SimpleMethodCalls" )
class SimpleMethodCalls_A {
	public void fun( Client c_A, Unit c_B, SymChannel_A < Object > ch_AB ) {
		fun( c_A, ch_AB );
	}
	
	private void helper( Integer in_A, Unit in_B ) {
		
	}
	
	public void fun( Client c_A, SymChannel_A < Object > ch_AB ) {
		Integer i_A = 0;
		c_A.fun0();
		c_A.fun_in( i_A );
		Integer msg0 = ch_AB.< Integer >com( Unit.id );
		c_A.fun_in( msg0 );
		c_A.fun_in( c_A.fun_out() );
		Integer msg1 = ch_AB.< Integer >com( Unit.id );
		c_A.fun_in( msg1 );
		ch_AB.< Integer >com( c_A.fun_out() );
		Integer msg3 = ch_AB.< Integer >com( Unit.id );
		c_A.fun_in( msg3 );
		String msg4 = ch_AB.< String >com( Unit.id );
		c_A.fun_in( msg4 );
		helper( i_A, Unit.id );
		helper( 0, Unit.id );
	}

}
