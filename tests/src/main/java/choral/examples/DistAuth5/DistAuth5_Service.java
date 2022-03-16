package choral.examples.DistAuth5;

import choral.lang.Unit;
import choral.runtime.TLSChannel.TLSChannel_A;
import choral.examples.DistAuthUtils.AuthToken;
import choral.DistAuth.EnumBoolean;
import choral.annotations.Choreography;
import choral.examples.AuthResult.AuthResult_B;

@Choreography( role = "Service", name = "DistAuth5" )
public class DistAuth5_Service {
	private TLSChannel_A< Object > ch_Service_IP;

	public DistAuth5_Service(
			Unit ch_Client_IP, TLSChannel_A< Object > ch_Service_IP, Unit ch_s1, Unit ch_s2
	) {
		this( ch_Service_IP );
	}

	public DistAuth5_Service( TLSChannel_A< Object > ch_Service_IP ) {
		this.ch_Service_IP = ch_Service_IP;
	}

	private Unit calcHash( Unit salt, Unit pwd ) {
		{
			return Unit.id;
		}
	}

	public AuthResult_B authenticate( Unit credentials ) {
		return authenticate();
	}

	public AuthResult_B authenticate() {
		calcHash( Unit.id, Unit.id );
		{
			switch( ch_Service_IP.< EnumBoolean >select( Unit.id ) ) {
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case True -> {
					return new AuthResult_B( Unit.id, ch_Service_IP.< AuthToken >com( Unit.id ) );
				}
				case False -> {
					return new AuthResult_B();
				}
			}
		}
	}

}
