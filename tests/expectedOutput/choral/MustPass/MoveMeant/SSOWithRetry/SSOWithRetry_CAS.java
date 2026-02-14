package choral.MustPass.MoveMeant.SSOWithRetry;

import choral.MustPass.MoveMeant.SSOWithRetry.utils.*;
import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "CAS", name = "SSOWithRetry" )
public class SSOWithRetry_CAS {
	SymChannel_B < Object > ch_SCAS;
	SymChannel_A < Object > ch_CASC;

	public SSOWithRetry_CAS( Unit ch_CS, SymChannel_B < Object > ch_SCAS, SymChannel_A < Object > ch_CASC ) {
		this( ch_SCAS, ch_CASC );
	}
	
	public SSOWithRetry_CAS( SymChannel_B < Object > ch_SCAS, SymChannel_A < Object > ch_CASC ) {
		this.ch_SCAS = ch_SCAS;
		this.ch_CASC = ch_CASC;
	}

	public void auth( Unit client, Unit service, Authenticator authenticator ) {
		auth( authenticator );
	}
	
	public void auth( Authenticator authenticator ) {
		Creds msg0 = ch_CASC.< Creds >com( Unit.id );
		Creds x = msg0;
		if( authenticator.valid( x ) ){
			ch_CASC.< KOCEnum >select( KOCEnum.CASE0 );
			ch_SCAS.< KOCEnum >select( KOCEnum.CASE0 );
		} else { 
			ch_CASC.< KOCEnum >select( KOCEnum.CASE1 );
			ch_SCAS.< KOCEnum >select( KOCEnum.CASE1 );
			switch( ch_CASC.< KOCEnum >select( Unit.id ) ){
				case CASE0 -> {
					auth( Unit.id, Unit.id, authenticator );
				}
				case CASE1 -> {
					
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
			}
		}
	}

}
