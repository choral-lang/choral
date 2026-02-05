package choral.MustPass.MoveMeant.DownloadFile;

import choral.channels.SymChannel;
import choral.MustPass.MoveMeant.SendPackets.utils.Client;
import choral.MustPass.MoveMeant.SendPackets.utils.Server;

import choral.MustPass.MoveMeant.SendPackets.SendPackets;

public class DownloadFile@( Downloader, Storage ){

    public static Object@Downloader downloadFile( 
        SymChannel@( Downloader, Storage )<Object> channel, 
        String@Downloader filename_D,
        Client@Downloader client,
        Server@Storage server
    ){

        String@Storage filename_S = filename_D;
        server.file = server.readFile(filename_S);
        client.file = client.emptyFile();
        server.n = 0@Storage;
        SendPackets@( Downloader, Storage ).sendPackets( channel, server, client );
        return client.file;
    }
}