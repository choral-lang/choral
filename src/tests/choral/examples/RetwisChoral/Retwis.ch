package choral.examples.RetwisChoral;

import choral.channels.SymChannel;

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
                status();
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
            String@Repository username = chSR.< String >com( name );
            Posts@Repository.of(
                databaseConnection.getFollowers( username ),
                databaseConnection.getFollowed( username ),
                databaseConnection.getPostsPage( username, chSR.< Integer >com( page ) )
            ) >> chSR::< Posts >com
              >> chCS::< Posts >com
              >> cli::showPosts;
        } else {
            chSR.< Result >select( Result@Server.ERROR );
            chCS.< Result >select( Result@Server.ERROR );
            cli.showErrorMessage( "Error, could not find user "@Client + cli.getPostsUsername() + "."@Client );
        }
    }

    private void post(){
        Token@Server token = cli.getSessionToken() >> chCS::< Token >com;
        String@Server post = cli.getPost() >> chCS::< String >com;
        if( sessionManager.checkLoggedUser( token ) ){
            chSR.< Result >select( Result@Server.OK );
            chCS.< Result >select( Result@Server.OK );
            databaseConnection.post(
                chSR.< String >com( sessionManager.getUsernameFromToken( token ) ),
                chSR.< String >com( post )
            );
            cli.showSuccessMessage( "Tweet posted successfully."@Client );
        } else {
            chSR.< Result >select( Result@Server.ERROR );
            chCS.< Result >select( Result@Server.ERROR );
            cli.showErrorMessage( "Error, the client is not logged in."@Client );
        }
    }

    // we can use e.g., a static class between the Server and the Repository
    // to reduce the amount selections the client is involved into
    private void follow(){
        Token@Server token = cli.getSessionToken() >> chCS::< Token >com;
        String@Server followTarget = cli.getFollowTarget() >> chCS::< String >com;
        if( sessionManager.checkLoggedUser( token ) ){
            chSR.< Result >select( Result@Server.OK );
            chCS.< Result >select( Result@Server.OK );
            if( checkUser( followTarget ) ){
                chSR.< Result >select( Result@Server.OK );
                chCS.< Result >select( Result@Server.OK );
                String@Server name = sessionManager.getUsernameFromToken( token );
                if( checkFollow( name, followTarget ) ){
                    chSR.< Result >select( Result@Server.OK );
                    chCS.< Result >select( Result@Server.OK );
                    databaseConnection.follow(
                        chSR.< String >com( name ),
                        chSR.< String >com( followTarget )
                    );
                    cli.showSuccessMessage( "You now follow "@Client + cli.getFollowTarget() );
                } else {
                    chSR.< Result >select( Result@Server.ERROR );
                    chCS.< Result >select( Result@Server.ERROR );
                    cli.showErrorMessage( "Error, user "@Client + cli.getUsername() + " already follows "@Client + cli.getFollowTarget() + "."@Client );
                }
            } else {
                chSR.< Result >select( Result@Server.ERROR );
                chCS.< Result >select( Result@Server.ERROR );
                cli.showErrorMessage( "Error, could not find user "@Client + cli.getFollowTarget() + " to follow."@Client );
            }
        } else {
            chSR.< Result >select( Result@Server.ERROR );
            chCS.< Result >select( Result@Server.ERROR );
            cli.showErrorMessage( "Error, the client is not logged in."@Client );
        }
    }

    // TODO: we can reduce the nested ifs into a three-case section
    private void stopFollow(){
        Token@Server token = cli.getSessionToken() >> chCS::< Token >com;
        String@Server stopFollowTarget = cli.getStopFollowTarget() >> chCS::< String >com;
        if( sessionManager.checkLoggedUser( token ) ){
           chSR.< Result >select( Result@Server.OK );
           chCS.< Result >select( Result@Server.OK );
           String@Server name = sessionManager.getUsernameFromToken( token );
           if( checkFollow( name, stopFollowTarget ) ){
               chSR.< Result >select( Result@Server.OK );
               chCS.< Result >select( Result@Server.OK );
               databaseConnection.stopFollow(
                   chSR.< String >com( name ),
                   chSR.< String >com( stopFollowTarget )
               );
               cli.showSuccessMessage( "You now do not follow "@Client + cli.getStopFollowTarget() + " anymore."@Client );
           } else {
               chSR.< Result >select( Result@Server.ERROR );
               chCS.< Result >select( Result@Server.ERROR );
               cli.showErrorMessage( "Error, user "@Client + cli.getUsername() + " does not follow "@Client + cli.getStopFollowTarget() + "."@Client );
           }
        } else {
           chSR.< Result >select( Result@Server.ERROR );
           chCS.< Result >select( Result@Server.ERROR );
           cli.showErrorMessage( "Error, the client is not logged in"@Client );
        }
    }

    private void mentions(){
        Token@Server token = cli.getSessionToken() >> chCS::< Token >com;
        String@Server mentionsName = cli.getMentionsUsername() >> chCS::< String >com;
        if( checkUser( mentionsName ) ){
            chSR.< Result >select( Result@Server.OK );
            chCS.< Result >select( Result@Server.OK );
            Boolean@Server selfMentions =
                sessionManager.getUsernameFromToken( token )
                >> mentionsName::equals;
            databaseConnection.mentions(
                chSR.< String >com( mentionsName ),
                chSR.< Boolean >com( selfMentions ) // if the user is logged and the user is the mentionsName, we add their personal info
            ) >> chSR::< Mentions >com
              >> chCS::< Mentions >com
              >> cli::showMentions;
        } else {
            chSR.< Result >select( Result@Server.ERROR );
            chCS.< Result >select( Result@Server.ERROR );
            cli.showErrorMessage( "Error, could not find user "@Client + cli.getMentionsUsername() + "."@Client );
        }
    }

    private void status(){
        String@Server postID = cli.getStatusPostID() >> chCS::< String >com;
        if( checkPost( postID ) ){
            chSR.< Result >select( Result@Server.OK );
            chCS.< Result >select( Result@Server.OK );
            postID
                >> chSR::< String >com
                >> databaseConnection::getPost
                >> chSR::< Post >com
                >> chCS::< Post >com
                >> cli::showPost;
        } else {
            chSR.< Result >select( Result@Server.ERROR );
            chCS.< Result >select( Result@Server.ERROR );
            cli.showErrorMessage( "Error, could not find post with ID "@Client + cli.getStatusPostID() + "."@Client );
        }
    }

    private Boolean@Server checkUser( String@Server name ){
        return name
        >> chSR::< String >com
        >> databaseConnection::isUserValid
        >> chSR::< Boolean >com;
    }

    private Boolean@Server checkPost( String@Server postID ){
        return postID
        >> chSR::< String >com
        >> databaseConnection::isPostValid
        >> chSR::< Boolean >com;
    }


    private Boolean@Server checkFollow( String@Server name, String@Server followTarget ){
        return databaseConnection.isFollower(
            chSR.< String >com( name ),
            chSR.< String >com( followTarget )
        ) >> chSR::< Boolean >com;
    }


}