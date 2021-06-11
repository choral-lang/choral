package choral.examples.RetwisChoral;
import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.lang.Unit;

@Choreography( role = "Client", name = "Retwis" )
public class Retwis_Client {
	private SymChannel_A < Object > chCS;
	private CLI cli;

	public Retwis_Client( SymChannel_A < Object > chCS, Unit chSR, CLI cli, Unit databaseConnection, Unit sessionManager ) {
		this( chCS, cli );
	}
	
	public Retwis_Client( SymChannel_A < Object > chCS, CLI cli ) {
		this.chCS = chCS;
		this.cli = cli;
	}

	private void posts() {
		chCS.< String >com( cli.getPostsUsername() );
		chCS.< Integer >com( cli.getPostsPage() );
		{
			checkUser( Unit.id );
			switch( chCS.< Result >select( Unit.id ) ){
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case ERROR -> {
					cli.showErrorMessage( "Error, could not find user " + cli.getPostsUsername() + "." );
				}
				case OK -> {
					cli.showPosts( chCS.< Posts >com( Unit.id ) );
				}
			}
		}
	}
	
	private void post() {
		chCS.< Token >com( cli.getSessionToken() );
		chCS.< String >com( cli.getPost() );
		{
			switch( chCS.< Result >select( Unit.id ) ){
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case ERROR -> {
					cli.showErrorMessage( "Error, the client is not logged in." );
				}
				case OK -> {
					cli.showSuccessMessage( "Tweet posted successfully." );
				}
			}
		}
	}
	
	private void follow() {
		chCS.< Token >com( cli.getSessionToken() );
		chCS.< String >com( cli.getFollowTarget() );
		{
			switch( chCS.< Result >select( Unit.id ) ){
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case ERROR -> {
					cli.showErrorMessage( "Error, the client is not logged in." );
				}
				case OK -> {
					{
						checkUser( Unit.id );
						switch( chCS.< Result >select( Unit.id ) ){
							default -> {
								throw new RuntimeException( "Received unexpected label from select operation" );
							}
							case ERROR -> {
								cli.showErrorMessage( "Error, could not find user " + cli.getFollowTarget() + " to follow." );
							}
							case OK -> {
								{
									checkFollow( Unit.id, Unit.id );
									switch( chCS.< Result >select( Unit.id ) ){
										default -> {
											throw new RuntimeException( "Received unexpected label from select operation" );
										}
										case ERROR -> {
											cli.showErrorMessage( "Error, user " + cli.getUsername() + " already follows " + cli.getFollowTarget() + "." );
										}
										case OK -> {
											cli.showSuccessMessage( "You now follow " + cli.getFollowTarget() );
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
		chCS.< Token >com( cli.getSessionToken() );
		chCS.< String >com( cli.getStopFollowTarget() );
		{
			switch( chCS.< Result >select( Unit.id ) ){
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case ERROR -> {
					cli.showErrorMessage( "Error, the client is not logged in" );
				}
				case OK -> {
					{
						checkFollow( Unit.id, Unit.id );
						switch( chCS.< Result >select( Unit.id ) ){
							default -> {
								throw new RuntimeException( "Received unexpected label from select operation" );
							}
							case ERROR -> {
								cli.showErrorMessage( "Error, user " + cli.getUsername() + " does not follow " + cli.getStopFollowTarget() + "." );
							}
							case OK -> {
								cli.showSuccessMessage( "You now do not follow " + cli.getStopFollowTarget() + " anymore." );
							}
						}
					}
				}
			}
		}
	}
	
	private void mentions() {
		chCS.< Token >com( cli.getSessionToken() );
		chCS.< String >com( cli.getMentionsUsername() );
		{
			checkUser( Unit.id );
			switch( chCS.< Result >select( Unit.id ) ){
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case ERROR -> {
					cli.showErrorMessage( "Error, could not find user " + cli.getMentionsUsername() + "." );
				}
				case OK -> {
					cli.showMentions( chCS.< Mentions >com( Unit.id ) );
				}
			}
		}
	}
	
	private void status() {
		chCS.< String >com( cli.getStatusPostID() );
		{
			checkPost( Unit.id );
			switch( chCS.< Result >select( Unit.id ) ){
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case ERROR -> {
					cli.showErrorMessage( "Error, could not find post with ID " + cli.getStatusPostID() + "." );
				}
				case OK -> {
					cli.showPost( chCS.< Post >com( Unit.id ) );
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
