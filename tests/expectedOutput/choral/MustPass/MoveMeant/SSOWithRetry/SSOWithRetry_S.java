package choral.MustPass.MoveMeant.SSOWithRetry;

import choral.MustPass.MoveMeant.SSOWithRetry.utils.*;
import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "S", name = "SSOWithRetry" )
public class SSOWithRetry_S {
	SymChannel_B < Object > ch_CS;
	SymChannel_A < Object > ch_SCAS;

	public SSOWithRetry_S( SymChannel_B < Object > ch_CS, SymChannel_A < Object > ch_SCAS, Unit ch_CASC ) {
		this( ch_CS, ch_SCAS );
	}
	
	public SSOWithRetry_S( SymChannel_B < Object > ch_CS, SymChannel_A < Object > ch_SCAS ) {
		this.ch_CS = ch_CS;
		this.ch_SCAS = ch_SCAS;
	}

	public void auth( Unit client, Service service, Unit authenticator ) {
		auth( service );
	}
	
	public void auth( Service service ) {
		switch( ch_SCAS.< KOCEnum >select( Unit.id ) ){
			case CASE0 -> {
				ch_CS.< Token >com( service.newToken() );
			}
			case CASE1 -> {
				switch( ch_CS.< KOCEnum >select( Unit.id ) ){
					case CASE0 -> {
						auth( Unit.id, service, Unit.id );
					}
					case CASE1 -> {
						
					}
					default -> {
						throw new RuntimeException( "Received unexpected label from select operation" );
					}
				}
			}
			default -> {
				throw new RuntimeException( "Received unexpected label from select operation" );
			}
		}
	}

}
