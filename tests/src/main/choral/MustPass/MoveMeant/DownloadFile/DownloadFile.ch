package choral.amend.downloadfile;

import choral.channels.SymChannel;
import choral.amend.sendpackets.utils.Client;
import choral.amend.sendpackets.utils.Server;

import choral.amend.sendpackets.SendPackets;

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