package choral.examples.RetwisChoral;

import choral.runtime.Serializers.KryoSerializable;

import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;

@KryoSerializable
public class Posts {

	// here we need to group implementation for kryo
	private final LinkedList< String > followers;
	private final LinkedList< String > followed;
	private final LinkedList< Post > posts;

	private Posts(
			LinkedList< String > followers,
			LinkedList< String > followed,
			LinkedList< Post > posts
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
		return new Posts( new LinkedList<>( followers ), new LinkedList<>( followed ),
				new LinkedList<>( posts ) );
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner( "\n" );
		joiner.add( "Followers:" );
		for( String f : followers() ) {
			joiner.add( " - " + f );
		}
		joiner.add( "Followed:" );
		for( String f : followed() ) {
			joiner.add( " - " + f );
		}
		joiner.add( "Posts:" );
		for( Post p : posts() ) {
			joiner.add( " - " + p );
		}
		return joiner.toString();
	}
}