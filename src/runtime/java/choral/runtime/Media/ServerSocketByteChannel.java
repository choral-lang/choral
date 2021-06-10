package choral.runtime.Media;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

public class ServerSocketByteChannel {

	private final ServerSocketChannel listeningChannel;

	private ServerSocketByteChannel( String hostname, int portNumber ){
		try {
			this.listeningChannel = ServerSocketChannel.open();
			listeningChannel.socket().bind( new InetSocketAddress( hostname, portNumber ) );
		} catch( IOException e ) {
			e.printStackTrace();
			throw new RuntimeException( "Could not initialise listening channel" );
		}
	}

	public static ServerSocketByteChannel at( String hostname, int portNumber ){
		return new ServerSocketByteChannel( hostname, portNumber );
	}

	public SocketByteChannel getNext() throws IOException {
		return new SocketByteChannel( listeningChannel.accept() );
	}

	public void close(){
		try {
			listeningChannel.close();
		} catch( IOException e ) {
			e.printStackTrace();
			throw new RuntimeException( "Could not properly close the listening channel" );
		}
	}

	public boolean isOpen(){
		return listeningChannel.isOpen();
	}

}
