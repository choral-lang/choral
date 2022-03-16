package choral.examples.RetwisChoral.inMemoryImpl;

import choral.examples.RetwisChoral.DatabaseConnection;
import choral.examples.RetwisChoral.Mentions;
import choral.examples.RetwisChoral.Post;

import java.util.*;

public class InMemoryDatabaseConnection implements DatabaseConnection {

	private static final InMemoryDatabaseConnection INSTANCE = new InMemoryDatabaseConnection();
	private final Map< String, User > users;
	private final Map< String, Post > posts;

	public static InMemoryDatabaseConnection instance() {
		return INSTANCE;
	}

	private InMemoryDatabaseConnection() {
		this.users = new HashMap<>();
		this.posts = new HashMap<>();
	}

	@Override
	public List< String > getFollowers( String name ) {
		return new LinkedList<>( users.get( name ).followers() );
	}

	@Override
	public List< String > getFollowed( String name ) {
		return new LinkedList<>( users.get( name ).followed() );
	}

	@Override
	public List< Post > getPostsPage( String name, Integer page ) {
		return users.get( name ).posts().get( page );
	}

	@Override
	public void post( String name, String post ) {
		Post _post = users.get( name ).addPost( post );
		posts.put( _post.postId(), _post );
	}

	@Override
	public void follow( String name, String followTarget ) {
		users.get( name ).followed().add( followTarget );
		users.get( followTarget ).followers().add( name );
	}

	@Override
	public void stopFollow( String name, String stopFollowTarget ) {
		users.get( name ).followed().remove( stopFollowTarget );
		users.get( stopFollowTarget ).followers().remove( name );
	}

	@Override
	public Mentions mentions( String mentionsName, Boolean selfMentions ) {
		return null;
	}

	@Override
	public Post getPost( String postId ) {
		return posts.get( postId );
	}

	@Override
	public Boolean isUserValid( String username ) {
		return users.containsKey( username );
	}

	@Override
	public Boolean isPostValid( String postId ) {
		return posts.containsKey( postId );
	}

	@Override
	public Boolean isFollower( String name, String followTarget ) {
		return users.get( followTarget ).followers.contains( name );
	}

	@Override
	public void addUser( String name, String pswd ) {
		users.put( name, new User(
				UUID.randomUUID().toString(),
				name, pswd,
				new HashSet<>(), new HashSet<>(),
				new ArrayList<>( new LinkedList<>() )
		) );
	}

	@Override
	public Boolean auth( String name, String pswd ) {
		return users.get( name ).password().equals( pswd );
	}

	private static class User {

		private final int POST_PAGE_CAPACITY = 10;

		private final String userId;
		private final String username;
		private final String password;
		private final Set< String > followers;
		private final Set< String > followed;
		private final ArrayList< List< Post > > posts;

		public String username() {
			return username;
		}

		private User(
				String userId,
				String username, String password,
				Set< String > followers,
				Set< String > followed,
				ArrayList< List< Post > > posts
		) {
			this.userId = userId;
			this.username = username;
			this.password = password;
			this.followers = followers;
			this.followed = followed;
			this.posts = posts;
		}

		public String userId() {
			return userId;
		}

		public String password() {
			return password;
		}

		public Set< String > followers() {
			return followers;
		}

		public Set< String > followed() {
			return followed;
		}

		public List< List< Post > > posts() {
			return posts;
		}

		public Post addPost( String content ) {
			Post post = new Post( content, UUID.randomUUID().toString(), username() );
			if( posts.size() > 0 && posts.get( posts.size() - 1 ).size() <= POST_PAGE_CAPACITY ) {
				posts.get( posts.size() - 1 ).add( post );
			} else {
				posts.add( new LinkedList<>( List.of( post ) ) );
			}
			return post;
		}

		@Override
		public String toString() {
			return "User{" +
					"POST_PAGE_CAPACITY=" + POST_PAGE_CAPACITY +
					", userId='" + userId + '\'' +
					", username='" + username + '\'' +
					", password='" + password + '\'' +
					", followers=" + followers +
					", followed=" + followed +
					", posts=" + posts +
					'}';
		}
	}
}
