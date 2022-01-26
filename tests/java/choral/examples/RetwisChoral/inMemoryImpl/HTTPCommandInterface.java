package choral.examples.RetwisChoral.inMemoryImpl;

import choral.examples.RetwisChoral.*;
import choral.examples.RetwisChoral.emitters.Emitter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.checkerframework.checker.units.qual.C;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class HTTPCommandInterface implements CommandInterface {

	private final List< CompletableFuture< Emitter.Action > > actions;
	private final HttpServer httpServer;
	private Emitter.Action currentAction;
	private ResponseMessage currentResponseMessage;
	private final List< CompletableFuture< ResponseMessage > > actionResponses;

	public HTTPCommandInterface( InetSocketAddress socketAddress ) throws IOException {
		actions = new LinkedList<>();
		actionResponses = new LinkedList<>();
		httpServer = HttpServer.create();
		httpServer.bind( socketAddress, 10 );
		addContexts();
		httpServer.start();
	}

	public void addContexts() {
		httpServer.createContext( "/post", exchange -> {
			Headers headers = exchange.getRequestHeaders();
			Emitter.Action action = new Emitter.Post(
					new Token( headers.getFirst( Emitter.Action.Fields.sessionToken.name() ) ),
					headers.getFirst( Emitter.Action.Fields.post.name() )
			);
			addAction( action );
			handleResponse( exchange );
		} );
		httpServer.createContext( "/posts", exchange -> {
			Headers headers = exchange.getRequestHeaders();
			Emitter.Action action = new Emitter.Posts(
					headers.getFirst( Emitter.Action.Fields.postsUsername.name() ),
					Integer.parseInt( headers.getFirst( Emitter.Action.Fields.postsPage.name() ) )
			);
			addAction( action );
			handleResponse( exchange );
		} );
		httpServer.createContext( "/follow", exchange -> {
			Headers headers = exchange.getRequestHeaders();
			Emitter.Action action = new Emitter.Follow(
					new Token( headers.getFirst( Emitter.Action.Fields.sessionToken.name() ) ),
					headers.getFirst( Emitter.Action.Fields.followTarget.name() ),
					headers.getFirst( Emitter.Action.Fields.username.name() )
			);
			addAction( action );
			handleResponse( exchange );
		} );
		httpServer.createContext( "/stopfollow", exchange -> {
			Headers headers = exchange.getRequestHeaders();
			Emitter.Action action = new Emitter.StopFollow(
					new Token( headers.getFirst( Emitter.Action.Fields.sessionToken.name() ) ),
					headers.getFirst( Emitter.Action.Fields.stopFollowTarget.name() ),
					headers.getFirst( Emitter.Action.Fields.username.name() )
			);
			addAction( action );
			handleResponse( exchange );
		} );
		httpServer.createContext( "/logout", exchange -> {
			Emitter.Action action = new Emitter.Logout();
			addAction( action );
			handleResponse( exchange );
		} );
		httpServer.createContext( "/mentions", exchange -> {
			Headers headers = exchange.getRequestHeaders();
			Emitter.Action action = new Emitter.Mentions(
					new Token( headers.getFirst( Emitter.Action.Fields.sessionToken.name() ) ),
					headers.getFirst( Emitter.Action.Fields.mentionsUsername.name() )
			);
			addAction( action );
			handleResponse( exchange );
		} );
		httpServer.createContext( "/status", exchange -> {
			Headers headers = exchange.getRequestHeaders();
			Emitter.Action action = new Emitter.Status(
					headers.getFirst( Emitter.Action.Fields.statusPostID.name() )
			);
			addAction( action );
			handleResponse( exchange );
		} );
	}

	private void handleResponse( HttpExchange exchange ) {
		try {
			CompletableFuture< ResponseMessage > frm = new CompletableFuture<>();
			synchronized( actionResponses ) {
				actionResponses.add( frm );
			}
			ResponseMessage responseMessage = frm.get();
			synchronized( actionResponses ) {
				actionResponses.remove( 0 );
			}
			String response = String.join( "\n", responseMessage.messages );
			exchange.sendResponseHeaders(
					responseMessage.isError() ? 500 : 200,
					response.getBytes( StandardCharsets.UTF_8 ).length );
			OutputStream os = exchange.getResponseBody();
			os.write( response.getBytes( StandardCharsets.UTF_8 ) );
			os.flush();
			os.close();
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	public void stop() {
		synchronized( actionResponses ) {
			actionResponses.get( 0 ).complete( currentResponseMessage );
		}
		httpServer.stop( 1000 );
	}

	boolean firstLoop = true;

	@Override
	public RetwisAction action() {
		if( firstLoop ) {
			firstLoop = false;
		} else {
			synchronized( actionResponses ) {
				actionResponses.get( 0 ).complete( currentResponseMessage );
			}
		}
		currentResponseMessage = new ResponseMessage();
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

	// OUTPUT METHODS

	@Override
	public void showPosts( Posts posts ) {
		currentResponseMessage.add( posts.toString() );
	}

	@Override
	public void showPost( Post post ) {
		currentResponseMessage.add( post.toString() );
	}

	@Override
	public void showErrorMessage( String message ) {
		currentResponseMessage.add( message, true );
	}

	@Override
	public void showSuccessMessage( String message ) {
		currentResponseMessage.add( message );
	}

	@Override
	public void showMentions( Mentions mentions ) {
		currentResponseMessage.add( mentions.toString() );
	}


	private static class ResponseMessage {

		boolean isError;
		List< String > messages;

		private ResponseMessage() {
			isError = false;
			messages = new LinkedList<>();
		}

		void add( String message, boolean isError ) {
			this.isError |= isError;
			messages.add( message );
		}

		void add( String message ) {
			add( message, false );
		}

		public boolean isError() {
			return isError;
		}

		public List< String > messages() {
			return messages;
		}
	}

}
