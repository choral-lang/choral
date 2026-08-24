package choral.examples.RetwisChoral;

import choral.annotations.Choreography;
import choral.channels.SymChannel_B;
import choral.channels.SymChannel_A;
import choral.lang.Unit;

@Choreography( role = "Server", name = "RetwisLoginManager" )
public class RetwisLoginManager_Server {
	private SymChannel_B< Object > chCS;
	private SymChannel_A< Object > chSR;
	private SessionManager sessionManager;

	public RetwisLoginManager_Server(
			SymChannel_B< Object > chCS, SymChannel_A< Object > chSR, Unit cli, Unit db,
			SessionManager sessionManager
	) {
		this( chCS, chSR, sessionManager );
	}

	public RetwisLoginManager_Server(
			SymChannel_B< Object > chCS, SymChannel_A< Object > chSR, SessionManager sessionManager
	) {
		this.chCS = chCS;
		this.chSR = chSR;
		this.sessionManager = sessionManager;
	}

	public Unit main( Unit action ) {
		return main();
	}

	public Unit signUp() {
		String name;
		name = chCS.< String >com( Unit.id );
		Boolean isValidUsername;
		isValidUsername = chSR.< Boolean >com( chSR.< String >com( name ) );
		if( isValidUsername ) {
			chSR.< Result >select( Result.OK );
			chCS.< Result >select( Result.OK );
			chSR.< String >com( chCS.< String >com( Unit.id ) );
			chSR.< String >com( name );
			return chCS.< Token >com( sessionManager.createSession( name ) );
		} else {
			chSR.< Result >select( Result.ERROR );
			chCS.< Result >select( Result.ERROR );
			return Unit.id;
		}
	}

	public Unit signIn() {
		String username;
		username = chCS.< String >com( Unit.id );
		chSR.< String >com( username );
		chSR.< String >com( chCS.< String >com( Unit.id ) );
		{
			switch( chSR.< Result >select( Unit.id ) ) {
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case ERROR -> {
					chCS.< Result >select( Result.ERROR );
					return Unit.id;
				}
				case OK -> {
					chCS.< Result >select( Result.OK );
					return chCS.< Token >com( sessionManager.createSession( username ) );
				}
			}
		}
	}

	public void logout() {
		sessionManager.closeSession( chCS.< Token >com( Unit.id ) );
	}

	public Unit main() {
		{
			switch( chCS.< LoginAction >select( Unit.id ) ) {
				case SIGNUP -> {
					chSR.< LoginAction >select( LoginAction.SIGNUP );
					return signUp();
				}
				case LOGOUT -> {
					chSR.< LoginAction >select( LoginAction.LOGOUT );
					logout();
					return Unit.id;
				}
				case SIGNIN -> {
					chSR.< LoginAction >select( LoginAction.SIGNIN );
					return signIn();
				}
			}
		}
		return Unit.id;
	}

}
