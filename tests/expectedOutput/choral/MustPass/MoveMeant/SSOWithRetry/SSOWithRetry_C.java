package choral.MustPass.MoveMeant.SSOWithRetry;

import choral.MustPass.MoveMeant.SSOWithRetry.utils.*;
import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "C", name = "SSOWithRetry" )
public class SSOWithRetry_C {
	SymChannel_A < Object > ch_CS;
	SymChannel_B < Object > ch_CASC;

	public SSOWithRetry_C( SymChannel_A < Object > ch_CS, Unit ch_SCAS, SymChannel_B < Object > ch_CASC ) {
		this( ch_CS, ch_CASC );
	}
	
	public SSOWithRetry_C( SymChannel_A < Object > ch_CS, SymChannel_B < Object > ch_CASC ) {
		this.ch_CS = ch_CS;
		this.ch_CASC = ch_CASC;
	}

	public void auth( Client client, Unit service, Unit authenticator ) {
		auth( client );
	}
	
	public void auth( Client client ) {
		ch_CASC.< Creds >com( client.creds() );
		switch( ch_CASC.< KOCEnum >select( Unit.id ) ){
			case CASE0 -> {
				Token msg1 = ch_CS.< Token >com( Unit.id );
				Token t = msg1;
			}
			case CASE1 -> {
				if( client.again() ){
					ch_CASC.< KOCEnum >select( KOCEnum.CASE0 );
					ch_CS.< KOCEnum >select( KOCEnum.CASE0 );
					auth( client, Unit.id, Unit.id );
				} else { 
					ch_CASC.< KOCEnum >select( KOCEnum.CASE1 );
					ch_CS.< KOCEnum >select( KOCEnum.CASE1 );
				}
			}
			default -> {
				throw new RuntimeException( "Received unexpected label from select operation" );
			}
		}
	}

}
