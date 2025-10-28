package choral.MustPass.diffieHellman;

import choral.lang.Unit;
import choral.annotations.Choreography;
import choral.MustPass.BiPair.BiPair_A;
import choral.channels.SymDataChannel_A;
import java.math.BigInteger;

@Choreography( role = "Alice", name = "DiffieHellman" )
public class DiffieHellman_Alice {
	public static BiPair_A < BigInteger, BigInteger > exchangeKeys( SymDataChannel_A < BigInteger > channel, BigInteger aPrivKey, Unit bPrivKey, BigInteger aSharedGenerator, Unit bSharedGenerator, BigInteger aSharedPrime, Unit bSharedPrime ) {
		return exchangeKeys( channel, aPrivKey, aSharedGenerator, aSharedPrime );
	}
	
	public static BiPair_A < BigInteger, BigInteger > exchangeKeys( SymDataChannel_A < BigInteger > channel, BigInteger aPrivKey, BigInteger aSharedGenerator, BigInteger aSharedPrime ) {
		BigInteger aPubKey = aSharedGenerator.modPow( aPrivKey, aSharedPrime );
		channel.< BigInteger >com( aPubKey );
		BigInteger aRecvKey = channel.< BigInteger >com( Unit.id );
		BigInteger aSharedKey = aSharedGenerator.modPow( aRecvKey, aSharedPrime );
		return new BiPair_A < BigInteger, BigInteger >( aSharedKey, Unit.id );
	}

}
