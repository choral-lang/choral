package choral.examples.RetwisChoral;

import choral.annotations.Choreography;
import choral.lang.Unit;
import choral.channels.SymChannel_A;

@Choreography( role = "Client", name = "Retwis" )
public class Retwis_Client {
	private SymChannel_A< Object > chCS;
	private CommandInterface commandInterface;

	public Retwis_Client(
			SymChannel_A< Object > chCS, Unit chSR, CommandInterface commandInterface,
			Unit databaseConnection, Unit sessionManager
	) {
		this( chCS, commandInterface );
	}

	public Retwis_Client( SymChannel_A< Object > chCS, CommandInterface commandInterface ) {
		this.chCS = chCS;
		this.commandInterface = commandInterface;
	}

	public void loop() {
		switch( commandInterface.action() ) {
			case STOPFOLLOW -> {
				chCS.< RetwisAction >select( RetwisAction.STOPFOLLOW );
				stopFollow();
				loop();
			}
			case FOLLOW -> {
				chCS.< RetwisAction >select( RetwisAction.FOLLOW );
				follow();
				loop();
			}
			case LOGOUT -> {
				chCS.< RetwisAction >select( RetwisAction.LOGOUT );
			}
			case STATUS -> {
				chCS.< RetwisAction >select( RetwisAction.STATUS );
				status();
				loop();
			}
			case POST -> {
				chCS.< RetwisAction >select( RetwisAction.POST );
				post();
				loop();
			}
			case MENTIONS -> {
				chCS.< RetwisAction >select( RetwisAction.MENTIONS );
				mentions();
				loop();
			}
			case POSTS -> {
				chCS.< RetwisAction >select( RetwisAction.POSTS );
				posts();
				loop();
			}
		}
	}

	private void posts() {
		chCS.< String >com( commandInterface.getPostsUsername() );
		chCS.< Integer >com( commandInterface.getPostsPage() );
		{
			checkUser( Unit.id );
			switch( chCS.< Result >select( Unit.id ) ) {
				case ERROR -> {
					commandInterface.showErrorMessage(
							"Error, could not find user " + commandInterface.getPostsUsername() + "." );
				}
				case OK -> {
					commandInterface.showPosts( chCS.< Posts >com( Unit.id ) );
				}
			}
		}
	}

	private void post() {
		chCS.< Token >com( commandInterface.getSessionToken() );
		chCS.< String >com( commandInterface.getPost() );
		{
			switch( chCS.< Result >select( Unit.id ) ) {
				case ERROR -> {
					commandInterface.showErrorMessage( "Error, the client is not logged in." );
				}
				case OK -> {
					commandInterface.showSuccessMessage( "Tweet posted successfully." );
				}
			}
		}
	}

	private void follow() {
		chCS.< Token >com( commandInterface.getSessionToken() );
		chCS.< String >com( commandInterface.getFollowTarget() );
		{
			switch( chCS.< Result >select( Unit.id ) ) {
				case ERROR -> {
					commandInterface.showErrorMessage( "Error, the client is not logged in." );
				}
				case OK -> {
					{
						checkUser( Unit.id );
						switch( chCS.< Result >select( Unit.id ) ) {
							case ERROR -> {
								commandInterface.showErrorMessage(
										"Error, could not find user " + commandInterface.getFollowTarget() + " to follow." );
							}
							case OK -> {
								{
									checkFollow( Unit.id, Unit.id );
									switch( chCS.< Result >select( Unit.id ) ) {
										case ERROR -> {
											commandInterface.showErrorMessage(
													"Error, user " + commandInterface.getUsername() + " already follows " + commandInterface.getFollowTarget() + "." );
										}
										case OK -> {
											commandInterface.showSuccessMessage(
													"You now follow " + commandInterface.getFollowTarget() );
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private void stopFollow() {
		chCS.< Token >com( commandInterface.getSessionToken() );
		chCS.< String >com( commandInterface.getStopFollowTarget() );
		{
			switch( chCS.< Result >select( Unit.id ) ) {
				case ERROR -> {
					commandInterface.showErrorMessage( "Error, the client is not logged in" );
				}
				case OK -> {
					{
						checkFollow( Unit.id, Unit.id );
						switch( chCS.< Result >select( Unit.id ) ) {
							case ERROR -> {
								commandInterface.showErrorMessage(
										"Error, user " + commandInterface.getUsername() + " does not follow " + commandInterface.getStopFollowTarget() + "." );
							}
							case OK -> {
								commandInterface.showSuccessMessage(
										"You now do not follow " + commandInterface.getStopFollowTarget() + " anymore." );
							}
						}
					}
				}
			}
		}
	}

	private void mentions() {
		chCS.< Token >com( commandInterface.getSessionToken() );
		chCS.< String >com( commandInterface.getMentionsUsername() );
		{
			checkUser( Unit.id );
			switch( chCS.< Result >select( Unit.id ) ) {
				case ERROR -> {
					commandInterface.showErrorMessage(
							"Error, could not find user " + commandInterface.getMentionsUsername() + "." );
				}
				case OK -> {
					commandInterface.showMentions( chCS.< Mentions >com( Unit.id ) );
				}
			}
		}
	}

	private void status() {
		chCS.< String >com( commandInterface.getStatusPostID() );
		{
			checkPost( Unit.id );
			switch( chCS.< Result >select( Unit.id ) ) {
				case ERROR -> {
					commandInterface.showErrorMessage(
							"Error, could not find post with ID " + commandInterface.getStatusPostID() + "." );
				}
				case OK -> {
					commandInterface.showPost( chCS.< Post >com( Unit.id ) );
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
