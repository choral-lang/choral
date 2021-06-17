package choral.examples.RetwisChoral;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ScriptedCLI implements CLI {

	private final Map< String, Token > sessions;
	private Map< String, Object > currentAction = new HashMap<>();
	static private final List< Map< String, Object > > actions = new LinkedList<>();

	public ScriptedCLI() {
		this.sessions = new HashMap<>();
	}

	public ScriptedCLI addSession( String name, Token token ) {
		sessions.put( name, token );
		return this;
	}

	private static final String ACTION_KEY = "action";
	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";
	private static final String PAGE_NUM = "page";
	private static final String POST = "post";
	private static final String FOLLOW_TARGET = "follow_target";
	private static final String STOP_FOLLOW_TARGET = "stop_follow_target";
	private static final String POST_ID = "post_id";

	static {
		actions.add( MapBuilder.< String, Object >of( new HashMap<>() )
				.put( ACTION_KEY, RetwisAction.POST )
				.put( USERNAME, "Save" )
				.put( POST, "This is a simple post." )
				.done()
		);

		actions.add( MapBuilder.< String, Object >of( new HashMap<>() )
				.put( ACTION_KEY, RetwisAction.POSTS )
				.put( USERNAME, "Save" )
				.put( PAGE_NUM, 0 )
				.put( POST, "This is a simple post." )
				.done()
		);

		actions.add( MapBuilder.< String, Object >of( new HashMap<>() )
				.put( ACTION_KEY, RetwisAction.LOGOUT )
				.done()
		);

	}

	@Override
	public RetwisAction action() {
		currentAction = actions.remove( 0 );
		return (RetwisAction) currentAction.get( ACTION_KEY );
	}

	@Override
	public String getPostsUsername() {
		return (String) currentAction.get( USERNAME );
	}

	@Override
	public String getUsername() {
		return (String) currentAction.get( USERNAME );
	}

	@Override
	public Integer getPostsPage() {
		return (Integer) currentAction.get( PAGE_NUM );
	}

	@Override
	public void showPosts( Posts posts ) {
		System.out.println( posts );
	}

	@Override
	public void showPost( Post post ) {
		System.out.println( post );
	}

	@Override
	public void showErrorMessage( String message ) {
		System.err.println( message );
	}

	@Override
	public void showSuccessMessage( String message ) {
		System.out.println( message );
	}

	@Override
	public Token getSessionToken() {
		return sessions.get( (String) currentAction.get( USERNAME ) );
	}

	@Override
	public String getPost() {
		return (String) currentAction.get( POST );
	}

	@Override
	public String getFollowTarget() {
		return (String) currentAction.get( FOLLOW_TARGET );
	}

	@Override
	public void showMentions( Mentions mentions ) {
		System.out.println( mentions );
	}

	@Override
	public String getStatusPostID() {
		return (String) currentAction.get( POST_ID );
	}

	@Override
	public String promptPassword() {
		return (String) currentAction.get( PASSWORD );
	}

	@Override
	public String getMentionsUsername() {
		return (String) currentAction.get( USERNAME );
	}

	@Override
	public String getStopFollowTarget() {
		return (String) currentAction.get( STOP_FOLLOW_TARGET );
	}

	static class MapBuilder< KEY, VALUE > {

		private final Map< KEY, VALUE > map;

		private MapBuilder( Map< KEY, VALUE > map ) {
			this.map = map;
		}

		public static < BKEY, BVALUE > MapBuilder< BKEY, BVALUE > of( Map< BKEY, BVALUE > map ) {
			return new MapBuilder<>( map );
		}

		MapBuilder< KEY, VALUE > put( KEY key, VALUE value ) {
			map.put( key, value );
			return this;
		}

		Map< KEY, VALUE > done() {
			return map;
		}

	}

}
