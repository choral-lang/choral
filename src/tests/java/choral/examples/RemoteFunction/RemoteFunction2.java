package choral.examples.RemoteFunction;
import org.choral.lang.DataChannels.BiDataChannel2;
import java.util.function.Function;
import org.choral.lang.Unit;
import org.choral.annotations.Choreography;

@Choreography( role = "Server", name = "RemoteFunction" )
class RemoteFunction2< T, R > {
	private BiDataChannel2 < T, R > ch;
	private Function < T, R > f;

	public RemoteFunction2( BiDataChannel2 < T, R > ch, Function < T, R > f ) {
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
