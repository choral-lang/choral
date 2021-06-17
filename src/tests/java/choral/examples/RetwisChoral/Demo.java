package choral.examples.RetwisChoral;

import choral.runtime.Media.ServerSocketByteChannel;
import choral.runtime.Media.SocketByteChannel;
import choral.runtime.SerializerChannel.SerializerChannel_A;
import choral.runtime.SerializerChannel.SerializerChannel_B;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_A;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_B;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Demo {

	private static final int SERVER_PORT = 12345;
	private static final int REPOSITORY_PORT = 54321;

	private static void bootstrapRepository(
			ExecutorService executor, ServerSocketByteChannel listener
	) {
		executor.submit( () -> {
			System.out.println( "Repository accepting connections" );
			while( listener.isOpen() ) {
				try {
					SerializerChannel_B channel = new SerializerChannel_B(
							KryoSerializer.getInstance(),
							new WrapperByteChannel_B( listener.getNext() ) );
					executor.submit( () -> {
						new Retwis_Repository( channel, InMemoryDatabaseConnection.instance() ).loop();
					} );
				} catch( IOException ignored ) {
				}
			}
			executor.shutdown();
		} );
	}

	private static void bootstrapServer(
			ExecutorService executor, ServerSocketByteChannel listener
	) {
		executor.submit( () -> {
			System.out.println( "Server accepting connections" );
			while( listener.isOpen() ) {
				try {
					SerializerChannel_B chCS = new SerializerChannel_B(
							KryoSerializer.getInstance(),
							new WrapperByteChannel_B( listener.getNext() ) );
					executor.submit( () -> {
						SerializerChannel_A chSR = new SerializerChannel_A(
								KryoSerializer.getInstance(),
								new WrapperByteChannel_A(
										SocketByteChannel.connect( "localhost", REPOSITORY_PORT ) )
						);
						new Retwis_Server( chCS, chSR, SimpleSessionManager.instance() ).loop();
					} );
				} catch( IOException ignored ) {
				}
			}
			executor.shutdown();
		} );
	}

	public static void main( String[] args ) throws InterruptedException {

		Token token = SimpleSessionManager.instance().createSession( "Save" );
		InMemoryDatabaseConnection.instance().addUser( "Save", "pswd" );
		InMemoryDatabaseConnection.instance().post( "Save", "This is a simple post." );

		ServerSocketByteChannel repositoryListener = ServerSocketByteChannel
				.at( "localhost", REPOSITORY_PORT );
		bootstrapRepository( Executors.newCachedThreadPool(), repositoryListener );
		ServerSocketByteChannel serverListener = ServerSocketByteChannel
				.at( "localhost", SERVER_PORT );
		bootstrapServer( Executors.newCachedThreadPool(), serverListener );

		Thread.sleep( 1000 );

		System.out.println( "Connecting client" );

		new Retwis_Client(
				new SerializerChannel_A(
						KryoSerializer.getInstance(),
						new WrapperByteChannel_A(
								SocketByteChannel.connect( "localhost", SERVER_PORT ) )
				),
				new ScriptedCLI().addSession( "Save", token )
		).loop();

		System.out.println( "Loop done, closing" );

		repositoryListener.close();
		serverListener.close();
	}

}
