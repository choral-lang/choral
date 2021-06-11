package choral.examples.RetwisChoral;

import java.util.List;

public class DatabaseConnection{

    List< String > getFollowers( String name ){
		return null;
	};

    List< String > getFollowed( String name ){
		return null;
	};

    List< Post > getPostsPage( String name, Integer page ){
		return null;
	};

    void post( String name, String post ){};

    void follow( String name, String followTarget ){};

    void stopFollow( String name, String stopFollowTarget ){};

    Mentions mentions( String mentionsName, Boolean selfMentions ){
		return null;
	};

    Post getPost( String postId ){
		return null;
	};

    Boolean isUserValid( String username ){
		return null;
	};

    Boolean isPostValid( String postId ){
		return null;
	};

    Boolean isFollower( String name, String followTarget ){
		return null;
	};

    void addUser( String name, String pswd ){};

    Boolean auth( String name, String pswd ){
		return null;
	};

}