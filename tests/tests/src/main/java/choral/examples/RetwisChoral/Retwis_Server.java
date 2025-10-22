package choral.examples.RetwisChoral;

import choral.annotations.Choreography;
import choral.lang.Unit;
import choral.channels.SymChannel_B;
import choral.channels.SymChannel_A;

@Choreography( role = "Server", name = "Retwis" )
public class Retwis_Server {
	private SymChannel_B < Object > chCS;
	private SymChannel_A < Object > chSR;
	private SessionManager sessionManager;

	public Retwis_Server( SymChannel_B < Object > chCS, SymChannel_A < Object > chSR, Unit commandInterface, Unit databaseConnection, SessionManager sessionManager ) {
		this( chCS, chSR, sessionManager );
	}
	
	public Retwis_Server( SymChannel_B < Object > chCS, SymChannel_A < Object > chSR, SessionManager sessionManager ) {
		this.chCS = chCS;
		this.chSR = chSR;
		this.sessionManager = sessionManager;
	}

	public void loop() {
		{
			switch( chCS.< RetwisAction >select( Unit.id ) ){
				case POSTS -> {
					chSR.< RetwisAction >select( RetwisAction.POSTS );
					posts();
					loop();
				}
				case POST -> {
					chSR.< RetwisAction >select( RetwisAction.POST );
					post();
					loop();
				}
				case FOLLOW -> {
					chSR.< RetwisAction >select( RetwisAction.FOLLOW );
					follow();
					loop();
				}
				case STOPFOLLOW -> {
					chSR.< RetwisAction >select( RetwisAction.STOPFOLLOW );
					stopFollow();
					loop();
				}
				case MENTIONS -> {
					chSR.< RetwisAction >select( RetwisAction.MENTIONS );
					mentions();
					loop();
				}
				case STATUS -> {
					chSR.< RetwisAction >select( RetwisAction.STATUS );
					status();
					loop();
				}
				case LOGOUT -> {
					chSR.< RetwisAction >select( RetwisAction.LOGOUT );
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
			}
		}
	}
	
	private void posts() {
		String name = chCS.< String >com( Unit.id );
		Integer page = chCS.< Integer >com( Unit.id );
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
		Token token = chCS.< Token >com( Unit.id );
		String post = chCS.< String >com( Unit.id );
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
		Token token = chCS.< Token >com( Unit.id );
		String followTarget = chCS.< String >com( Unit.id );
		if( sessionManager.checkLoggedUser( token ) ){
			chSR.< Result >select( Result.OK );
			chCS.< Result >select( Result.OK );
			if( checkUser( followTarget ) ){
				chSR.< Result >select( Result.OK );
				chCS.< Result >select( Result.OK );
				String name = sessionManager.getUsernameFromToken( token );
				if( !checkFollow( name, followTarget ) ){
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
		Token token = chCS.< Token >com( Unit.id );
		String stopFollowTarget = chCS.< String >com( Unit.id );
		if( sessionManager.checkLoggedUser( token ) ){
			chSR.< Result >select( Result.OK );
			chCS.< Result >select( Result.OK );
			String name = sessionManager.getUsernameFromToken( token );
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
		Token token = chCS.< Token >com( Unit.id );
		String mentionsName = chCS.< String >com( Unit.id );
		if( checkUser( mentionsName ) ){
			chSR.< Result >select( Result.OK );
			chCS.< Result >select( Result.OK );
			Boolean selfMentions = mentionsName.equals( sessionManager.getUsernameFromToken( token ) );
			chCS.< Mentions >com( chSR.< Mentions >com( Unit.id( chSR.< String >com( mentionsName ), chSR.< Boolean >com( selfMentions ) ) ) );
		} else { 
			chSR.< Result >select( Result.ERROR );
			chCS.< Result >select( Result.ERROR );
		}
	}
	
	private void status() {
		String postID = chCS.< String >com( Unit.id );
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
