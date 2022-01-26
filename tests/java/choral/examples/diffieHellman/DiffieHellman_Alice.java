package choral.examples.diffieHellman;

import java.math.BigInteger;

import choral.lang.Unit;
import choral.channels.SymDataChannel_A;
import choral.examples.BiPair.BiPair_A;
import choral.annotations.Choreography;

@Choreography( role = "Alice", name = "DiffieHellman" )
public class DiffieHellman_Alice {
	public static BiPair_A< BigInteger, BigInteger > exchangeKeys(
			SymDataChannel_A< BigInteger > channel, BigInteger aPrivKey, Unit bPrivKey,
			BigInteger aSharedGenerator, Unit bSharedGenerator, BigInteger aSharedPrime,
			Unit bSharedPrime
	) {
		return exchangeKeys( channel, aPrivKey, aSharedGenerator, aSharedPrime );
	}

	public static BiPair_A< BigInteger, BigInteger > exchangeKeys(
			SymDataChannel_A< BigInteger > channel, BigInteger aPrivKey,
			BigInteger aSharedGenerator, BigInteger aSharedPrime
	) {
		BigInteger aPubKey;
		aPubKey = aSharedGenerator.modPow( aPrivKey, aSharedPrime );
		channel.< BigInteger >com( aPubKey );
		BigInteger aRecvKey;
		aRecvKey = channel.< BigInteger >com( Unit.id );
		BigInteger aSharedKey;
		aSharedKey = aSharedGenerator.modPow( aRecvKey, aSharedPrime );
		return new BiPair_A< BigInteger, BigInteger >( aSharedKey, Unit.id );
	}

}
