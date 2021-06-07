public class Retwis@( Client, Server, Repository ){

    private SymChannel@( Client, Server )< Object > chCS;
    private SymChannel@( Server, Repository )< Object > chSR;
    private CLI@Client cli;
    private DatabaseConnection@Repository databaseConnection;
    private SessionManager@Server sessionManager;
    private LoginManager@Client clientLoginManager;

    public Retwis( SymChannel@( Client, Server )< Object > chCS,
                   SymChannel@( Server, Repository )< Object > chSR,
                   CLI@Client cli,
                   DatabaseConnection@Repository databaseConnection,
                   SessionManager@Server sessionManager,
                   LoginManager@Client clientLoginManager
    ){
        this.chCS = chCS;
        this.chSR = chSR;
        this.cli = cli;
        this.databaseConnection = databaseConnection;
        this.sessionManager = sessionManager;
        this.loginManager = loginManager;
    }

    public loop(){
        switch( cli.action() ){
            case POSTS      -> {
                chCS.< Result >select( Action@Client.POSTS );
                chSR.< Result >select( Action@Server.POSTS );
                posts();
                loop();
            }
            case POST       -> {
                chCS.< Result >select( Action@Client.POST );
                chSR.< Result >select( Action@Server.POST );
                post();
                loop();
            }
            case FOLLOW     -> {
                chCS.< Result >select( Action@Client.FOLLOW );
                chSR.< Result >select( Action@Server.FOLLOW );
                follow();
                loop();
            }
            case STOPFOLLOW -> {
                chCS.< Result >select( Action@Client.STOPFOLLOW );
                chSR.< Result >select( Action@Server.STOPFOLLOW );
                stopFollow();
                loop();
            }
            case MENTIONS   -> {
                chCS.< Result >select( Action@Client.MENTIONS );
                chSR.< Result >select( Action@Server.MENTIONS );
                mentions();
                loop();
            }
            case STATUS     -> {
                chCS.< Result >select( Action@Client.STATUS );
                chSR.< Result >select( Action@Server.STATUS );
                timeline();
                loop();
            }
            case LOGOUT     -> {
                chCS.< Result >select( Action@Client.LOGOUT );
                chSR.< Result >select( Action@Server.LOGOUT );
                clientLoginManager.logout();
            }
        }

    }

    private void posts(){
        String@Server name = cli.getPostsUsername() >> chCS::< String >com;
        Integer@Server page = cli.getPostsPage() >> chCS::< String >com;
        if( checkUser( name ) ){
            chSR.< Result >select( Result@Server.OK );
            chCS.< Result >select( Result@Server.OK );
            Posts@Repository.of(
                databaseConnection.getFollowers( name ),
                databaseConnection.getFollowed( name ),
                databaseConnection.getPostsPage( name, page >> chSR::< Integer >com )
            ) >> chSR::< Posts >com
              >> chCS::< Posts >com
              >> cli::showPosts( posts );
        } else {
            chSR.< Result >select( Result@Server.ERROR );
            chCS.< Result >select( Result@Server.ERROR );
            cli.showErrorMessage( "Error, could not find user " + cli.getPostsUsername() + "." );
        }
    }

    private void post(){
        String@Server name = cli.getUsername() >> chCS::< String >com;
        String@Server post = cli.getPost() >> chCS::< String >com;
        if( sessionManager.checkLoggedUser( name ) ){
            chSR.< Result >select( Result@Repository.OK );
            chCS.< Result >select( Result@Server.OK );
            databaseConnection.post(
                name >> chSR::< String >com,
                post >> chSR::< String >com
            );
            cli.showSuccessMessage( "Tweet posted successfully." );
        } else {
            chSR.< Result >select( Result@Repository.ERROR );
            chCS.< Result >select( Result@Server.ERROR );
            cli.showErrorMessage( "Error, could not post tweet, user " + cli.getUsername() + " is logged out." );
        }
    }

    private void follow(){
        String@Server name = cli.getUsername() >> chCS::< String >com;
        String@Server followTarget = cli.getFollowTarget() >> chCS::< String >com;
        if( sessionManager.checkLoggedUser( name ) ){
            chSR.< Result >select( Result@Repository.OK );
            chCS.< Result >select( Result@Server.OK );
            if( checkUser( followTarget ) ){
                chSR.< Result >select( Result@Repository.OK );
                chCS.< Result >select( Result@Server.OK );
                if( checkFollow( name, followTarget ) ){
                    chSR.< Result >select( Result@Repository.OK );
                    chCS.< Result >select( Result@Server.OK );
                    databaseConnection.follow(
                        name >> chSR::< String >com,
                        followTarget >> chSR::< String >com
                    );
                    cli.showSuccessMessage( "You now follow " + cli.getFollowTarget() );
                } else {
                    chSR.< Result >select( Result@Repository.ERROR );
                    chCS.< Result >select( Result@Server.ERROR );
                    cli.showErrorMessage( "Error, user " + cli.getUsername() + " already follows " + cli.getFollowTarget() + "." );
                }
            } else {
                chSR.< Result >select( Result@Repository.ERROR );
                chCS.< Result >select( Result@Server.ERROR );
                cli.showErrorMessage( "Error, could not find user " + cli.getFollowTarget() + " to follow." );
            }
        } else {
            chSR.< Result >select( Result@Repository.ERROR );
            chCS.< Result >select( Result@Server.ERROR );
            cli.showErrorMessage( "Error, user " + cli.getUsername() + " is logged out." );
        }
    }

    private void stopFollow(){
        String@Server name = cli.getUsername() >> chCS::< String >com;
        String@Server stopFollowTarget = cli.getStopFollowTarget() >> chCS::< String >com;
        if( sessionManager.checkLoggedUser( name ) ){
           chSR.< Result >select( Result@Repository.OK );
           chCS.< Result >select( Result@Server.OK );
           if( checkFollow( name, stopFollowTarget ) ){
               chSR.< Result >select( Result@Repository.OK );
               chCS.< Result >select( Result@Server.OK );
               databaseConnection.stopFollow(
                   name >> chSR::< String >com,
                   stopFollowTarget >> chSR::< String >com
               );
               cli.showSuccessMessage( "You now do not follow " + cli.getUnfollowTarget() + " anymore." );
           } else {
               chSR.< Result >select( Result@Repository.ERROR );
               chCS.< Result >select( Result@Server.ERROR );
               cli.showErrorMessage( "Error, user " + cli.getUsername() + " does not follow " + cli.getStopFollowTarget() + "." );
           }
        } else {
           chSR.< Result >select( Result@Repository.ERROR );
           chCS.< Result >select( Result@Server.ERROR );
           cli.showErrorMessage( "Error, user " + cli.getUsername() + " is logged out." );
        }
    }

    private void mentions(){
        String@Server name = cli.getUsername() >> chCS::< String >com;
        String@Server mentionsName = cli.getMentionsUsername() >> chCS::< String >com;
        if( checkUser( mentionsName ) ){
            chSR.< Result >select( Result@Server.OK );
            chCS.< Result >select( Result@Server.OK );
            databaseConnection.mentions(
                mentionsName >> chSR::< String >com,
                name.equals( mentionsName ) >> chSR::< Boolean >com // if the logged user is the mentionsName, we add their personal info
            ) >> chSR::< Mentions >com
              >> chCS::< Mentions >com
              >> cli::showMentions
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