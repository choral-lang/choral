package choral.examples.RemoteFunction;
import org.choral.lang.Unit;
import org.choral.annotations.Choreography;
import org.choral.channels.BiDataChannel_A;

@Choreography( role = "Client", name = "RemoteFunction" )
class RemoteFunction_Client< T, R > {
	private BiDataChannel_A < T, R > ch;

	public RemoteFunction_Client( BiDataChannel_A < T, R > ch, Unit f ) {
		this( ch );
	}
	
	public RemoteFunction_Client( BiDataChannel_A < T, R > ch ) {
		this.ch = ch;
	}

	public R call( T t ) {
		return ch.< R >com( ch.< T >com( t ) );
	}

}
