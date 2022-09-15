package choral.examples.DistAuth10;

import choral.annotations.Choreography;
import choral.DistAuth.EnumBoolean;
import choral.lang.Unit;
import choral.runtime.TLSChannel.TLSChannel_A;

@Choreography( role = "S2", name = "DistAuth10" )
public class DistAuth10_S2 {
	private TLSChannel_A < Object > ch_s2;

	public DistAuth10_S2( Unit ch_Client_IP, Unit ch_Service_IP, Unit ch_s1, TLSChannel_A < Object > ch_s2, Unit ch_s3, Unit ch_s4, Unit ch_s5, Unit ch_s6, Unit ch_s7 ) {
		this( ch_s2 );
	}

	public DistAuth10_S2( TLSChannel_A < Object > ch_s2 ) {
		this.ch_s2 = ch_s2;
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
			switch( ch_s2.< EnumBoolean >select( Unit.id ) ){
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
