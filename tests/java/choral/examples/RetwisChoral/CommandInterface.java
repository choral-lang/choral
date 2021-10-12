package choral.examples.RetwisChoral;

public interface CommandInterface {

    RetwisAction action();

    String getPostsUsername();

    String getUsername();

    Integer getPostsPage();

    void showPosts( Posts posts );

    void showPost( Post post );

    void showErrorMessage( String message );

    void showSuccessMessage( String message );

    Token getSessionToken();

    String getPost();

    String getFollowTarget();

    void showMentions( Mentions mentions );

    String getStatusPostID();

    String promptPassword();

    String getMentionsUsername();

    String getStopFollowTarget();

}