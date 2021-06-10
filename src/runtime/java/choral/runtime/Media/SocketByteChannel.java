package choral.runtime.Media;

import choral.utils.Pair;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;

public class SocketByteChannel implements ByteChannel {

	private final SocketChannel channel;

	public SocketByteChannel( SocketChannel channel ) {
		this.channel = channel;
	}

	public static SocketByteChannel connect( String hostname, int portNumber ){
		try {
			SocketChannel channel = SocketChannel.open();
			channel.connect( new InetSocketAddress( hostname, portNumber ) );
			return new SocketByteChannel( channel );
		} catch( IOException e ) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int read( ByteBuffer dst ) throws IOException {
		return channel.read( dst );
	}

	@Override
	public int write( ByteBuffer src ) throws IOException {
		return channel.write( src );
	}

	@Override
	public boolean isOpen() {
		return channel.isOpen();
	}

	@Override
	public void close() throws IOException {
		channel.close();
	}
}
