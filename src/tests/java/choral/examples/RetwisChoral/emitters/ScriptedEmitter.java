package choral.examples.RetwisChoral.emitters;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScriptedEmitter {

	private final Emitter emitter;

	private ScriptedEmitter( Emitter emitter ) {
		this.emitter = emitter;
	}

	public static ScriptedEmitter use( Emitter emitter ) {
		return new ScriptedEmitter( emitter );
	}

	public void start( List< Emitter.Action > actions ) {
		ExecutorService actionsSubmitter = Executors.newSingleThreadExecutor();
		actionsSubmitter.submit( () -> {
			try {
				while( !actions.isEmpty() ) {
					long wait = (long) ( Math.random() * 5000 );
					System.out.println( "ScriptedEmitter waits: " + wait / 1000 + " seconds" );
					Thread.sleep( wait );
					emitter.emit( actions.remove( 0 ) );
				}
				actionsSubmitter.shutdown();
			} catch( Exception e ) {
				e.printStackTrace();
			}
		} );
	}
}
