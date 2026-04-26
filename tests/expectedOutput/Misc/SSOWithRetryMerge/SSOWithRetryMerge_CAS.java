package Misc.SSOWithRetryMerge;

import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "CAS", name = "SSOWithRetryMerge" )
public class SSOWithRetryMerge_CAS {
	SymChannel_B < Object > ch_S_CAS;
	SymChannel_A < Object > ch_CAS_C;

	public void auth() {
		if( true ){
			ch_CAS_C.< Validity >select( Validity.TOKEN );
			ch_S_CAS.< Validity >select( Validity.TOKEN );
		} else { 
			ch_CAS_C.< Validity >select( Validity.INVALID );
			switch( ch_CAS_C.< Validity >select( Unit.id ) ){
				case RETRY -> {
					ch_S_CAS.< Validity >select( Validity.RETRY );
					auth();
				}
				case ERROR -> {
					ch_S_CAS.< Validity >select( Validity.ERROR );
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
			}
		}
	}

}
