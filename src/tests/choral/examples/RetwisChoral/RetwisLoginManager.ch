public class RetwisLoginManager@( Client, Server, Repository ) extends LoginManager@( Client, Server, Repository ){

     private SymChannel< Object >@( Client, Server ) chCS;
     private SymChannel< Object >@( Server, Repository ) chSR;
     private CLI@Client cli;
     private DatabaseConnection@Repository db;
     private SessionManager@Server sessionManager;

     public RetwisLoginManager(
         SymChannel< Object >@( Client, Server ) chCS,
         SymChannel< Object >@( Server, Repository ) chSR,
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

    public Optional< Token >@Client signUp(){
        String@Server name = cli.getUsername() >> chCS::< String >com;
        Boolean@Server isValidUsername
            >> chSR::< String >com
            >> db::isValidUsername
            >> chSR::< Boolean >com;
        if( isValidUsername ){ // this check that the name is valid within the system
            chCS.< Result >select( Result@R.OK );
            chSR.< Result >select( Result@R.OK );
            String pswd@Repository = cli.promptPassword() >> chCS::< String >com >> chSR::< String >com;
            db.addUser( name >> chSR::< String >com, pswd );
        return sessionManager.createSession( name )
            >> chCS::< Token >com
            >> Optional::< Token >of;
        } else {
            chCS.< Result >select( Result@R.ERROR );
            chSR.< Result >select( Result@R.ERROR );
            return Optional@C.empty();
        }
    }

    public Optional@C< Token > signIn() {
        String@Server username = cli.getUsername() >> chCS::< String >com;
        String@Repository name = name >> chSR::< String >com;
        String@Repository pswd = cli.pswd() >> chCS::< String >com >> chSR::< String >com;
        if( db.auth( name, pswd ) {
            chSR.< Result >select( Result@R.OK );
            chCS.< Result >select( Result@S.OK );
        return sessionManager.createSession( name )
            >> chCS::< Token >com
            >> Optional::< Token >of;
        } else {
            chSR.< Result >select( Result@R.ERROR );
            chCS.< Result >select( Result@S.ERROR );
            return Optional@C.empty();
        }
    }

    public void logout( Token@Client token ){
        token
            >> chCS::< Token >com
            >> sessionManager::closeSession;
    }

}