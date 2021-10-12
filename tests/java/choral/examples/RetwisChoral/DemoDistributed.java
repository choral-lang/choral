package choral.examples.RetwisChoral;

import choral.examples.RetwisChoral.emitters.Emitter;
import choral.examples.RetwisChoral.inMemoryImpl.InMemoryCommandInterface;
import choral.examples.RetwisChoral.emitters.InMemoryEmitter;
import choral.examples.RetwisChoral.emitters.ScriptedEmitter;
import choral.examples.RetwisChoral.inMemoryImpl.InMemoryDatabaseConnection;
import choral.examples.RetwisChoral.inMemoryImpl.SimpleSessionManager;
import choral.runtime.Media.ServerSocketByteChannel;
import choral.runtime.Media.SocketByteChannel;
import choral.runtime.SerializerChannel.SerializerChannel_A;
import choral.runtime.SerializerChannel.SerializerChannel_B;
import choral.runtime.Serializers.KryoSerializer;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_A;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_B;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DemoDistributed {

	private static final int SERVER_PORT = 12345;
	private static final int REPOSITORY_PORT = 54321;

	private static CompletableFuture< Void > bootstrapRepository(
			ExecutorService executor, ServerSocketByteChannel listener
	) {
		CompletableFuture< Void > f = new CompletableFuture<>();
		executor.submit( () -> {
			System.out.println( "Repository accepting connections" );
			while( listener.isOpen() ) {
				if( !f.isDone() ) {
					f.complete( null );
				}
				try {
					SerializerChannel_B chSR = new SerializerChannel_B(
							KryoSerializer.getInstance(),
							new WrapperByteChannel_B( listener.getNext() ) );
					executor.submit( () -> {
						try {
							new Retwis_Repository( chSR,
									InMemoryDatabaseConnection.instance() ).loop();
						} catch( Exception e ) {
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
				if( !f.isDone() ) {
					f.complete( null );
				}
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
						} catch( Exception e ) {
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

		Token sToken = SimpleSessionManager.instance().createSession( "Save" );
		Token mToken = SimpleSessionManager.instance().createSession( "Marco" );
		Token fToken = SimpleSessionManager.instance().createSession( "Fabrizio" );

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

		InMemoryCommandInterface imci = new InMemoryCommandInterface();
		ScriptedEmitter.use( InMemoryEmitter.use( imci ) )
				.emit( new LinkedList<>( List.of(
						new Emitter.Post( sToken, "Save" ),
						new Emitter.Follow( fToken, "Save", "Fabrizio" ),
						new Emitter.Follow( sToken, "Marco", "Save" ),
						new Emitter.Posts( "Save", 0 ),
						new Emitter.Logout()
				) ) );

		new Retwis_Client( chCS, imci ).loop();

		System.out.println( "Loop done, closing" );

		repositoryListener.close();
		serverListener.close();
	}

}
