package choral.examples.RetwisChoral;

public class Retwis@( Client, Server, Repository ){

    private SymChannel@( Client, Server )< Object > chCS;
    private SymChannel@( Server, Repository )< Object > chSR;
    private CLI@Client cli;
    private DatabaseConnection@Repository databaseConnection;
    private SessionManager@Server sessionManager;

    public Retwis( SymChannel@( Client, Server )< Object > chCS,
                   SymChannel@( Server, Repository )< Object > chSR,
                   CLI@Client cli,
                   DatabaseConnection@Repository databaseConnection,
                   SessionManager@Server sessionManager
    ){
        this.chCS = chCS;
        this.chSR = chSR;
        this.cli = cli;
        this.databaseConnection = databaseConnection;
        this.sessionManager = sessionManager;
    }

    public void loop(){
        switch( cli.action() ){
            case POSTS      -> {
                chCS.< RetwisAction >select( RetwisAction@Client.POSTS );
                chSR.< RetwisAction >select( RetwisAction@Server.POSTS );
                posts();
                loop();
            }
            case POST       -> {
                chCS.< RetwisAction >select( RetwisAction@Client.POST );
                chSR.< RetwisAction >select( RetwisAction@Server.POST );
                post();
                loop();
            }
            case FOLLOW     -> {
                chCS.< RetwisAction >select( RetwisAction@Client.FOLLOW );
                chSR.< RetwisAction >select( RetwisAction@Server.FOLLOW );
                follow();
                loop();
            }
            case STOPFOLLOW -> {
                chCS.< RetwisAction >select( RetwisAction@Client.STOPFOLLOW );
                chSR.< RetwisAction >select( RetwisAction@Server.STOPFOLLOW );
                stopFollow();
                loop();
            }
            case MENTIONS   -> {
                chCS.< RetwisAction >select( RetwisAction@Client.MENTIONS );
                chSR.< RetwisAction >select( RetwisAction@Server.MENTIONS );
                mentions();
                loop();
            }
            case STATUS     -> {
                chCS.< RetwisAction >select( RetwisAction@Client.STATUS );
                chSR.< RetwisAction >select( RetwisAction@Server.STATUS );
                timeline();
                loop();
            }
            case LOGOUT     -> {
                chCS.< RetwisAction >select( RetwisAction@Client.LOGOUT );
                chSR.< RetwisAction >select( RetwisAction@Server.LOGOUT );
            }
        }
    }

    private void posts(){
        String@Server name = cli.getPostsUsername() >> chCS::< String >com;
        Integer@Server page = cli.getPostsPage() >> chCS::< Integer >com;
        if( checkUser( name ) ){
            chSR.< Result >select( Result@Server.OK );
            chCS.< Result >select( Result@Server.OK );
            Posts@Repository.of(
                databaseConnection.getFollowers( name ),
                databaseConnection.getFollowed( name ),
                databaseConnection.getPostsPage( name, page >> chSR::< Integer >com )
            ) >> chSR::< Posts >com
              >> chCS::< Posts >com
              >> cli::showPosts;
        } else {
            chSR.< Result >select( Result@Server.ERROR );
            chCS.< Result >select( Result@Server.ERROR );
            cli.showErrorMessage( "Error, could not find user " + cli.getPostsUsername() + "." );
        }
    }

    private void post(){
        Token@Server token = cli.getSessionToken() >> chCS::< Token >com;
        String@Server post = cli.getPost() >> chCS::< String >com;
        if( sessionManager.checkLoggedUser( token ) ){
            chSR.< Result >select( Result@Server.OK );
            chCS.< Result >select( Result@Server.OK );
            databaseConnection.post(
                name >> chSR::< String >com,
                post >> chSR::< String >com
            );
            cli.showSuccessMessage( "Tweet posted successfully." );
        } else {
            chSR.< Result >select( Result@Server.ERROR );
            chCS.< Result >select( Result@Server.ERROR );
            cli.showErrorMessage( "Error, the client is not logged in." );
        }
    }

    // we can use e.g., a static class between the Server and the Repository
    // to reduce the amount selections the client is involved into
    private void follow(){
        Token@Server token = cli.getSessionToken() >> chCS::< String >com;
        String@Server followTarget = cli.getFollowTarget() >> chCS::< String >com;
        if( sessionManager.checkLoggedUser( token ) ){
            chSR.< Result >select( Result@Server.OK );
            chCS.< Result >select( Result@Server.OK );
            if( checkUser( followTarget ) ){
                chSR.< Result >select( Result@Repository.OK );
                chCS.< Result >select( Result@Server.OK );
                String@Server name = sessionManager.getUsernameFromToken( token );
                if( checkFollow( name, followTarget ) ){
                    chSR.< Result >select( Result@Server.OK );
                    chCS.< Result >select( Result@Server.OK );
                    databaseConnection.follow(
                        name >> chSR::< String >com,
                        followTarget >> chSR::< String >com
                    );
                    cli.showSuccessMessage( "You now follow " + cli.getFollowTarget() );
                } else {
                    chSR.< Result >select( Result@Server.ERROR );
                    chCS.< Result >select( Result@Server.ERROR );
                    cli.showErrorMessage( "Error, user " + cli.getUsername() + " already follows " + cli.getFollowTarget() + "." );
                }
            } else {
                chSR.< Result >select( Result@Server.ERROR );
                chCS.< Result >select( Result@Server.ERROR );
                cli.showErrorMessage( "Error, could not find user " + cli.getFollowTarget() + " to follow." );
            }
        } else {
            chSR.< Result >select( Result@Server.ERROR );
            chCS.< Result >select( Result@Server.ERROR );
            cli.showErrorMessage( "Error, the client is not logged in." );
        }
    }

    // TODO: we can reduce the nested ifs into a three-case section
    private void stopFollow(){
        Token@Server token = cli.getSessionToken() >> chCS::< String >com;
        String@Server stopFollowTarget = cli.getStopFollowTarget() >> chCS::< String >com;
        if( sessionManager.checkLoggedUser( token ) ){
           chSR.< Result >select( Result@Server.OK );
           chCS.< Result >select( Result@Server.OK );
           String@Server name = sessionManager.getUsernameFromToken( token );
           if( checkFollow( name, stopFollowTarget ) ){
               chSR.< Result >select( Result@Server.OK );
               chCS.< Result >select( Result@Server.OK );
               databaseConnection.stopFollow(
                   name >> chSR::< String >com,
                   stopFollowTarget >> chSR::< String >com
               );
               cli.showSuccessMessage( "You now do not follow " + cli.getUnfollowTarget() + " anymore." );
           } else {
               chSR.< Result >select( Result@Server.ERROR );
               chCS.< Result >select( Result@Server.ERROR );
               cli.showErrorMessage( "Error, user " + cli.getUsername() + " does not follow " + cli.getStopFollowTarget() + "." );
           }
        } else {
           chSR.< Result >select( Result@Server.ERROR );
           chCS.< Result >select( Result@Server.ERROR );
           cli.showErrorMessage( "Error, the client is not logged in" );
        }
    }

    private void mentions(){
        String@Token token = cli.getSessionToken() >> chCS::< String >com;
        String@Server mentionsName = cli.getMentionsUsername() >> chCS::< String >com;
        if( checkUser( mentionsName ) ){
            chSR.< Result >select( Result@Server.OK );
            chCS.< Result >select( Result@Server.OK );
            Boolean selfMentions =
                sessionManager.getUsernameFromToken( token )
                >> mentionsName::equals;
            databaseConnection.mentions(
                mentionsName >> chSR::< String >com,
                selfMentions >> chSR::< Boolean >com // if the user is logged and the user is the mentionsName, we add their personal info
            ) >> chSR::< Mentions >com
              >> chCS::< Mentions >com
              >> cli::showMentions;
        } else {
            chSR.< Result >select( Result@Server.ERROR );
            chCS.< Result >select( Result@Server.ERROR );
            cli.showErrorMessage( "Error, could not find user " + cli.getMentionsUsername() + "." );
        }
    }

    private void status(){
        String@Server postID = cli.getStatusPostID() >> chCS::< String >com;
        if( checkPost( postID ) ){
            chSR.< Result >select( Result@Server.OK );
            chCS.< Result >select( Result@Server.OK );
            postID
                >> chSR::< String >com
                >> databaseConnection.getPost
                >> chSR::< Post >com
                >> chCS::< Post >com
                >> cli::showPost;
        } else {
            chSR.< Result >select( Result@Server.ERROR );
            chCS.< Result >select( Result@Server.ERROR );
            cli.showErrorMessage( "Error, could not find post with ID " + cli.getStatusPostID() + "." );
        }
    }

    private Boolean@Server checkUser( String@Server name ){
        return name
        >> chSR::< String >com
        >> databaseConnection.isUserValid
        >> chSR::< Boolean >com
    }

    private Boolean@Server checkPost( String@Server postID ){
        return postID
        >> chSR::< String >com
        >> databaseConnection.isPostValid
        >> chSR::< Boolean >com
    }

    private Boolean@Server checkFollow( String@Server name, String@Server followTarget ){
        return databaseConnection.isFollower(
            name >> chSR::< String >com,
            followTarget >> chSR::< String >com
        ) >> chSR::< Boolean >com;
    }

}