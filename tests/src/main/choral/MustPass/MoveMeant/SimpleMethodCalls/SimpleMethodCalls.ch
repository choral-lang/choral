package choral.MustPass.MoveMeant.SimpleMethodCalls;

import choral.MustPass.MoveMeant.utils.Client;
import choral.channels.SymChannel;

class SimpleMethodCalls@( A, B ) {
	public void fun( Client@A c_A, Client@B c_B, SymChannel@( A, B )< Object > ch_AB ) {
		Integer@A i_A = 0@A;
		Integer@B i_B = 0@B;
		c_A.fun0();
		c_A.fun_in( i_A );
		c_A.fun_in( i_B );
		// c_A.fun_in( 0@B ); // illegal

		c_A.fun_in( c_A.fun_out() );
		c_A.fun_in( c_B.fun_out() );

		c_A.fun_in( c_B.fun_in_out( c_A.fun_out() ) );
		c_A.fun_in( c_B.price.currency );

		helper( i_A, i_B );
		helper( 0@A, 0@B );
	}

	private void helper(Integer@A in_A, Integer@B in_B){}

}
