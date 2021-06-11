package choral.examples.RetwisChoral;
import choral.channels.SymChannel_B;
import choral.channels.SymChannel_A;
import choral.annotations.Choreography;
import choral.lang.Unit;

@Choreography( role = "Server", name = "Retwis" )
public class Retwis_Server {
	private SymChannel_B < Object > chCS;
	private SymChannel_A < Object > chSR;
	private SessionManager sessionManager;

	public Retwis_Server( SymChannel_B < Object > chCS, SymChannel_A < Object > chSR, Unit cli, Unit databaseConnection, SessionManager sessionManager ) {
		this( chCS, chSR, sessionManager );
	}
	
	public Retwis_Server( SymChannel_B < Object > chCS, SymChannel_A < Object > chSR, SessionManager sessionManager ) {
		this.chCS = chCS;
		this.chSR = chSR;
		this.sessionManager = sessionManager;
	}

	private void posts() {
		String name;
		name = chCS.< String >com( Unit.id );
		Integer page;
		page = chCS.< Integer >com( Unit.id );
		if( checkUser( name ) ){
			chSR.< Result >select( Result.OK );
			chCS.< Result >select( Result.OK );
			chSR.< String >com( name );
			chCS.< Posts >com( chSR.< Posts >com( chSR.< Integer >com( page ) ) );
		} else { 
			chSR.< Result >select( Result.ERROR );
			chCS.< Result >select( Result.ERROR );
		}
	}
	
	private void post() {
		Token token;
		token = chCS.< Token >com( Unit.id );
		String post;
		post = chCS.< String >com( Unit.id );
		if( sessionManager.checkLoggedUser( token ) ){
			chSR.< Result >select( Result.OK );
			chCS.< Result >select( Result.OK );
			Unit.id( chSR.< String >com( sessionManager.getUsernameFromToken( token ) ), chSR.< String >com( post ) );
		} else { 
			chSR.< Result >select( Result.ERROR );
			chCS.< Result >select( Result.ERROR );
		}
	}
	
	private void follow() {
		Token token;
		token = chCS.< Token >com( Unit.id );
		String followTarget;
		followTarget = chCS.< String >com( Unit.id );
		if( sessionManager.checkLoggedUser( token ) ){
			chSR.< Result >select( Result.OK );
			chCS.< Result >select( Result.OK );
			if( checkUser( followTarget ) ){
				chSR.< Result >select( Result.OK );
				chCS.< Result >select( Result.OK );
				String name;
				name = sessionManager.getUsernameFromToken( token );
				if( checkFollow( name, followTarget ) ){
					chSR.< Result >select( Result.OK );
					chCS.< Result >select( Result.OK );
					Unit.id( chSR.< String >com( name ), chSR.< String >com( followTarget ) );
				} else { 
					chSR.< Result >select( Result.ERROR );
					chCS.< Result >select( Result.ERROR );
				}
			} else { 
				chSR.< Result >select( Result.ERROR );
				chCS.< Result >select( Result.ERROR );
			}
		} else { 
			chSR.< Result >select( Result.ERROR );
			chCS.< Result >select( Result.ERROR );
		}
	}
	
	private void stopFollow() {
		Token token;
		token = chCS.< Token >com( Unit.id );
		String stopFollowTarget;
		stopFollowTarget = chCS.< String >com( Unit.id );
		if( sessionManager.checkLoggedUser( token ) ){
			chSR.< Result >select( Result.OK );
			chCS.< Result >select( Result.OK );
			String name;
			name = sessionManager.getUsernameFromToken( token );
			if( checkFollow( name, stopFollowTarget ) ){
				chSR.< Result >select( Result.OK );
				chCS.< Result >select( Result.OK );
				Unit.id( chSR.< String >com( name ), chSR.< String >com( stopFollowTarget ) );
			} else { 
				chSR.< Result >select( Result.ERROR );
				chCS.< Result >select( Result.ERROR );
			}
		} else { 
			chSR.< Result >select( Result.ERROR );
			chCS.< Result >select( Result.ERROR );
		}
	}
	
	private void mentions() {
		Token token;
		token = chCS.< Token >com( Unit.id );
		String mentionsName;
		mentionsName = chCS.< String >com( Unit.id );
		if( checkUser( mentionsName ) ){
			chSR.< Result >select( Result.OK );
			chCS.< Result >select( Result.OK );
			Boolean selfMentions;
			selfMentions = mentionsName.equals( sessionManager.getUsernameFromToken( token ) );
			chCS.< Mentions >com( chSR.< Mentions >com( Unit.id( chSR.< String >com( mentionsName ), chSR.< Boolean >com( selfMentions ) ) ) );
		} else { 
			chSR.< Result >select( Result.ERROR );
			chCS.< Result >select( Result.ERROR );
		}
	}
	
	private void status() {
		String postID;
		postID = chCS.< String >com( Unit.id );
		if( checkPost( postID ) ){
			chSR.< Result >select( Result.OK );
			chCS.< Result >select( Result.OK );
			chCS.< Post >com( chSR.< Post >com( chSR.< String >com( postID ) ) );
		} else { 
			chSR.< Result >select( Result.ERROR );
			chCS.< Result >select( Result.ERROR );
		}
	}
	
	private Boolean checkUser( String name ) {
		return chSR.< Boolean >com( chSR.< String >com( name ) );
	}
	
	private Boolean checkPost( String postID ) {
		return chSR.< Boolean >com( chSR.< String >com( postID ) );
	}
	
	private Boolean checkFollow( String name, String followTarget ) {
		return chSR.< Boolean >com( Unit.id( chSR.< String >com( name ), chSR.< String >com( followTarget ) ) );
	}

}
