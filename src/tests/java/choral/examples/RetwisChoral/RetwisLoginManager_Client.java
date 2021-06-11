package choral.examples.RetwisChoral;
import java.util.Optional;
import choral.channels.SymChannel_A;
import choral.annotations.Choreography;
import choral.lang.Unit;

@Choreography( role = "Client", name = "RetwisLoginManager" )
public class RetwisLoginManager_Client {
	private SymChannel_A < Object > chCS;
	private CLI cli;

	public RetwisLoginManager_Client( SymChannel_A < Object > chCS, Unit chSR, CLI cli, Unit db, Unit sessionManager ) {
		this( chCS, cli );
	}
	
	public RetwisLoginManager_Client( SymChannel_A < Object > chCS, CLI cli ) {
		this.chCS = chCS;
		this.cli = cli;
	}

	public Optional < Token > signUp() {
		chCS.< String >com( cli.getUsername() );
		{
			switch( chCS.< Result >select( Unit.id ) ){
				default -> {
					throw new RuntimeException( "Received unexpected label from select operation" );
				}
				case ERROR -> {
					return Optional.< Token >empty();
				}
				case OK -> {
					chCS.< String >com( cli.promptPassword() );
					return Optional.< Token >of( chCS.< Token >com( Unit.id ) );
				}
			}
		}
	}
	
	public Optional < Token > signIn() {
		chCS.< String >com( cli.getUsername() );
		chCS.< String >com( cli.promptPassword() );
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
	
	public void logout( Token token ) {
		chCS.< Token >com( token );
	}

}
