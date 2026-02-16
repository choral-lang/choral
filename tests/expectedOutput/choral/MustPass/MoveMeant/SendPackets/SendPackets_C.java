package choral.MustPass.MoveMeant.SendPackets;

import choral.MustPass.MoveMeant.SendPackets.utils.Client;
import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.lang.Unit;

@Choreography( role = "C", name = "SendPackets" )
public class SendPackets_C {
	public static void sendPackets( SymChannel_A < Object > channel, Unit server, Client client ) {
		sendPackets( channel, client );
	}
	
	public static void sendPackets( SymChannel_A < Object > channel, Client client ) {
		switch( channel.< KOCEnum >select( Unit.id ) ){
			case CASE0 -> {
				Integer msg0 = channel.< Integer >com( Unit.id );
				Integer packet = msg0;
				client.file = client.append( client.file, packet );
				sendPackets( channel, Unit.id, client );
			}
			case CASE1 -> {
				
			}
			default -> {
				throw new RuntimeException( "Received unexpected label from select operation" );
			}
		}
	}

}
