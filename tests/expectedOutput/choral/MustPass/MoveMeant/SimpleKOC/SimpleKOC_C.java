package choral.MustPass.MoveMeant.SimpleKOC;

import choral.annotations.Choreography;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "C", name = "SimpleKOC" )
class SimpleKOC_C {
	public static void fun( Unit ch_AB, SymChannel_B < Object > ch_AC ) {
		fun( ch_AC );
	}
	
	public static void fun( SymChannel_B < Object > ch_AC ) {
		int IC = 0;
		switch( ch_AC.< KOCEnum >select( Unit.id ) ){
			case CASE0 -> {
				IC = IC + 1;
			}
			case CASE1 -> {
				IC = IC - 1;
			}
			default -> {
				throw new RuntimeException( "Received unexpected label from select operation" );
			}
		}
	}

}
