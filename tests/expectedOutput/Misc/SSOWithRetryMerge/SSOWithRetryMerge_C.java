package Misc.SSOWithRetryMerge;

import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "C", name = "SSOWithRetryMerge" )
public class SSOWithRetryMerge_C {
	SymChannel_A < Object > ch_CS;
	SymChannel_B < Object > ch_CAS_C;

	public void auth() {
		switch( ch_CAS_C.< Validity >select( Unit.id ) ){
			case TOKEN -> {
				
			}
			case INVALID -> {
				if( true ){
					ch_CAS_C.< Validity >select( Validity.RETRY );
					auth();
				} else { 
					ch_CAS_C.< Validity >select( Validity.ERROR );
				}
			}
			default -> {
				throw new RuntimeException( "Received unexpected label from select operation" );
			}
		}
	}

}
