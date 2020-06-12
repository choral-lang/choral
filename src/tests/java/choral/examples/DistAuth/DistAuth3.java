package choral.examples.DistAuth;
import choral.examples.DistAuthUtils.AuthToken;
import choral.examples.DistAuthUtils.ClientRegistry;
import org.choral.runtime.Enum.EnumBoolean;
import org.choral.runtime.TLSChannel.TLSChannel2;
import org.choral.lang.Unit;
import org.choral.annotations.Choreography;

@Choreography( role = "IP", name = "DistAuth" )
public class DistAuth3 {
	private TLSChannel2 < Object > ch_Client_IP;
	private TLSChannel2 < Object > ch_Service_IP;

	public DistAuth3( TLSChannel2 < Object > ch_Client_IP, TLSChannel2 < Object > ch_Service_IP ) {
		this.ch_Client_IP = ch_Client_IP;
		this.ch_Service_IP = ch_Service_IP;
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
		ch_Client_IP.< String >com( ClientRegistry.getSalt( ch_Client_IP.< String >com( Unit.id ) ) );
		Boolean valid;
		valid = ClientRegistry.check( ch_Client_IP.< String >com( calcHash( Unit.id, Unit.id ) ) );
		if( valid ){
			ch_Client_IP.< EnumBoolean >select( EnumBoolean.True );
			ch_Service_IP.< EnumBoolean >select( EnumBoolean.True );
			AuthToken t;
			t = AuthToken.create();
			return Unit.id( ch_Client_IP.< AuthToken >com( t ), ch_Service_IP.< AuthToken >com( t ) );
		} else {
			ch_Client_IP.< EnumBoolean >select( EnumBoolean.False );
			ch_Service_IP.< EnumBoolean >select( EnumBoolean.False );
			return Unit.id;
		}
	}

}
