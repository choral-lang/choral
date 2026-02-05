package choral.MustPass.MoveMeant.Sendpackets;

import choral.channels.SymChannel;
import choral.MustPass.MoveMeant.SendPackets.utils.Server;
import choral.MustPass.MoveMeant.SendPackets.utils.Client;

import choral.runtime.Serializers.KryoSerializable;

enum Stream@R{ NEXT, END }

public class SendPackets@( C, S ){

    public static void sendPackets( 
        SymChannel@( C, S )<Object> channel, 
        Server@S server, 
        Client@C client 
    ) {
        if (server.n < server.packets(server.file)){
            
            Integer@C packet = server.mkPacket( server.file, server.n );
            client.file = client.append( client.file, packet );
            server.n += 1@S;
            sendPackets( channel, server, client );
        }
        else{
            
        }
    }
}