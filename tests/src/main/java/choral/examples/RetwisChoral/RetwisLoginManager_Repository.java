package choral.examples.RetwisChoral;

import choral.annotations.Choreography;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "Repository", name = "RetwisLoginManager" )
public class RetwisLoginManager_Repository {
	private SymChannel_B< Object > chSR;
	private DatabaseConnection db;

	public RetwisLoginManager_Repository(
			Unit chCS, SymChannel_B< Object > chSR, Unit cli, DatabaseConnection db,
			Unit sessionManager
	) {
		this( chSR, db );
	}

	public RetwisLoginManager_Repository( SymChannel_B< Object > chSR, DatabaseConnection db ) {
		this.chSR = chSR;
		this.db = db;
	}

	public Unit main( Unit action ) {
		return main();
	}

	public Unit signUp() {
		chSR.< Boolean >com( db.isUserValid( chSR.< String >com( Unit.id ) ) );
		{
			switch( chSR.< Result >select( Unit.id ) ) {
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case ERROR -> {
					return Unit.id;
				}
				case OK -> {
					String pswd;
					pswd = chSR.< String >com( Unit.id );
					db.addUser( chSR.< String >com( Unit.id ), pswd );
					return Unit.id;
				}
			}
		}
	}

	public Unit signIn() {
		String name;
		name = chSR.< String >com( Unit.id );
		String pswd;
		pswd = chSR.< String >com( Unit.id );
		if( db.auth( name, pswd ) ) {
			chSR.< Result >select( Result.OK );
			return Unit.id;
		} else {
			chSR.< Result >select( Result.ERROR );
			return Unit.id;
		}
	}

	public void logout() {

	}

	public Unit main() {
		{
			switch( chSR.< LoginAction >select( Unit.id ) ) {
				case SIGNUP -> {
					return signUp();
				}
				case LOGOUT -> {
					logout();
					return Unit.id;
				}
				case SIGNIN -> {
					return signIn();
				}
			}
		}
		return Unit.id;
	}

}
