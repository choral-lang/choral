package choral.examples.RetwisChoral;

import java.util.List;

public interface DatabaseConnection{

    List< String > getFollowers( String name );

    List< String > getFollowed( String name );

    List< Post > getPostsPage( String name, Integer page );

    void post( String name, String post );

    void follow( String name, String followTarget );

    void stopFollow( String name, String stopFollowTarget );

    Mentions mentions( String mentionsName, Boolean selfMentions );

    Post getPost( String postId );

    Boolean isUserValid( String username );

    Boolean isPostValid( String postId );

    Boolean isFollower( String name, String followTarget );

    void addUser( String name, String pswd );

    Boolean auth( String name, String pswd );

}