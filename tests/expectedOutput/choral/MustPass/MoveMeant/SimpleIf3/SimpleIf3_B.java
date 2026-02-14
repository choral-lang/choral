package choral.MustPass.MoveMeant.SimpleIf3;

import choral.MustPass.MoveMeant.SimpleIf3.utils.Client;
import choral.annotations.Choreography;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "B", name = "SimpleIf3" )
class SimpleIf3_B {
	public void fun( Unit c_A, Client c_B, SymChannel_B < Object > ch_AB ) {
		fun( c_B, ch_AB );
	}
	
	public void fun( Client c_B, SymChannel_B < Object > ch_AB ) {
		Integer y1 = 1;
		Integer y2 = 1;
		if( y1 < 1 ){
			ch_AB.< KOCEnum >select( KOCEnum.CASE0 );
			Integer msg0 = ch_AB.< Integer >com( Unit.id );
			c_B.fun( msg0 );
		} else { 
			ch_AB.< KOCEnum >select( KOCEnum.CASE1 );
			c_B.fun( y1 );
			ch_AB.< Integer >com( y1 );
		}
		if( y1 < y2 ){
			ch_AB.< KOCEnum >select( KOCEnum.CASE0 );
			c_B.fun();
		} else { 
			ch_AB.< KOCEnum >select( KOCEnum.CASE1 );
		}
	}

}
