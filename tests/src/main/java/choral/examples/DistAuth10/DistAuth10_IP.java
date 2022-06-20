package choral.examples.DistAuth10;

import choral.runtime.TLSChannel.TLSChannel_B;
import choral.DistAuth.EnumBoolean;
import choral.examples.DistAuthUtils.AuthToken;
import choral.annotations.Choreography;
import choral.lang.Unit;
import choral.examples.DistAuthUtils.ClientRegistry;

@Choreography( role = "IP", name = "DistAuth10" )
public class DistAuth10_IP {
	private TLSChannel_B < Object > ch_Client_IP;
	private TLSChannel_B < Object > ch_Service_IP;
	private TLSChannel_B < Object > ch_s1;
	private TLSChannel_B < Object > ch_s2;
	private TLSChannel_B < Object > ch_s3;
	private TLSChannel_B < Object > ch_s4;
	private TLSChannel_B < Object > ch_s5;
	private TLSChannel_B < Object > ch_s6;
	private TLSChannel_B < Object > ch_s7;

	public DistAuth10_IP( TLSChannel_B < Object > ch_Client_IP, TLSChannel_B < Object > ch_Service_IP, TLSChannel_B < Object > ch_s1, TLSChannel_B < Object > ch_s2, TLSChannel_B < Object > ch_s3, TLSChannel_B < Object > ch_s4, TLSChannel_B < Object > ch_s5, TLSChannel_B < Object > ch_s6, TLSChannel_B < Object > ch_s7 ) {
		this.ch_Client_IP = ch_Client_IP;
		this.ch_Service_IP = ch_Service_IP;
		this.ch_s1 = ch_s1;
		this.ch_s2 = ch_s2;
		this.ch_s3 = ch_s3;
		this.ch_s4 = ch_s4;
		this.ch_s5 = ch_s5;
		this.ch_s6 = ch_s6;
		this.ch_s7 = ch_s7;
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
			ch_s1.< EnumBoolean >select( EnumBoolean.True );
			ch_s2.< EnumBoolean >select( EnumBoolean.True );
			ch_s3.< EnumBoolean >select( EnumBoolean.True );
			ch_s4.< EnumBoolean >select( EnumBoolean.True );
			ch_s5.< EnumBoolean >select( EnumBoolean.True );
			ch_s6.< EnumBoolean >select( EnumBoolean.True );
			ch_s7.< EnumBoolean >select( EnumBoolean.True );
			AuthToken t = AuthToken.create();
			return Unit.id( ch_Client_IP.< AuthToken >com( t ), ch_Service_IP.< AuthToken >com( t ) );
		} else { 
			ch_Client_IP.< EnumBoolean >select( EnumBoolean.False );
			ch_Service_IP.< EnumBoolean >select( EnumBoolean.False );
			ch_s1.< EnumBoolean >select( EnumBoolean.False );
			ch_s2.< EnumBoolean >select( EnumBoolean.False );
			ch_s3.< EnumBoolean >select( EnumBoolean.False );
			ch_s4.< EnumBoolean >select( EnumBoolean.False );
			ch_s5.< EnumBoolean >select( EnumBoolean.False );
			ch_s6.< EnumBoolean >select( EnumBoolean.False );
			ch_s7.< EnumBoolean >select( EnumBoolean.False );
			return Unit.id;
		}
	}

}
