package choral.examples.DistAuth5;
import choral.lang.Unit;
import choral.DistAuth.EnumBoolean;
import choral.runtime.TLSChannel.TLSChannel_A;
import choral.annotations.Choreography;

@Choreography( role = "S2", name = "DistAuth5" )
public class DistAuth5_S2 {
	private TLSChannel_A < Object > ch_s2;

	public DistAuth5_S2( Unit ch_Client_IP, Unit ch_Service_IP, Unit ch_s1, TLSChannel_A < Object > ch_s2 ) {
		this( ch_s2 );
	}
	
	public DistAuth5_S2( TLSChannel_A < Object > ch_s2 ) {
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
