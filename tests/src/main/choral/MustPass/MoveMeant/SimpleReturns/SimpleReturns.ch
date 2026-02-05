package choral.MustPass.MoveMeant.SimpleReturns;

import choral.channels.SymChannel;

class SimpleReturns@( A, B ) {
	public void fun( SymChannel@( A, B )< Object > ch_AB ) {
		Integer@A i_A = 0@A;
		Integer@B i_B = 0@B;
        
        Integer@A i2_A = out_A(1@A);
        i2_A = out_A(i_A);
        i2_A = out_A(i_B);
        // i2_A = out_A(1@B); // illegal

        Integer@B i2_B = out_A(1@A);
        i2_B = out_A(i_A);
        i2_B = out_A(i_B);
        // i2_B = out_A(1@B); // illegal
        
	}

	private Integer@A out_A(Integer@A in_A){
        return in_A;
    }

}
