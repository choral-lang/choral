package choral.MustPass.MoveMeant.SimpleInfer;

import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.lang.Unit;

@Choreography( role = "A", name = "SimpleInfer" )
class SimpleInfer_A {
	SymChannel_A < Integer > ch;

	Unit main( Integer x, Unit y ) {
		return main( x );
	}
	
	Unit main( Integer x ) {
		ch.< Integer >com( x );
		return Unit.id;
	}

}
