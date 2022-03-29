package choral.examples.Karatsuba;

import choral.lang.Unit;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;

public class Karatsuba_C {
	public static Unit multiply( Unit n1, Unit n2, Unit ch_AB, SymChannel_B < Object > ch_BC, SymChannel_A < Object > ch_CA ) {
		return multiply( ch_BC, ch_CA );
	}
	
	public static Unit multiply( SymChannel_B < Object > ch_BC, SymChannel_A < Object > ch_CA ) {
		{
			switch( ch_CA.< Choice >select( Unit.id ) ){
				case DONE -> {
					return Unit.id;
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case RECUR -> {
					Karatsuba_B.multiply( Unit.id, Unit.id, ch_BC, ch_CA, Unit.id );
					ch_CA.< Long >com( Karatsuba_A.multiply( ch_CA.< Long >com( Unit.id ), ch_CA.< Long >com( Unit.id ), ch_CA, Unit.id, ch_BC ) );
					Karatsuba_C.multiply( Unit.id, Unit.id, Unit.id, ch_BC, ch_CA );
					return Unit.id;
				}
			}
		}
	}

}
