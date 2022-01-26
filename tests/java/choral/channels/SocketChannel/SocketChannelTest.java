package choral.channels.SocketChannel;

import choral.lang.Unit;
import choral.runtime.Media.ServerSocketByteChannel;
import choral.runtime.Media.SocketByteChannel;
import choral.runtime.SerializerChannel.SerializerChannel_B;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_A;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_B;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class SocketChannelTest {

	public static void main( String[] args ) throws InterruptedException {

		ExecutorService client = Executors.newSingleThreadExecutor();
		ExecutorService server = Executors.newCachedThreadPool();

		server.submit( () -> {
			System.out.println( "Accepting connections" );
			ServerSocketByteChannel connectionListener =
					ServerSocketByteChannel.at( "localhost", 12345 );
			while( connectionListener.isOpen() ) {
				try {
					SerializerChannel_B channel = new SerializerChannel_B(
							KryoSerializer.getInstance(),
							new WrapperByteChannel_B( connectionListener.getNext() ) );
					server.submit( () -> {
						channel.com( "You are connected!" );
						String command = channel.com( Unit.id );
						if( command.equalsIgnoreCase( "close" ) ) {
							System.out.println( "closing the server" );
							connectionListener.close();
							server.shutdown();
						}
					} );
				} catch( IOException ignored ) {
				}
			}
		} );

		Thread.sleep( 1000 );

		Function< String, Runnable > clientBehaviour = ( command ) -> () -> {
			System.out.println( "Connecting" );
			SerializerChannel_B channel = new SerializerChannel_B(
					KryoSerializer.getInstance(),
					new WrapperByteChannel_B( SocketByteChannel.connect( "localhost", 12345 ) ) );
			System.out.println( channel.< String >com( Unit.id ) );
			channel.com( command );
		};

		client.submit( clientBehaviour.apply( "close" ) );
		client.submit( clientBehaviour.apply( "hello" ) );

		client.shutdown();

	}

}
