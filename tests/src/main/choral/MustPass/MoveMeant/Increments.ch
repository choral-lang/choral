package choral.MustPass.MoveMeant.Increments;

import choral.channels.SymChannel;

class Increments@( A, B ) {
	public void fun( SymChannel@( A, B )< Object > ch_AB ) {
		Integer@A a1 = 1@A; 
        Integer@B b1 = 2@B + 3@B;
        Integer@A a2 = 5@A + b1;
        a1 += 4@A;
        Integer@B b2 = a1 + 5@B * b1;
        b2 -= a1;
        // b2 += 1@B + 1@A; // Illegal

        Boolean@B b3 = true@B;
        Boolean@A a3 = b3 && false@A;
        b3 &= a3 || true@B;
        a3 |= false@A || b3;
        // a3 |= true@B; // illegal
	}
}
