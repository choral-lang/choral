package choral.examples.RetwisChoral;

import choral.runtime.Media.ServerSocketByteChannel;
import choral.runtime.Media.SocketByteChannel;
import choral.runtime.SerializerChannel.SerializerChannel_A;
import choral.runtime.SerializerChannel.SerializerChannel_B;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_A;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_B;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Demo {

	private static final int SERVER_PORT = 12345;
	private static final int REPOSITORY_PORT = 54321;

	private static CompletableFuture< Void > bootstrapRepository(
			ExecutorService executor, ServerSocketByteChannel listener
	) {
		CompletableFuture< Void > f = new CompletableFuture<>();
		executor.submit( () -> {
			System.out.println( "Repository accepting connections" );
			while( listener.isOpen() ) {
				if( ! f.isDone() ){ f.complete( null ); }
				try {
					SerializerChannel_B chSR = new SerializerChannel_B(
							KryoSerializer.getInstance(),
							new WrapperByteChannel_B( listener.getNext() ) );
					executor.submit( () -> {
						try {
							new Retwis_Repository( chSR, InMemoryDatabaseConnection.instance() ).loop();
						} catch( Exception e ){
							e.printStackTrace();
						}
					} );
				} catch( IOException ignored ) {
				}
			}
			executor.shutdown();
		} );
		return f;
	}

	private static CompletableFuture< Void > bootstrapServer(
			ExecutorService executor, ServerSocketByteChannel listener
	) {
		CompletableFuture< Void > f = new CompletableFuture<>();
		executor.submit( () -> {
			System.out.println( "Server accepting connections" );
			while( listener.isOpen() ) {
				if( ! f.isDone() ){ f.complete( null ); }
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
						try {
							new Retwis_Server( chCS, chSR, SimpleSessionManager.instance() ).loop();
						} catch( Exception e ){
							e.printStackTrace();
						}
					} );
				} catch( IOException ignored ) {
				}
			}
			executor.shutdown();
		} );
		return f;
	}

	public static void main( String[] args ) throws InterruptedException, ExecutionException {

		Token t1 = SimpleSessionManager.instance().createSession( "Save" );
		Token t2 = SimpleSessionManager.instance().createSession( "Marco" );
		Token t3 = SimpleSessionManager.instance().createSession( "Fabrizio" );

		InMemoryDatabaseConnection.instance().addUser( "Save", "pswd" );
		InMemoryDatabaseConnection.instance().addUser( "Marco", "pswd" );
		InMemoryDatabaseConnection.instance().addUser( "Fabrizio", "pswd" );

		ServerSocketByteChannel repositoryListener = ServerSocketByteChannel
				.at( "localhost", REPOSITORY_PORT );
		bootstrapRepository( Executors.newCachedThreadPool(), repositoryListener ).get();
		ServerSocketByteChannel serverListener = ServerSocketByteChannel
				.at( "localhost", SERVER_PORT );
		bootstrapServer( Executors.newCachedThreadPool(), serverListener ).get();


		System.out.println( "Connecting client" );

		SerializerChannel_A chCS = new SerializerChannel_A(
				KryoSerializer.getInstance(),
				new WrapperByteChannel_A(
						SocketByteChannel.connect( "localhost", SERVER_PORT ) )
		);

		new Retwis_Client(
				chCS,
				new ScriptedCLI()
						.addSession( "Save", t1 )
						.addSession( "Marco", t2 )
						.addSession( "Fabrizio", t3 )
		).loop();

		System.out.println( "Loop done, closing" );

		repositoryListener.close();
		serverListener.close();
	}

}
