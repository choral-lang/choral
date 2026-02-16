package choral.MustPass.MoveMeant.SendPackets.utils;

import choral.annotations.Choreography;
import java.util.List;

@Choreography( role = "A", name = "Server" )
public class Server {
	public List < Integer > file;
	public int n;

	public Server() {
		
	}
	
	public Server( List < Integer > file ) {
		
	}

	public int packets( List < Integer > file ) {
		return 0;
	}
	
	public Integer mkPacket( List < Integer > file, int n ) {
		return 0;
	}
	
	public List < Integer > readFile( String filename ) {
		return null;
	}

}
