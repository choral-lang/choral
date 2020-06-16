package choral.examples.RemoteFunction;
import java.util.function.Function;

import org.choral.lang.DataChannels.BiDataChannel_A;
import org.choral.lang.Unit;

class RemoteFunction_Client< T, R > {
	private BiDataChannel_A < T, R > ch;

	public RemoteFunction_Client( BiDataChannel_A < T, R > ch, Unit f ) {
		this( ch );
	}

	public RemoteFunction_Client( BiDataChannel_A< T, R > ch ) {
		this.ch = ch;
	}

	public R call( T t ) {
		return ch.< R >com( ch.< T >com( t ) );
	}

}
