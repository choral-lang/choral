package choral.examples.RetwisChoral;

import choral.runtime.Serializers.KryoSerializable;

import java.util.List;
import java.util.StringJoiner;

@KryoSerializable
public class Posts {

	private final List< String > followers;
	private final List< String > followed;
	private final List< Post > posts;

	private Posts(
			List< String > followers, List< String > followed,
			List< Post > posts
	) {
		this.followers = followers;
		this.followed = followed;
		this.posts = posts;
	}

	public List< String > followers() {
		return followers;
	}

	public List< String > followed() {
		return followed;
	}

	public List< Post > posts() {
		return posts;
	}

	static Posts of( List< String > followers, List< String > followed, List< Post > posts ) {
		return new Posts( followers, followed, posts );
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner( "\n" );
		joiner.add( "Followers: " );
		followers().forEach( f -> joiner.add( " - " + f ) );
		System.out.println( "Followed: " );
		followed().forEach( f -> joiner.add( " - " + f ) );
		System.out.println( "Posts" );
		posts().forEach( p -> joiner.add( " - " + p ) );
		return joiner.toString();
	}
}