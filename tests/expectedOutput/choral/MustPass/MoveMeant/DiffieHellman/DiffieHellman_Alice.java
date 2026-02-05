package choral.MustPass.MoveMeant.DiffieHellman;

import choral.MustPass.BiPair.BiPair_A;
import choral.annotations.Choreography;
import choral.channels.SymDataChannel_A;
import choral.lang.Unit;
import java.math.BigInteger;

@Choreography( role = "Alice", name = "DiffieHellman" )
public class DiffieHellman_Alice {
	public static BiPair_A < BigInteger, BigInteger > exchangeKeys( SymDataChannel_A < Object > channel, BigInteger aPrivKey, Unit bPrivKey, BigInteger aSharedGenerator, Unit bSharedGenerator, BigInteger aSharedPrime, Unit bSharedPrime ) {
		return exchangeKeys( channel, aPrivKey, aSharedGenerator, aSharedPrime );
	}
	
	public static BiPair_A < BigInteger, BigInteger > exchangeKeys( SymDataChannel_A < Object > channel, BigInteger aPrivKey, BigInteger aSharedGenerator, BigInteger aSharedPrime ) {
		BigInteger aPubKey = aSharedGenerator.modPow( aPrivKey, aSharedPrime );
		channel.< BigInteger >com( aPubKey );
		BigInteger msg1 = channel.< BigInteger >com( Unit.id );
		BigInteger aRecvKey = msg1;
		BigInteger aSharedKey = aRecvKey.modPow( aPrivKey, aSharedPrime );
		return new BiPair_A < BigInteger, BigInteger >( aSharedKey, Unit.id );
	}

}
