public interface LoginManager@( Client, Server, Database ){

    public Optional@Client< Token > signUp();
    public Optional@Client< Token > signIn();
    public void logout( Token@Client token );

}
