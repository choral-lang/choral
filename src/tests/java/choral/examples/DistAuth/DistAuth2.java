package choral.examples.DistAuth;
import choral.examples.AuthResult.AuthResult2;
import choral.examples.DistAuthUtils.AuthToken;
import org.choral.runtime.Enum.EnumBoolean;
import org.choral.runtime.TLSChannel.TLSChannel1;
import org.choral.lang.Unit;
import org.choral.annotations.Choreography;

@Choreography( role = "Service", name = "DistAuth" )
public class DistAuth2 {
	private TLSChannel1 < Object > ch_Service_IP;

	public DistAuth2( Unit ch_Client_IP, TLSChannel1 < Object > ch_Service_IP ) {
		this( ch_Service_IP );
	}

	public DistAuth2( TLSChannel1 < Object > ch_Service_IP ) {
		this.ch_Service_IP = ch_Service_IP;
	}

	private Unit calcHash( Unit salt, Unit pwd ) {
		{
			return Unit.id;
		}
	}

	public AuthResult2 authenticate( Unit credentials ) {
		return authenticate();
	}

	public AuthResult2 authenticate() {
		calcHash( Unit.id, Unit.id );
		{
			switch( ch_Service_IP.< EnumBoolean >select( Unit.id ) ){
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case True -> {
					return new AuthResult2( Unit.id, ch_Service_IP.< AuthToken >com( Unit.id ) );
				}
				case False -> {
					return new AuthResult2();
				}
			}
		}
	}

}
