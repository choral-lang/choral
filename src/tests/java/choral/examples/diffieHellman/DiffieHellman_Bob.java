package choral.examples.diffieHellman;
import choral.examples.BiPair.BiPair_B;
import choral.lang.Unit;
import choral.channels.SymDataChannel_B;
import java.math.BigInteger;
import choral.annotations.Choreography;

@Choreography( role = "Bob", name = "DiffieHellman" )
public class DiffieHellman_Bob {
	public static BiPair_B< BigInteger, BigInteger > exchangeKeys( SymDataChannel_B < BigInteger > channel, Unit aPrivKey, BigInteger bPrivKey, Unit aSharedGenerator, BigInteger bSharedGenerator, Unit aSharedPrime, BigInteger bSharedPrime ) {
		return exchangeKeys( channel, bPrivKey, bSharedGenerator, bSharedPrime );
	}

	public static BiPair_B < BigInteger, BigInteger > exchangeKeys( SymDataChannel_B < BigInteger > channel, BigInteger bPrivKey, BigInteger bSharedGenerator, BigInteger bSharedPrime ) {
		BigInteger bPubKey;
		bPubKey = bSharedGenerator.modPow( bPrivKey, bSharedPrime );
		BigInteger bRecvKey;
		bRecvKey = channel.< BigInteger >com( Unit.id );
		channel.< BigInteger >com( bPubKey );
		BigInteger bSharedKey;
		bSharedKey = bSharedGenerator.modPow( bRecvKey, bSharedPrime );
		return new BiPair_B < BigInteger, BigInteger >( Unit.id, bSharedKey );
	}
}
