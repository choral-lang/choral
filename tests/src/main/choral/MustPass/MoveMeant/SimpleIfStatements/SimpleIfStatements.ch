package choral.amend.SimpleIfStatements;

import choral.channels.SymChannel;

class SimpleIfStatements@( A, B ) {
	public void fun( SymChannel@( A, B )< Object > channel ) {
		Integer@A c_A = 0@A;
        Integer@B c_B = 0@B;
        
        if( 1@A < 4@A ){}

        if( 1@A > c_A ){}

        if( 0@A > c_B ){} 

        if( c_B > 0@A ){} 

        if( c_B == 0@A ){} 
        
        if( c_B + 0@A <  c_B * 2@A ){} 
        
        if( !(1@A == 0@A) ){} 
        
        if( c_B == c_A + c_B ){} 

	}
}
