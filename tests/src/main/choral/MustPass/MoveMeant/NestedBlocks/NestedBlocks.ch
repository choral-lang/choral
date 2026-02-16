package choral.MustPass.MoveMeant.NestedBlocks;

import choral.MustPass.MoveMeant.utils.Client;
import choral.channels.SymChannel;

class NestedBlocks@( A, B ) {
	public void fun( Client@A state_A, Client@B state_B, SymChannel@( A, B )< Object > ch_AB ) {
		Integer@A x = 0@A;
        Integer@B y1 = 1@B;
        Integer@B y2 = 1@B;
        if( x < 1@A ){
            state_B.fun0();
            {
            }
            state_B.fun_in(state_A.fun_in_out(state_B.fun_out()));
            state_A.fun_in(x);
        } else {
            {
                state_A.fun_in(y1);
            }
            state_B.fun_in(state_A.fun_in_out(y1));
            state_A.fun0();
        }
	}

}














