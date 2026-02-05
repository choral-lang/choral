package choral.MustPass.MoveMeant.SimpleInfer;

import choral.annotations.Choreography;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "B", name = "SimpleInfer" )
class SimpleInfer_B {
	SymChannel_B < Integer > ch;

	Integer main( Unit x, Integer y ) {
		return main( y );
	}
	
	Integer main( Integer y ) {
		Integer dependencyAtB_77738832 = ch.< Integer >com( Unit.id );
		return dependencyAtB_77738832 + y;
	}

}
