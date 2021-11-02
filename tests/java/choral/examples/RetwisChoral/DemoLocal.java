package choral.examples.RetwisChoral;

import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.choralUnit.testUtils.TestUtils;
import choral.examples.RetwisChoral.emitters.Emitter;
import choral.examples.RetwisChoral.emitters.InMemoryEmitter;
import choral.examples.RetwisChoral.emitters.ScriptedEmitter;
import choral.examples.RetwisChoral.inMemoryImpl.InMemoryCommandInterface;
import choral.examples.RetwisChoral.inMemoryImpl.InMemoryDatabaseConnection;
import choral.examples.RetwisChoral.inMemoryImpl.SimpleSessionManager;
import choral.utils.Pair;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DemoLocal {

	private static void startRepository(
			ExecutorService executor, SymChannel_B< Object > chSR
	) {
		executor.submit( () -> {
			try {
				new Retwis_Repository( chSR, InMemoryDatabaseConnection.instance() ).loop();
				executor.shutdown();
			} catch( Exception e ) {
				e.printStackTrace();
			}
		} );
	}

	private static void startServer(
			ExecutorService executor, SymChannel_B< Object > chCS, SymChannel_A< Object > chSR
	) {
		executor.submit( () -> {
			try {
				new Retwis_Server( chCS, chSR, SimpleSessionManager.instance() ).loop();
				executor.shutdown();
			} catch( Exception e ) {
				e.printStackTrace();
			}
		} );
	}

	public static void main( String[] args ) throws InterruptedException, ExecutionException {

		Token sToken = SimpleSessionManager.instance().createSession( "Save" );
		Token mToken = SimpleSessionManager.instance().createSession( "Marco" );
		Token fToken = SimpleSessionManager.instance().createSession( "Fabrizio" );

		InMemoryDatabaseConnection.instance().addUser( "Save", "pswd" );
		InMemoryDatabaseConnection.instance().addUser( "Marco", "pswd" );
		InMemoryDatabaseConnection.instance().addUser( "Fabrizio", "pswd" );

		Pair< SymChannel_A< Object >, SymChannel_B< Object > > chSR =
				TestUtils.newLocalChannel( "chSR" );
		Pair< SymChannel_A< Object >, SymChannel_B< Object > > chCS =
				TestUtils.newLocalChannel( "chCS" );

		startRepository( Executors.newSingleThreadExecutor(), chSR.right() );
		startServer( Executors.newSingleThreadExecutor(), chCS.right(), chSR.left() );

		InMemoryCommandInterface imci = new InMemoryCommandInterface();
		ScriptedEmitter.use( InMemoryEmitter.use( imci ) )
				.emit( List.of( new Emitter.Post( sToken, "Save" ),
						new Emitter.Follow( fToken, "Save", "Fabrizio" ),
						new Emitter.Follow( sToken, "Marco", "Save" ),
						new Emitter.Posts( "Save", 0 ),
						new Emitter.Logout()
				) );

		new Retwis_Client( chCS.left(), imci ).loop();

		System.out.println( "Loop done, closing" );

	}

}
