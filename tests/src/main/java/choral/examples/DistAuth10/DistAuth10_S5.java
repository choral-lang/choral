package choral.examples.DistAuth10;

import choral.DistAuth.EnumBoolean;
import choral.runtime.TLSChannel.TLSChannel_A;
import choral.lang.Unit;
import choral.annotations.Choreography;

@Choreography( role = "S5", name = "DistAuth10" )
public class DistAuth10_S5 {
	private TLSChannel_A < Object > ch_s5;

	public DistAuth10_S5( Unit ch_Client_IP, Unit ch_Service_IP, Unit ch_s1, Unit ch_s2, Unit ch_s3, Unit ch_s4, TLSChannel_A < Object > ch_s5, Unit ch_s6, Unit ch_s7 ) {
		this( ch_s5 );
	}
	
	public DistAuth10_S5( TLSChannel_A < Object > ch_s5 ) {
		this.ch_s5 = ch_s5;
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
			switch( ch_s5.< EnumBoolean >select( Unit.id ) ){
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
