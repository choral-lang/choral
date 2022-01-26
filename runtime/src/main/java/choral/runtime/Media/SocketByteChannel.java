package choral.runtime.Media;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SocketByteChannel implements BlockingByteChannel {

	private final SocketChannel channel;

	public SocketByteChannel( SocketChannel channel ) {
		this.channel = channel;
	}

	public static SocketByteChannel connect( String hostname, int portNumber ) {
		try {
			SocketChannel channel = SocketChannel.open();
			channel.connect( new InetSocketAddress( hostname, portNumber ) );
			channel.configureBlocking( true );
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

	@Override
	public int recvTransmissionLength() throws IOException {
		DataInputStream dis = new DataInputStream( channel.socket().getInputStream() );
		return dis.readInt();
	}

	@Override
	public void sendTransmissionLength( int length ) throws IOException {
		DataOutputStream dos = new DataOutputStream( channel.socket().getOutputStream() );
		dos.writeInt( length );
	}

}
