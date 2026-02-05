package choral.amend.simpleif3;

import choral.amend.simpleif3.utils.Client; 
import choral.channels.SymChannel;

class SimpleIf3@( A, B, C ) {
	public void fun( Client@A c_A, Client@B c_B, SymChannel@( A, B )< Object > ch_AB ) {
		Integer@A x = 0@A;
        Integer@B y1 = 1@B;
        Integer@B y2 = 1@B;
        if( y1 < 1@B ){
            c_B.fun(x);
            c_A.fun(x);
        } else {
            c_B.fun(y1);
            c_A.fun(y1);
        }
        c_A.midpoint();
        if( y1 < y2 ){
            c_B.fun();
        } else {
            c_A.fun();
        }
	}

}

