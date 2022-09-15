package choral.examples.RetwisChoral;

import choral.channels.SymChannel;

public class Retwis@( Client, Server, Repository ){

    private SymChannel@( Client, Server )< Object > chCS;
    private SymChannel@( Server, Repository )< Object > chSR;
    private CommandInterface@Client commandInterface;
    private DatabaseConnection@Repository databaseConnection;
    private SessionManager@Server sessionManager;

    public Retwis( SymChannel@( Client, Server )< Object > chCS,
                   SymChannel@( Server, Repository )< Object > chSR,
                   CommandInterface@Client commandInterface,
                   DatabaseConnection@Repository databaseConnection,
                   SessionManager@Server sessionManager
    ){
        this.chCS = chCS;
        this.chSR = chSR;
        this.commandInterface = commandInterface;
        this.databaseConnection = databaseConnection;
        this.sessionManager = sessionManager;
    }

    public void loop(){
        switch( commandInterface.action() ){
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
        String@Server name = commandInterface.getPostsUsername() >> chCS::< String >com;
        Integer@Server page = commandInterface.getPostsPage() >> chCS::< Integer >com;
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
              >> commandInterface::showPosts;
        } else {
            chSR.< Result >select( Result@Server.ERROR );
            chCS.< Result >select( Result@Server.ERROR );
            commandInterface.showErrorMessage( "Error, could not find user "@Client + commandInterface.getPostsUsername() + "."@Client );
        }
    }

    private void post(){
        Token@Server token = commandInterface.getSessionToken() >> chCS::< Token >com;
        String@Server post = commandInterface.getPost() >> chCS::< String >com;
        if( sessionManager.checkLoggedUser( token ) ){
            chSR.< Result >select( Result@Server.OK );
            chCS.< Result >select( Result@Server.OK );
            databaseConnection.post(
                chSR.< String >com( sessionManager.getUsernameFromToken( token ) ),
                chSR.< String >com( post )
            );
            commandInterface.showSuccessMessage( "Tweet posted successfully."@Client );
        } else {
            chSR.< Result >select( Result@Server.ERROR );
            chCS.< Result >select( Result@Server.ERROR );
            commandInterface.showErrorMessage( "Error, the client is not logged in."@Client );
        }
    }

    // we can use e.g., a static class between the Server and the Repository
    // to reduce the amount selections the client is involved into
    private void follow(){
        Token@Server token = commandInterface.getSessionToken() >> chCS::< Token >com;
        String@Server followTarget = commandInterface.getFollowTarget() >> chCS::< String >com;
        if( sessionManager.checkLoggedUser( token ) ){
            chSR.< Result >select( Result@Server.OK );
            chCS.< Result >select( Result@Server.OK );
            if( checkUser( followTarget ) ){
                chSR.< Result >select( Result@Server.OK );
                chCS.< Result >select( Result@Server.OK );
                String@Server name = sessionManager.getUsernameFromToken( token );
                if( ! checkFollow( name, followTarget ) ){
                    chSR.< Result >select( Result@Server.OK );
                    chCS.< Result >select( Result@Server.OK );
                    databaseConnection.follow(
                        chSR.< String >com( name ),
                        chSR.< String >com( followTarget )
                    );
                    commandInterface.showSuccessMessage( "You now follow "@Client + commandInterface.getFollowTarget() );
                } else {
                    chSR.< Result >select( Result@Server.ERROR );
                    chCS.< Result >select( Result@Server.ERROR );
                    commandInterface.showErrorMessage( "Error, user "@Client + commandInterface.getUsername() + " already follows "@Client + commandInterface.getFollowTarget() + "."@Client );
                }
            } else {
                chSR.< Result >select( Result@Server.ERROR );
                chCS.< Result >select( Result@Server.ERROR );
                commandInterface.showErrorMessage( "Error, could not find user "@Client + commandInterface.getFollowTarget() + " to follow."@Client );
            }
        } else {
            chSR.< Result >select( Result@Server.ERROR );
            chCS.< Result >select( Result@Server.ERROR );
            commandInterface.showErrorMessage( "Error, the client is not logged in."@Client );
        }
    }

    // TODO: we can reduce the nested ifs into a three-case section
    private void stopFollow(){
        Token@Server token = commandInterface.getSessionToken() >> chCS::< Token >com;
        String@Server stopFollowTarget = commandInterface.getStopFollowTarget() >> chCS::< String >com;
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
               commandInterface.showSuccessMessage( "You now do not follow "@Client + commandInterface.getStopFollowTarget() + " anymore."@Client );
           } else {
               chSR.< Result >select( Result@Server.ERROR );
               chCS.< Result >select( Result@Server.ERROR );
               commandInterface.showErrorMessage( "Error, user "@Client + commandInterface.getUsername() + " does not follow "@Client + commandInterface.getStopFollowTarget() + "."@Client );
           }
        } else {
           chSR.< Result >select( Result@Server.ERROR );
           chCS.< Result >select( Result@Server.ERROR );
           commandInterface.showErrorMessage( "Error, the client is not logged in"@Client );
        }
    }

    private void mentions(){
        Token@Server token = commandInterface.getSessionToken() >> chCS::< Token >com;
        String@Server mentionsName = commandInterface.getMentionsUsername() >> chCS::< String >com;
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
              >> commandInterface::showMentions;
        } else {
            chSR.< Result >select( Result@Server.ERROR );
            chCS.< Result >select( Result@Server.ERROR );
            commandInterface.showErrorMessage( "Error, could not find user "@Client + commandInterface.getMentionsUsername() + "."@Client );
        }
    }

    private void status(){
        String@Server postID = commandInterface.getStatusPostID() >> chCS::< String >com;
        if( checkPost( postID ) ){
            chSR.< Result >select( Result@Server.OK );
            chCS.< Result >select( Result@Server.OK );
            postID
                >> chSR::< String >com
                >> databaseConnection::getPost
                >> chSR::< Post >com
                >> chCS::< Post >com
                >> commandInterface::showPost;
        } else {
            chSR.< Result >select( Result@Server.ERROR );
            chCS.< Result >select( Result@Server.ERROR );
            commandInterface.showErrorMessage( "Error, could not find post with ID "@Client + commandInterface.getStatusPostID() + "."@Client );
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
