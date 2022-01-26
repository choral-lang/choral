package choral.examples.RetwisChoral.emitters;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static choral.examples.RetwisChoral.emitters.Emitter.Action.Fields;

public class HTTPEmitter implements Emitter {

	private final InetSocketAddress serverAddress;

	private HTTPEmitter( InetSocketAddress serverAddress ) {
		this.serverAddress = serverAddress;
	}

	public static HTTPEmitter use( InetSocketAddress serverAddress ) {
		return new HTTPEmitter( serverAddress );
	}

	private String getServerPrefix() {
		return "http://" + serverAddress.getHostName() + ":" + serverAddress.getPort();
	}

	@Override
	public Emitter emit( Action action ) {
		try {
			HttpRequest request = null;
			switch( action.action() ) {
				case POSTS -> {
					request = HttpRequest.newBuilder()
							.GET()
							.uri( URI.create( getServerPrefix() + "/posts" ) )
							.header( Fields.postsUsername.name(), action.postsUsername() )
							.header( Fields.postsPage.name(), action.postsPage().toString() )
							.build();
				}
				case POST -> {
					request = HttpRequest.newBuilder()
							.POST( HttpRequest.BodyPublishers.noBody() )
							.uri( URI.create( getServerPrefix() + "/post" ) )
							.header( Fields.sessionToken.name(), action.sessionToken().id() )
							.header( Fields.post.name(), action.post() )
							.build();
				}
				case FOLLOW -> {
					request = HttpRequest.newBuilder()
							.POST( HttpRequest.BodyPublishers.noBody() )
							.uri( URI.create( getServerPrefix() + "/follow" ) )
							.header( Fields.sessionToken.name(), action.sessionToken().id() )
							.header( Fields.followTarget.name(), action.followTarget() )
							.header( Fields.username.name(), action.username() )
							.build();
				}
				case STOPFOLLOW -> {
					request = HttpRequest.newBuilder()
							.POST( HttpRequest.BodyPublishers.noBody() )
							.uri( URI.create( getServerPrefix() + "/stopfollow" ) )
							.header( Fields.sessionToken.name(), action.sessionToken().id() )
							.header( Fields.stopFollowTarget.name(), action.followTarget() )
							.header( Fields.username.name(), action.username() )
							.build();
				}
				case MENTIONS -> {
					request = HttpRequest.newBuilder()
							.POST( HttpRequest.BodyPublishers.noBody() )
							.uri( URI.create( getServerPrefix() + "/mentions" ) )
							.header( Fields.sessionToken.name(), action.sessionToken().id() )
							.header( Fields.mentionsUsername.name(), action.mentionsUsername() )
							.build();
				}
				case STATUS -> {
					request = HttpRequest.newBuilder()
							.POST( HttpRequest.BodyPublishers.noBody() )
							.uri( URI.create( getServerPrefix() + "/status" ) )
							.header( Fields.statusPostID.name(), action.statusPostID() )
							.build();
				}
				case LOGOUT -> {
					request = HttpRequest.newBuilder()
							.POST( HttpRequest.BodyPublishers.noBody() )
							.uri( URI.create( getServerPrefix() + "/logout" ) )
							.build();
				}
			}
			HttpClient client = HttpClient.newBuilder()
					.version( HttpClient.Version.HTTP_1_1 )
					.build();
			HttpResponse< String > response = null;
			response = client.send( request,
					HttpResponse.BodyHandlers.ofString( StandardCharsets.UTF_8 ) );
			System.out.println( "HTTP_EMITTER Received response: "
					+ response + "\n"
					+ response.body() );
		} catch( IOException | InterruptedException e ) {
			e.printStackTrace();
		}
		return this;
	}
}
