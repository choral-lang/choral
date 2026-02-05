package choral.MustPass.MoveMeant.SimpleKOC;

import choral.annotations.Choreography;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "B", name = "SimpleKOC" )
class SimpleKOC_B {
	public static void fun( SymChannel_B < Object > ch_AB, Unit ch_AC ) {
		fun( ch_AB );
	}
	
	public static void fun( SymChannel_B < Object > ch_AB ) {
		int IB = 0;
		switch( ch_AB.< KOCEnum >select( Unit.id ) ){
			case CASE0 -> {
				IB = IB + 1;
			}
			case CASE1 -> {
				IB = IB - 1;
			}
			default -> {
				throw new RuntimeException( "Received unexpected label from select operation" );
			}
		}
		switch( ch_AB.< KOCEnum >select( Unit.id ) ){
			case CASE0 -> {
				IB = IB + 1;
			}
			case CASE1 -> {
				
			}
			default -> {
				throw new RuntimeException( "Received unexpected label from select operation" );
			}
		}
	}

}
