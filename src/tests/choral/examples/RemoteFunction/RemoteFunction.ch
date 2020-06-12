package choral.examples.RemoteFunction;

import org.choral.lang.DataChannels.BiDataChannel;
import org.choral.lang.DataChannels.BiDataChannel;
import java.util.function.Function;


class RemoteFunction@( Client, Server )< T@X, R@Y > {
	private BiDataChannel@( Client, Server )< T, R > ch;
	private Function@Server< T, R > f;

	public RemoteFunction( BiDataChannel@( Client, Server )< T, R > ch, Function@Server< T, R > f )
		{ this.ch = ch; this.f = f; }

	public R@Client call( T@Client t) { return ch.< R >com( f.apply( ch.<T>com( t ) ) ); }
}
