package choral.MustPass.MoveMeant.SimpleIfStatements;

import choral.annotations.Choreography;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "B", name = "SimpleIfStatements" )
class SimpleIfStatements_B {
	public void fun( SymChannel_B < Object > channel ) {
		Integer c_B = 0;
		channel.< Integer >com( c_B );
		( Unit.id );
		Integer msg1 = channel.< Integer >com( Unit.id );
		if( c_B == msg1 + c_B ){
			
		}
	}

}
