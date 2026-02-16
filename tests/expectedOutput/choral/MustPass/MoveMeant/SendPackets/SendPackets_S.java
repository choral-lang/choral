package choral.MustPass.MoveMeant.SendPackets;

import choral.MustPass.MoveMeant.SendPackets.utils.Server;
import choral.annotations.Choreography;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "S", name = "SendPackets" )
public class SendPackets_S {
	public static void sendPackets( SymChannel_B < Object > channel, Server server, Unit client ) {
		sendPackets( channel, server );
	}
	
	public static void sendPackets( SymChannel_B < Object > channel, Server server ) {
		if( server.n < server.packets( server.file ) ){
			channel.< KOCEnum >select( KOCEnum.CASE0 );
			channel.< Integer >com( server.mkPacket( server.file, server.n ) );
			server.n += 1;
			sendPackets( channel, server, Unit.id );
		} else { 
			channel.< KOCEnum >select( KOCEnum.CASE1 );
		}
	}

}
