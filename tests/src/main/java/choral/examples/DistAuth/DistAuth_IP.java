package choral.examples.DistAuth;

import choral.lang.Unit;
import choral.annotations.Choreography;
import choral.DistAuth.EnumBoolean;
import choral.examples.DistAuthUtils.ClientRegistry;
import choral.runtime.TLSChannel.TLSChannel_B;
import choral.examples.DistAuthUtils.AuthToken;

@Choreography( role = "IP", name = "DistAuth" )
public class DistAuth_IP {
	private TLSChannel_B < Object > ch_Client_IP;
	private TLSChannel_B < Object > ch_Service_IP;

	public DistAuth_IP( TLSChannel_B < Object > ch_Client_IP, TLSChannel_B < Object > ch_Service_IP ) {
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
		Boolean valid = ClientRegistry.check( ch_Client_IP.< String >com( calcHash( Unit.id, Unit.id ) ) );
		if( valid ){
			ch_Client_IP.< EnumBoolean >select( EnumBoolean.True );
			ch_Service_IP.< EnumBoolean >select( EnumBoolean.True );
			AuthToken t = AuthToken.create();
			return Unit.id( ch_Client_IP.< AuthToken >com( t ), ch_Service_IP.< AuthToken >com( t ) );
		} else { 
			ch_Client_IP.< EnumBoolean >select( EnumBoolean.False );
			ch_Service_IP.< EnumBoolean >select( EnumBoolean.False );
			return Unit.id;
		}
	}

}
