package choral.MustPass.MoveMeant.SimpleIf3;

import choral.MustPass.MoveMeant.SimpleIf3.utils.Client;
import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.lang.Unit;

@Choreography( role = "A", name = "SimpleIf3" )
class SimpleIf3_A {
	public void fun( Client c_A, Unit c_B, SymChannel_A < Object > ch_AB ) {
		fun( c_A, ch_AB );
	}
	
	public void fun( Client c_A, SymChannel_A < Object > ch_AB ) {
		Integer x = 0;
		switch( ch_AB.< KOCEnum >select( Unit.id ) ){
			case CASE0 -> {
				ch_AB.< Integer >com( x );
				c_A.fun( x );
			}
			case CASE1 -> {
				Integer dependencyAtA_4115711 = ch_AB.< Integer >com( Unit.id );
				c_A.fun( dependencyAtA_4115711 );
			}
			default -> {
				throw new RuntimeException( "Received unexpected label from select operation" );
			}
		}
		c_A.midpoint();
		switch( ch_AB.< KOCEnum >select( Unit.id ) ){
			case CASE0 -> {
				
			}
			case CASE1 -> {
				c_A.fun();
			}
			default -> {
				throw new RuntimeException( "Received unexpected label from select operation" );
			}
		}
	}

}
