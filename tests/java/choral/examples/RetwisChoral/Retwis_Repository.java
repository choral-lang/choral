package choral.examples.RetwisChoral;

import choral.channels.SymChannel_B;
import choral.annotations.Choreography;
import choral.lang.Unit;

@Choreography( role = "Repository", name = "Retwis" )
public class Retwis_Repository {
	private SymChannel_B< Object > chSR;
	private DatabaseConnection databaseConnection;

	public Retwis_Repository(
			Unit chCS, SymChannel_B< Object > chSR, Unit cli, DatabaseConnection databaseConnection,
			Unit sessionManager
	) {
		this( chSR, databaseConnection );
	}

	public Retwis_Repository( SymChannel_B< Object > chSR, DatabaseConnection databaseConnection ) {
		this.chSR = chSR;
		this.databaseConnection = databaseConnection;
	}

	public void loop() {
		{
			switch( chSR.< RetwisAction >select( Unit.id ) ) {
				case STOPFOLLOW -> {
					stopFollow();
					loop();
				}
				case FOLLOW -> {
					follow();
					loop();
				}
				case LOGOUT -> {

				}
				case STATUS -> {
					status();
					loop();
				}
				case POST -> {
					post();
					loop();
				}
				case MENTIONS -> {
					mentions();
					loop();
				}
				case POSTS -> {
					posts();
					loop();
				}
			}
		}
	}

	private void posts() {
		{
			checkUser( Unit.id );
			switch( chSR.< Result >select( Unit.id ) ) {
				case ERROR -> {

				}
				case OK -> {
					String username;
					username = chSR.< String >com( Unit.id );
					chSR.< Posts >com( Posts.of( databaseConnection.getFollowers( username ),
							databaseConnection.getFollowed( username ),
							databaseConnection.getPostsPage( username,
									chSR.< Integer >com( Unit.id ) ) ) );
				}
			}
		}
	}

	private void post() {
		{
			switch( chSR.< Result >select( Unit.id ) ) {
				case ERROR -> {

				}
				case OK -> {
					databaseConnection.post( chSR.< String >com( Unit.id ),
							chSR.< String >com( Unit.id ) );
				}
			}
		}
	}

	private void follow() {
		{
			switch( chSR.< Result >select( Unit.id ) ) {
				case ERROR -> {

				}
				case OK -> {
					{
						checkUser( Unit.id );
						switch( chSR.< Result >select( Unit.id ) ) {
							case ERROR -> {

							}
							case OK -> {
								{
									checkFollow( Unit.id, Unit.id );
									switch( chSR.< Result >select( Unit.id ) ) {
										case ERROR -> {

										}
										case OK -> {
											databaseConnection.follow(
													chSR.< String >com( Unit.id ),
													chSR.< String >com( Unit.id ) );
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
		{
			switch( chSR.< Result >select( Unit.id ) ) {
				case ERROR -> {

				}
				case OK -> {
					{
						checkFollow( Unit.id, Unit.id );
						switch( chSR.< Result >select( Unit.id ) ) {
							case ERROR -> {

							}
							case OK -> {
								databaseConnection.stopFollow( chSR.< String >com( Unit.id ),
										chSR.< String >com( Unit.id ) );
							}
						}
					}
				}
			}
		}
	}

	private void mentions() {
		{
			checkUser( Unit.id );
			switch( chSR.< Result >select( Unit.id ) ) {
				case ERROR -> {

				}
				case OK -> {
					chSR.< Mentions >com(
							databaseConnection.mentions( chSR.< String >com( Unit.id ),
									chSR.< Boolean >com( Unit.id ) ) );
				}
			}
		}
	}

	private void status() {
		{
			checkPost( Unit.id );
			switch( chSR.< Result >select( Unit.id ) ) {
				case ERROR -> {

				}
				case OK -> {
					chSR.< Post >com( databaseConnection.getPost( chSR.< String >com( Unit.id ) ) );
				}
			}
		}
	}

	private Unit checkUser( Unit name ) {
		return chSR.< Boolean >com(
				databaseConnection.isUserValid( chSR.< String >com( Unit.id ) ) );
	}

	private Unit checkPost( Unit postID ) {
		return chSR.< Boolean >com(
				databaseConnection.isPostValid( chSR.< String >com( Unit.id ) ) );
	}

	private Unit checkFollow( Unit name, Unit followTarget ) {
		return chSR.< Boolean >com( databaseConnection.isFollower( chSR.< String >com( Unit.id ),
				chSR.< String >com( Unit.id ) ) );
	}

}
