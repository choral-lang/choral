package choral.examples.RemoteFunction;
import org.choral.lang.DataChannels.BiDataChannel1;
import org.choral.lang.Unit;
import org.choral.annotations.Choreography;

@Choreography( role = "Client", name = "RemoteFunction" )
class RemoteFunction1< T, R > {
	private BiDataChannel1 < T, R > ch;

	public RemoteFunction1( BiDataChannel1 < T, R > ch, Unit f ) {
		this( ch );
	}

	public RemoteFunction1( BiDataChannel1 < T, R > ch ) {
		this.ch = ch;
	}

	public R call( T t ) {
		return ch.< R >com( ch.< T >com( t ) );
	}

}
