package choral.MustPass.MoveMeant.DiffieHellman;

import choral.MustPass.BiPair.BiPair_B;
import choral.annotations.Choreography;
import choral.channels.SymDataChannel_B;
import choral.lang.Unit;
import java.math.BigInteger;

@Choreography( role = "Bob", name = "DiffieHellman" )
public class DiffieHellman_Bob {
	public static BiPair_B < BigInteger, BigInteger > exchangeKeys( SymDataChannel_B < Object > channel, Unit aPrivKey, BigInteger bPrivKey, Unit aSharedGenerator, BigInteger bSharedGenerator, Unit aSharedPrime, BigInteger bSharedPrime ) {
		return exchangeKeys( channel, bPrivKey, bSharedGenerator, bSharedPrime );
	}
	
	public static BiPair_B < BigInteger, BigInteger > exchangeKeys( SymDataChannel_B < Object > channel, BigInteger bPrivKey, BigInteger bSharedGenerator, BigInteger bSharedPrime ) {
		BigInteger bPubKey = bSharedGenerator.modPow( bPrivKey, bSharedPrime );
		BigInteger msg0 = channel.< BigInteger >com( Unit.id );
		BigInteger bRecvKey = msg0;
		channel.< BigInteger >com( bPubKey );
		BigInteger bSharedKey = bRecvKey.modPow( bPrivKey, bSharedPrime );
		return new BiPair_B < BigInteger, BigInteger >( Unit.id, bSharedKey );
	}

}
