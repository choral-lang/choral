package choral.MustPass.MoveMeant.DownloadFile;

import choral.MustPass.MoveMeant.SendPackets.SendPackets_C;
import choral.MustPass.MoveMeant.SendPackets.utils.Client;
import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.lang.Unit;

@Choreography( role = "Downloader", name = "DownloadFile" )
public class DownloadFile_Downloader {
	public static Object downloadFile( SymChannel_A < Object > channel, String filename_D, Client client, Unit server ) {
		return downloadFile( channel, filename_D, client );
	}
	
	public static Object downloadFile( SymChannel_A < Object > channel, String filename_D, Client client ) {
		channel.< String >com( filename_D );
		client.file = client.emptyFile();
		SendPackets_C.sendPackets( channel, Unit.id, client );
		return client.file;
	}

}
