package choral.examples.DistAuth10;
import choral.DistAuth.EnumBoolean;
import choral.lang.Unit;
import choral.annotations.Choreography;
import choral.runtime.TLSChannel.TLSChannel_A;

@Choreography( role = "S4", name = "DistAuth10" )
public class DistAuth10_S4 {
	private TLSChannel_A < Object > ch_s4;

	public DistAuth10_S4( Unit ch_Client_IP, Unit ch_Service_IP, Unit ch_s1, Unit ch_s2, Unit ch_s3, TLSChannel_A < Object > ch_s4, Unit ch_s5, Unit ch_s6, Unit ch_s7 ) {
		this( ch_s4 );
	}
	
	public DistAuth10_S4( TLSChannel_A < Object > ch_s4 ) {
		this.ch_s4 = ch_s4;
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
			switch( ch_s4.< EnumBoolean >select( Unit.id ) ){
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case True -> {
					return Unit.id;
				}
				case False -> {
					return Unit.id;
				}
			}
		}
	}

}
