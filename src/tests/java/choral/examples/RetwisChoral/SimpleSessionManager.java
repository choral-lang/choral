package choral.examples.RetwisChoral;

import java.util.HashMap;
import java.util.UUID;

public class SimpleSessionManager implements SessionManager {

	private final static SimpleSessionManager INSTANCE = new SimpleSessionManager();
	private final HashMap< Token, String > sessions;

	private SimpleSessionManager(){
		this.sessions = new HashMap();
	}

	public static SimpleSessionManager instance() {
		return INSTANCE;
	}

	@Override
	public Token createSession( String name ) {
		Token token = new Token( UUID.randomUUID().toString() );
		sessions.put( token, name );
		return token;
	}

	@Override
	public void closeSession( Token token ) {
		sessions.remove( token );
	}

	@Override
	public Boolean checkLoggedUser( Token token ) {
		return sessions.containsKey( token );
	}

	@Override
	public String getUsernameFromToken( Token token ) {
		return sessions.get( token );
	}
}
