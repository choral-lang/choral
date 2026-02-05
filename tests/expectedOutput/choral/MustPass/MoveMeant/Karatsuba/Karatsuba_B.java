package choral.MustPass.MoveMeant.Karatsuba;

import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "B", name = "Karatsuba" )
public class Karatsuba_B {
	public static Unit multiply( Unit n1, Unit n2, SymChannel_B < Object > ch_AB, SymChannel_A < Object > ch_BC, Unit ch_CA ) {
		return multiply( ch_AB, ch_BC );
	}
	
	public static Unit multiply( SymChannel_B < Object > ch_AB, SymChannel_A < Object > ch_BC ) {
		switch( ch_AB.< KOCEnum >select( Unit.id ) ){
			case CASE0 -> {
				return Unit.id;
			}
			case CASE1 -> {
				Long dependencyAtB_890276879 = ch_AB.< Long >com( Unit.id );
				Long dependencyAtB_1841239912 = ch_AB.< Long >com( Unit.id );
				Long z0 = Karatsuba_A.multiply( dependencyAtB_890276879, dependencyAtB_1841239912, ch_BC, Unit.id, ch_AB );
				Karatsuba_C.multiply( Unit.id, Unit.id, Unit.id, ch_AB, ch_BC );
				ch_AB.< Long >com( z0 );
				Karatsuba_B.multiply( Unit.id, Unit.id, ch_AB, ch_BC, Unit.id );
				return Unit.id;
			}
			default -> {
				throw new RuntimeException( "Received unexpected label from select operation" );
			}
		}
	}

}
