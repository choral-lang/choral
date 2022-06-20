package choral.examples.DistAuth10;

import choral.annotations.Choreography;
import choral.DistAuth.EnumBoolean;
import choral.runtime.TLSChannel.TLSChannel_A;
import choral.lang.Unit;

@Choreography( role = "S6", name = "DistAuth10" )
public class DistAuth10_S6 {
	private TLSChannel_A < Object > ch_s6;

	public DistAuth10_S6( Unit ch_Client_IP, Unit ch_Service_IP, Unit ch_s1, Unit ch_s2, Unit ch_s3, Unit ch_s4, Unit ch_s5, TLSChannel_A < Object > ch_s6, Unit ch_s7 ) {
		this( ch_s6 );
	}
	
	public DistAuth10_S6( TLSChannel_A < Object > ch_s6 ) {
		this.ch_s6 = ch_s6;
	}

	private Unit calcHash( Unit salt, Unit pwd ) {
		{
			return Unit.id;
		}
	}
	
	public Unit authenticate( Unit credentials ) {
		return authenticate();
	}
	
	public Unit authenticate() {
		calcHash( Unit.id, Unit.id );
		{
			switch( ch_s6.< EnumBoolean >select( Unit.id ) ){
				case True -> {
					return Unit.id;
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case False -> {
					return Unit.id;
				}
			}
		}
	}

}
