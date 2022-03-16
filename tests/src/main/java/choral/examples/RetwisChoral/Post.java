package choral.examples.RetwisChoral;

import choral.runtime.Serializers.KryoSerializable;

@KryoSerializable
public class Post {

	private final String content;
	private final String postId;
	private final String poster;

	public Post( String content, String postId, String poster ) {
		this.content = content;
		this.postId = postId;
		this.poster = poster;
	}

	public String content() {
		return content;
	}

	public String postId() {
		return postId;
	}

	public String poster() {
		return poster;
	}

	@Override
	public String toString() {
		return "Post{" +
				"content='" + content + '\'' +
				", postId='" + postId + '\'' +
				", poster='" + poster + '\'' +
				'}';
	}
}