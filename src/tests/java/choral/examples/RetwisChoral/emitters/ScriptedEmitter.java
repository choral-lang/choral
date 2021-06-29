package choral.examples.RetwisChoral.emitters;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScriptedEmitter implements Emitter {

	private final Emitter emitter;
	private final CompletableFuture< Void > done;

	private ScriptedEmitter( Emitter emitter ) {
		this.emitter = emitter;
		this.done = new CompletableFuture();
	}

	public static ScriptedEmitter use( Emitter emitter ) {
		return new ScriptedEmitter( emitter );
	}

	public ScriptedEmitter emit( List< Emitter.Action > actions ) {
		ExecutorService actionsSubmitter = Executors.newSingleThreadExecutor();
		actionsSubmitter.submit( () -> {
			actions.forEach( action -> {
				try {
					long wait = (long) ( Math.random() * 5000 );
					System.out.println( "ScriptedEmitter waits: " + wait / 1000 + " seconds" );
					Thread.sleep( wait );
					emitter.emit( action );
				} catch( Exception e ) {
					e.printStackTrace();
				}
			} );
			done.complete( null );
		});
		actionsSubmitter.shutdown();
		return this;
	}

	public void waitForCompletion() {
		try {
			done.get();
		} catch( InterruptedException | ExecutionException e ) {
			e.printStackTrace();
		}
	}

	@Override
	public Emitter emit( Action action ) {
		return this.emit( Collections.singletonList( action ) );
	}
}
