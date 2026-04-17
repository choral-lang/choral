package choral.MustPass.MoveMeant.SimpleArithmetic;

import choral.channels.SymChannel;

class SimpleArithmetic@( A, B ) {
	public void calc( SymChannel@( A, B )< Object > ch_AB ) {
		Integer@A a1 = 1@A; 
        Integer@B b1 = 2@B + 3@B;
        Integer@A a2 = 4@A + b1;
        Integer@B b2 = a1 + 5@B - b1 * a2;
        // Integer@B b3 = 1@B + 1@A; // Illegal
	}
}
