package choral.amend.diffiehellman;

import java.math.BigInteger;
import choral.channels.SymDataChannel;
import choral.amend.bipair.BiPair;

public class DiffieHellman@(Alice,Bob) {

    public static BiPair@(Alice,Bob)<BigInteger,BigInteger> exchangeKeys (
        SymDataChannel@(Alice,Bob)<Object> channel,
        BigInteger@Alice aPrivKey,
        BigInteger@Bob   bPrivKey,
        BigInteger@Alice aSharedGenerator,
        BigInteger@Bob   bSharedGenerator,
        BigInteger@Alice aSharedPrime,
        BigInteger@Bob   bSharedPrime
    ) {
        /* Step 1: compute public keys. */
        BigInteger@Alice aPubKey = aSharedGenerator.modPow( aPrivKey, aSharedPrime );
        BigInteger@Bob   bPubKey = bSharedGenerator.modPow( bPrivKey, bSharedPrime );
        /* Step 2: exchange public keys. */
        BigInteger@Bob   bRecvKey = aPubKey;
        BigInteger@Alice aRecvKey = bPubKey;
        /* Step 3:compute shared key. */
        BigInteger@Alice aSharedKey = aRecvKey.modPow( aPrivKey, aSharedPrime );
        BigInteger@Bob   bSharedKey = bRecvKey.modPow( bPrivKey, bSharedPrime );
        return new BiPair@(Alice,Bob)<BigInteger,BigInteger>( aSharedKey,bSharedKey );
    }

}