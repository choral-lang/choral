package choral.examples.RetwisChoral;

import choral.channels.SymChannel_B;
import choral.annotations.Choreography;
import choral.lang.Unit;

@Choreography( role = "Repository", name = "Retwis" )
public class Retwis_Repository {
	private SymChannel_B < Object > chSR;
	private DatabaseConnection databaseConnection;

	public Retwis_Repository( Unit chCS, SymChannel_B < Object > chSR, Unit commandInterface, DatabaseConnection databaseConnection, Unit sessionManager ) {
		this( chSR, databaseConnection );
	}
	
	public Retwis_Repository( SymChannel_B < Object > chSR, DatabaseConnection databaseConnection ) {
		this.chSR = chSR;
		this.databaseConnection = databaseConnection;
	}

	public void loop() {
		{
			switch( chSR.< RetwisAction >select( Unit.id ) ){
				case POSTS -> {
					posts();
					loop();
				}
				case POST -> {
					post();
					loop();
				}
				case FOLLOW -> {
					follow();
					loop();
				}
				case STOPFOLLOW -> {
					stopFollow();
					loop();
				}
				case MENTIONS -> {
					mentions();
					loop();
				}
				case STATUS -> {
					status();
					loop();
				}
				case LOGOUT -> {
					
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
			}
		}
	}
	
	private void posts() {
		{
			checkUser( Unit.id );
			switch( chSR.< Result >select( Unit.id ) ){
				case OK -> {
					String username = chSR.< String >com( Unit.id );
					chSR.< Posts >com( Posts.of( databaseConnection.getFollowers( username ), databaseConnection.getFollowed( username ), databaseConnection.getPostsPage( username, chSR.< Integer >com( Unit.id ) ) ) );
				}
				case ERROR -> {
					
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
			}
		}
	}
	
	private void post() {
		{
			switch( chSR.< Result >select( Unit.id ) ){
				case OK -> {
					databaseConnection.post( chSR.< String >com( Unit.id ), chSR.< String >com( Unit.id ) );
				}
				case ERROR -> {
					
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
			}
		}
	}
	
	private void follow() {
		{
			switch( chSR.< Result >select( Unit.id ) ){
				case OK -> {
					{
						checkUser( Unit.id );
						switch( chSR.< Result >select( Unit.id ) ){
							case OK -> {
								{
									checkFollow( Unit.id, Unit.id );
									switch( chSR.< Result >select( Unit.id ) ){
										case OK -> {
											databaseConnection.follow( chSR.< String >com( Unit.id ), chSR.< String >com( Unit.id ) );
										}
										case ERROR -> {
											
										}
										default -> {
											throw new RuntimeException( "Received unexpected label from select operation" );
										}
									}
								}
							}
							case ERROR -> {
								
							}
							default -> {
								throw new RuntimeException( "Received unexpected label from select operation" );
							}
						}
					}
				}
				case ERROR -> {
					
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
			}
		}
	}
	
	private void stopFollow() {
		{
			switch( chSR.< Result >select( Unit.id ) ){
				case OK -> {
					{
						checkFollow( Unit.id, Unit.id );
						switch( chSR.< Result >select( Unit.id ) ){
							case OK -> {
								databaseConnection.stopFollow( chSR.< String >com( Unit.id ), chSR.< String >com( Unit.id ) );
							}
							case ERROR -> {
								
							}
							default -> {
								throw new RuntimeException( "Received unexpected label from select operation" );
							}
						}
					}
				}
				case ERROR -> {
					
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
			}
		}
	}
	
	private void mentions() {
		{
			checkUser( Unit.id );
			switch( chSR.< Result >select( Unit.id ) ){
				case OK -> {
					chSR.< Mentions >com( databaseConnection.mentions( chSR.< String >com( Unit.id ), chSR.< Boolean >com( Unit.id ) ) );
				}
				case ERROR -> {
					
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
			}
		}
	}
	
	private void status() {
		{
			checkPost( Unit.id );
			switch( chSR.< Result >select( Unit.id ) ){
				case OK -> {
					chSR.< Post >com( databaseConnection.getPost( chSR.< String >com( Unit.id ) ) );
				}
				case ERROR -> {
					
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
			}
		}
	}
	
	private Unit checkUser( Unit name ) {
		return chSR.< Boolean >com( databaseConnection.isUserValid( chSR.< String >com( Unit.id ) ) );
	}
	
	private Unit checkPost( Unit postID ) {
		return chSR.< Boolean >com( databaseConnection.isPostValid( chSR.< String >com( Unit.id ) ) );
	}
	
	private Unit checkFollow( Unit name, Unit followTarget ) {
		return chSR.< Boolean >com( databaseConnection.isFollower( chSR.< String >com( Unit.id ), chSR.< String >com( Unit.id ) ) );
	}

}
