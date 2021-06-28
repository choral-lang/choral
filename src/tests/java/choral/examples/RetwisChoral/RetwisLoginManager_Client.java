package choral.examples.RetwisChoral;
import choral.channels.SymChannel_A;
import choral.annotations.Choreography;
import java.util.Optional;
import choral.lang.Unit;

@Choreography( role = "Client", name = "RetwisLoginManager" )
public class RetwisLoginManager_Client {
	private SymChannel_A < Object > chCS;
	private CommandInterface commandInterface;

	public RetwisLoginManager_Client( SymChannel_A < Object > chCS, Unit chSR, CommandInterface commandInterface, Unit db, Unit sessionManager ) {
		this( chCS, commandInterface );
	}
	
	public RetwisLoginManager_Client( SymChannel_A < Object > chCS, CommandInterface commandInterface ) {
		this.chCS = chCS;
		this.commandInterface = commandInterface;
	}

	public Optional < Token > main( LoginAction action ) {
		switch( action ){
			case SIGNUP -> {
				chCS.< LoginAction >select( LoginAction.SIGNUP );
				return signUp();
			}
			case LOGOUT -> {
				chCS.< LoginAction >select( LoginAction.LOGOUT );
				logout();
				return Optional.< Token >empty();
			}
			case SIGNIN -> {
				chCS.< LoginAction >select( LoginAction.SIGNIN );
				return signIn();
			}
		}
		return Optional.< Token >empty();
	}
	
	public Optional < Token > signUp() {
		chCS.< String >com( commandInterface.getUsername() );
		{
			switch( chCS.< Result >select( Unit.id ) ){
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case ERROR -> {
					return Optional.< Token >empty();
				}
				case OK -> {
					chCS.< String >com( commandInterface.promptPassword() );
					return Optional.< Token >of( chCS.< Token >com( Unit.id ) );
				}
			}
		}
	}
	
	public Optional < Token > signIn() {
		chCS.< String >com( commandInterface.getUsername() );
		chCS.< String >com( commandInterface.promptPassword() );
		{
			switch( chCS.< Result >select( Unit.id ) ){
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case ERROR -> {
					return Optional.< Token >empty();
				}
				case OK -> {
					return Optional.< Token >of( chCS.< Token >com( Unit.id ) );
				}
			}
		}
	}
	
	public void logout() {
		chCS.< Token >com( commandInterface.getSessionToken() );
	}

}
