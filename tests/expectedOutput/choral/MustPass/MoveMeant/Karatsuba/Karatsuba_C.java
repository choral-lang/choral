package choral.MustPass.MoveMeant.Karatsuba;

import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "C", name = "Karatsuba" )
public class Karatsuba_C {
	public static Unit multiply( Unit n1, Unit n2, Unit ch_AB, SymChannel_B < Object > ch_BC, SymChannel_A < Object > ch_CA ) {
		return multiply( ch_BC, ch_CA );
	}
	
	public static Unit multiply( SymChannel_B < Object > ch_BC, SymChannel_A < Object > ch_CA ) {
		switch( ch_CA.< KOCEnum >select( Unit.id ) ){
			case CASE0 -> {
				return Unit.id;
			}
			case CASE1 -> {
				Karatsuba_B.multiply( Unit.id, Unit.id, ch_BC, ch_CA, Unit.id );
				Long dependencyAtC_1783206154 = ch_CA.< Long >com( Unit.id );
				Long dependencyAtC_1674685536 = ch_CA.< Long >com( Unit.id );
				Long z2 = Karatsuba_A.multiply( dependencyAtC_1783206154, dependencyAtC_1674685536, ch_CA, Unit.id, ch_BC );
				ch_CA.< Long >com( z2 );
				Karatsuba_C.multiply( Unit.id, Unit.id, Unit.id, ch_BC, ch_CA );
				return Unit.id;
			}
			default -> {
				throw new RuntimeException( "Received unexpected label from select operation" );
			}
		}
	}

}
