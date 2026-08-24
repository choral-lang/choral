package Misc.SSOWithRetryMerge;

import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "S", name = "SSOWithRetryMerge" )
public class SSOWithRetryMerge_S {
	SymChannel_B < Object > ch_CS;
	SymChannel_A < Object > ch_S_CAS;

	public void auth() {
		switch( ch_S_CAS.< Validity >select( Unit.id ) ){
			case TOKEN -> {
				
			}
			case RETRY -> {
				auth();
			}
			case ERROR -> {
				
			}
			default -> {
				throw new RuntimeException( "Received unexpected label from select operation" );
			}
		}
	}

}
