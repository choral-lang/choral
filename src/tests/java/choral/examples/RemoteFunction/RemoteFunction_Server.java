package choral.examples.RemoteFunction;
import java.util.function.Function;

import org.choral.lang.DataChannels.BiDataChannel_B;
import org.choral.lang.Unit;

class RemoteFunction_Server< T, R > {
	private BiDataChannel_B< T, R > ch;
	private Function < T, R > f;

	public RemoteFunction_Server( BiDataChannel_B < T, R > ch, Function < T, R > f ) {
		this.ch = ch;
		this.f = f;
	}

	public Unit call( Unit t ) {
		return call();
	}

	public Unit call() {
		return ch.< R >com( f.apply( ch.< T >com( Unit.id ) ) );
	}

}
