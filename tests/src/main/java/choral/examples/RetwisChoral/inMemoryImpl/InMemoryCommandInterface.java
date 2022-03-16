package choral.examples.RetwisChoral.inMemoryImpl;

import choral.examples.RetwisChoral.*;
import choral.examples.RetwisChoral.emitters.Emitter;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class InMemoryCommandInterface implements CommandInterface {

	private final List< CompletableFuture< Emitter.Action > > actions;
	private Emitter.Action currentAction;

	public InMemoryCommandInterface() {
		actions = new LinkedList<>();
	}

	@Override
	public RetwisAction action() {
		CompletableFuture< Emitter.Action > cc;
		synchronized( actions ) {
			if( !actions.isEmpty() && actions.get( 0 ).isDone() ) {
				cc = actions.remove( 0 );
			} else {
				cc = new CompletableFuture<>();
				actions.add( cc );
			}
		}
		try {
			currentAction = cc.get();
		} catch( InterruptedException | ExecutionException e ) {
			e.printStackTrace();
		}
		return currentAction.action();
	}

	public void addAction( Emitter.Action action ) {
		synchronized( actions ) {
			if( actions.isEmpty() || actions.get( 0 ).isDone() ) {
				CompletableFuture< Emitter.Action > cc = new CompletableFuture<>();
				cc.complete( action );
				actions.add( cc );
			} else {
				actions.remove( 0 ).complete( action );
			}
		}
	}

	@Override
	public String getPostsUsername() {
		return currentAction.postsUsername();
	}

	@Override
	public String getUsername() {
		return currentAction.username();
	}

	@Override
	public Integer getPostsPage() {
		return currentAction.postsPage();
	}

	@Override
	public void showPosts( Posts posts ) {
		System.out.println( posts );
	}

	@Override
	public void showPost( Post post ) {
		System.out.println( post );
	}

	@Override
	public void showErrorMessage( String message ) {
		System.err.println( message );
	}

	@Override
	public void showSuccessMessage( String message ) {
		System.out.println( message );
	}

	@Override
	public Token getSessionToken() {
		return currentAction.sessionToken();
	}

	@Override
	public String getPost() {
		return currentAction.post();
	}

	@Override
	public String getFollowTarget() {
		return currentAction.followTarget();
	}

	@Override
	public void showMentions( Mentions mentions ) {
		System.out.println( mentions );
	}

	@Override
	public String getStatusPostID() {
		return currentAction.statusPostID();
	}

	@Override
	public String promptPassword() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getMentionsUsername() {
		return currentAction.mentionsUsername();
	}

	@Override
	public String getStopFollowTarget() {
		return currentAction.stopFollowTarget();
	}

}
