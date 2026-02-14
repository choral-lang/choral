package choral.MustPass.MoveMeant.SimpleIfStatements;

import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.lang.Unit;

@Choreography( role = "A", name = "SimpleIfStatements" )
class SimpleIfStatements_A {
	public void fun( SymChannel_A < Object > channel ) {
		Integer c_A = 0;
		if( 1 < 4 ){
			
		}
		if( 1 > c_A ){
			
		}
		Integer msg0 = channel.< Integer >com( Unit.id );
		if( 0 > msg0 ){
			
		}
		if( msg0 > 0 ){
			
		}
		if( msg0 == 0 ){
			
		}
		if( msg0 + 0 < msg0 * 2 ){
			
		}
		if( !( 1 == 0 ) ){
			
		}
		channel.< Integer >com( c_A );
	}

}
