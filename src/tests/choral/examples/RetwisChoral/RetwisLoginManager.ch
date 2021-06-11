package choral.examples.RetwisChoral;

import choral.channels.SymChannel;
import java.util.Optional;

public class RetwisLoginManager@( Client, Server, Repository ) {

     private SymChannel@( Client, Server )< Object > chCS;
     private SymChannel@( Server, Repository )< Object > chSR;
     private CLI@Client cli;
     private DatabaseConnection@Repository db;
     private SessionManager@Server sessionManager;

     public RetwisLoginManager(
         SymChannel@( Client, Server )< Object > chCS,
         SymChannel@( Server, Repository )< Object > chSR,
         CLI@Client cli,
         DatabaseConnection@Repository db,
         SessionManager@Server sessionManager
     ) {
        this.chCS = chCS;
        this.chSR = chSR;
        this.cli = cli;
        this.db = db;
        this.sessionManager = sessionManager;
     }

/*
     public Optional@Client< Token > main( LoginAction@Client action ) {
         switch( action ){
             case SIGNUP      -> {
                 chCS.< LoginAction >select( LoginAction@Client.SIGNUP );
                 chSR.< LoginAction >select( LoginAction@Server.SIGNUP );
                 return signUp();
             }
             case SIGNIN      -> {
                 chCS.< LoginAction >select( LoginAction@Client.SIGNIN );
                 chSR.< LoginAction >select( LoginAction@Server.SIGNIN );
                 return signUp();
             }
             case LOGOUT      -> {
                 chCS.< LoginAction >select( LoginAction@Client.LOGOUT );
                 chSR.< LoginAction >select( LoginAction@Server.LOGOUT );
                 logout();
                 return Optional@Client.<Token>empty();
             }
             default -> {
                 return Optional@Client.<Token>empty(); //this happens only if action is null
             }
         }
     }
*/

    public Optional@Client< Token > signUp(){
        String@Server name = cli.getUsername() >> chCS::< String >com;
        Boolean@Server isValidUsername = name
            >> chSR::< String >com
            >> db::isUserValid
            >> chSR::< Boolean >com;
        if( isValidUsername ){ // this check that the name is valid within the system
            chSR.< Result >select( Result@Server.OK );
            chCS.< Result >select( Result@Server.OK );
            String@Repository pswd = cli.promptPassword() >> chCS::< String >com >> chSR::< String >com;
            db.addUser( chSR.< String >com( name ), pswd );
            return sessionManager.createSession( name )
                >> chCS::< Token >com
                >> Optional@Client::< Token >of;
        } else {
            chSR.< Result >select( Result@Server.ERROR );
            chCS.< Result >select( Result@Server.ERROR );
            return Optional@Client.< Token >empty();
        }
    }

    public Optional@Client< Token > signIn() {
        String@Server username = cli.getUsername() >> chCS::< String >com;
        String@Repository name = username >> chSR::< String >com;
        String@Repository pswd = cli.promptPassword() >> chCS::< String >com >> chSR::< String >com;
        if( db.auth( name, pswd ) ) {
            chSR.< Result >select( Result@Repository.OK );
            chCS.< Result >select( Result@Server.OK );
        return sessionManager.createSession( username )
            >> chCS::< Token >com
            >> Optional@Client::< Token >of;
        } else {
            chSR.< Result >select( Result@Repository.ERROR );
            chCS.< Result >select( Result@Server.ERROR );
            return Optional@Client.< Token >empty();
        }
    }

    public void logout( Token@Client token ){
        token
            >> chCS::< Token >com
            >> sessionManager::closeSession;
    }

}