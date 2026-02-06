package choral.MustPass.MoveMeant.SimpleKOC;

import choral.annotations.Choreography;
import choral.channels.SymChannel_A;

@Choreography( role = "A", name = "SimpleKOC" )
class SimpleKOC_A {
	public static void fun( SymChannel_A < Object > ch_AB, SymChannel_A < Object > ch_AC ) {
		int IA = 0;
		if( IA < 1 ){
			ch_AC.< KOCEnum >select( KOCEnum.CASE0 );
			ch_AB.< KOCEnum >select( KOCEnum.CASE0 );
			IA = IA + 1;
		} else { 
			ch_AC.< KOCEnum >select( KOCEnum.CASE1 );
			ch_AB.< KOCEnum >select( KOCEnum.CASE1 );
			IA = IA - 1;
		}
		if( IA < 1 ){
			ch_AB.< KOCEnum >select( KOCEnum.CASE0 );
			IA = IA + 1;
		} else { 
			ch_AB.< KOCEnum >select( KOCEnum.CASE1 );
		}
	}

}
