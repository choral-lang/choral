package choral.MustPass.MoveMeant.DownloadFile;

import choral.MustPass.MoveMeant.SendPackets.SendPackets_S;
import choral.MustPass.MoveMeant.SendPackets.utils.Server;
import choral.annotations.Choreography;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "Storage", name = "DownloadFile" )
public class DownloadFile_Storage {
	public static Unit downloadFile( SymChannel_B < Object > channel, Unit filename_D, Unit client, Server server ) {
		return downloadFile( channel, server );
	}
	
	public static Unit downloadFile( SymChannel_B < Object > channel, Server server ) {
		String msg0 = channel.< String >com( Unit.id );
		String filename_S = msg0;
		server.file = server.readFile( filename_S );
		server.n = 0;
		SendPackets_S.sendPackets( channel, server, Unit.id );
		return Unit.id;
	}

}
