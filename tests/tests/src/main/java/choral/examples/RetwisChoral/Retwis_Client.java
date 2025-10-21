package choral.examples.RetwisChoral;

import choral.annotations.Choreography;
import choral.lang.Unit;
import choral.channels.SymChannel_A;

@Choreography( role = "Client", name = "Retwis" )
public class Retwis_Client {
	private SymChannel_A < Object > chCS;
	private CommandInterface commandInterface;

	public Retwis_Client( SymChannel_A < Object > chCS, Unit chSR, CommandInterface commandInterface, Unit databaseConnection, Unit sessionManager ) {
		this( chCS, commandInterface );
	}
	
	public Retwis_Client( SymChannel_A < Object > chCS, CommandInterface commandInterface ) {
		this.chCS = chCS;
		this.commandInterface = commandInterface;
	}

	public void loop() {
		switch( commandInterface.action() ){
			case POSTS -> {
				chCS.< RetwisAction >select( RetwisAction.POSTS );
				posts();
				loop();
			}
			case POST -> {
				chCS.< RetwisAction >select( RetwisAction.POST );
				post();
				loop();
			}
			case FOLLOW -> {
				chCS.< RetwisAction >select( RetwisAction.FOLLOW );
				follow();
				loop();
			}
			case STOPFOLLOW -> {
				chCS.< RetwisAction >select( RetwisAction.STOPFOLLOW );
				stopFollow();
				loop();
			}
			case MENTIONS -> {
				chCS.< RetwisAction >select( RetwisAction.MENTIONS );
				mentions();
				loop();
			}
			case STATUS -> {
				chCS.< RetwisAction >select( RetwisAction.STATUS );
				status();
				loop();
			}
			case LOGOUT -> {
				chCS.< RetwisAction >select( RetwisAction.LOGOUT );
			}
		}
	}
	
	private void posts() {
		chCS.< String >com( commandInterface.getPostsUsername() );
		chCS.< Integer >com( commandInterface.getPostsPage() );
		{
			checkUser( Unit.id );
			switch( chCS.< Result >select( Unit.id ) ){
				case OK -> {
					commandInterface.showPosts( chCS.< Posts >com( Unit.id ) );
				}
				case ERROR -> {
					commandInterface.showErrorMessage( "Error, could not find user " + commandInterface.getPostsUsername() + "." );
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
			}
		}
	}
	
	private void post() {
		chCS.< Token >com( commandInterface.getSessionToken() );
		chCS.< String >com( commandInterface.getPost() );
		{
			switch( chCS.< Result >select( Unit.id ) ){
				case OK -> {
					commandInterface.showSuccessMessage( "Tweet posted successfully." );
				}
				case ERROR -> {
					commandInterface.showErrorMessage( "Error, the client is not logged in." );
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
			}
		}
	}
	
	private void follow() {
		chCS.< Token >com( commandInterface.getSessionToken() );
		chCS.< String >com( commandInterface.getFollowTarget() );
		{
			switch( chCS.< Result >select( Unit.id ) ){
				case OK -> {
					{
						checkUser( Unit.id );
						switch( chCS.< Result >select( Unit.id ) ){
							case OK -> {
								{
									checkFollow( Unit.id, Unit.id );
									switch( chCS.< Result >select( Unit.id ) ){
										case OK -> {
											commandInterface.showSuccessMessage( "You now follow " + commandInterface.getFollowTarget() );
										}
										case ERROR -> {
											commandInterface.showErrorMessage( "Error, user " + commandInterface.getUsername() + " already follows " + commandInterface.getFollowTarget() + "." );
										}
										default -> {
											throw new RuntimeException( "Received unexpected label from select operation" );
										}
									}
								}
							}
							case ERROR -> {
								commandInterface.showErrorMessage( "Error, could not find user " + commandInterface.getFollowTarget() + " to follow." );
							}
							default -> {
								throw new RuntimeException( "Received unexpected label from select operation" );
							}
						}
					}
				}
				case ERROR -> {
					commandInterface.showErrorMessage( "Error, the client is not logged in." );
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
			}
		}
	}
	
	private void stopFollow() {
		chCS.< Token >com( commandInterface.getSessionToken() );
		chCS.< String >com( commandInterface.getStopFollowTarget() );
		{
			switch( chCS.< Result >select( Unit.id ) ){
				case OK -> {
					{
						checkFollow( Unit.id, Unit.id );
						switch( chCS.< Result >select( Unit.id ) ){
							case OK -> {
								commandInterface.showSuccessMessage( "You now do not follow " + commandInterface.getStopFollowTarget() + " anymore." );
							}
							case ERROR -> {
								commandInterface.showErrorMessage( "Error, user " + commandInterface.getUsername() + " does not follow " + commandInterface.getStopFollowTarget() + "." );
							}
							default -> {
								throw new RuntimeException( "Received unexpected label from select operation" );
							}
						}
					}
				}
				case ERROR -> {
					commandInterface.showErrorMessage( "Error, the client is not logged in" );
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
			}
		}
	}
	
	private void mentions() {
		chCS.< Token >com( commandInterface.getSessionToken() );
		chCS.< String >com( commandInterface.getMentionsUsername() );
		{
			checkUser( Unit.id );
			switch( chCS.< Result >select( Unit.id ) ){
				case OK -> {
					commandInterface.showMentions( chCS.< Mentions >com( Unit.id ) );
				}
				case ERROR -> {
					commandInterface.showErrorMessage( "Error, could not find user " + commandInterface.getMentionsUsername() + "." );
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
			}
		}
	}
	
	private void status() {
		chCS.< String >com( commandInterface.getStatusPostID() );
		{
			checkPost( Unit.id );
			switch( chCS.< Result >select( Unit.id ) ){
				case OK -> {
					commandInterface.showPost( chCS.< Post >com( Unit.id ) );
				}
				case ERROR -> {
					commandInterface.showErrorMessage( "Error, could not find post with ID " + commandInterface.getStatusPostID() + "." );
				}
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
			}
		}
	}
	
	private Unit checkUser( Unit name ) {
		return Unit.id;
	}
	
	private Unit checkPost( Unit postID ) {
		return Unit.id;
	}
	
	private Unit checkFollow( Unit name, Unit followTarget ) {
		return Unit.id;
	}

}
