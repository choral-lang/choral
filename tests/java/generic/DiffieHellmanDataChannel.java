/*
 * Copyright (C) 2019-2020 by Saverio Giallorenzo <saverio.giallorenzo@gmail.com>
 * Copyright (C) 2019-2020 by Fabrizio Montesi <famontesi@gmail.com>
 * Copyright (C) 2019-2020 by Marco Peressotti <marco.peressotti@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package generic;

import choral.channels.SymDataChannelImpl;
import choral.lang.Unit;
import choral.runtime.ChoralByteChannel.SymByteChannelImpl;
import choral.runtime.Media.PipedByteChannel;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_A;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_B;
import choral.runtime.WrapperByteChannel.WrapperByteChannelImpl;
import choral.utils.Pair;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

public class DiffieHellmanDataChannel implements SymDataChannelImpl< String > {

	private final SymByteChannelImpl channel;

	public DiffieHellmanDataChannel(
			SymByteChannelImpl channel
	) {
		this.channel = channel;
	}


	public static void main( final String[] args ) throws IOException {

//		DiffieHellmanDataChannel aliceDH = new DiffieHellmanDataChannel( null );
//		DiffieHellmanDataChannel bobDH = new DiffieHellmanDataChannel( null );
//
//		KeyPair aliceKeys = aliceDH.generateKeys();
//		KeyPair bobKeys = bobDH.generateKeys();
//
//		ByteBuffer aliceCommonSecret =
//				aliceDH.generateCommonSecret( aliceKeys.getPrivate(), bobKeys.getPublic() ); // this is a com
//		ByteBuffer bobCommonSecret =
//				bobDH.generateCommonSecret( bobKeys.getPrivate(), aliceKeys.getPublic() );  // this is a com
//
//		ByteBuffer encryptedMessage = aliceDH.encryptMessage( aliceCommonSecret, "Bob, guess who I am!" );
//		String message = bobDH.decryptMessage( bobCommonSecret, encryptedMessage );
//
//		System.out.println( message );

		Pair< PipedByteChannel, PipedByteChannel > channel = PipedByteChannel.getConnectedChannels();
		WrapperByteChannelImpl channel1 = new WrapperByteChannel_A( channel.left() );
		WrapperByteChannelImpl channel2 = new WrapperByteChannel_B( channel.right() );
		DiffieHellmanDataChannel aliceDHChannel = new DiffieHellmanDataChannel( channel1 );
		DiffieHellmanDataChannel bobDHChannel = new DiffieHellmanDataChannel( channel2 );

		new Thread( () -> aliceDHChannel.com( "This is a secret message" ) ).start();
		new Thread( () -> System.out.println( bobDHChannel.com() ) ).start();

	}

	private KeyPair generateKeys() {
		KeyPair keys = null;
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance( "DH" );
			keyPairGenerator.initialize( 1024 );
			keys = keyPairGenerator.generateKeyPair();
		} catch( NoSuchAlgorithmException e ) {
			e.printStackTrace();
		}
		return keys;
	}

	private ByteBuffer generateCommonSecret( PrivateKey privateKey, PublicKey publicKey ) {
		ByteBuffer commonSecretKey = null;
		try {
			KeyAgreement keyAgreement = KeyAgreement.getInstance( "DH" );
			keyAgreement.init( privateKey );
			keyAgreement.doPhase( publicKey, true );
			commonSecretKey = ByteBuffer.wrap( Arrays.copyOf( keyAgreement.generateSecret(), 8 ) );
		} catch( Exception e ) {
			e.printStackTrace();
		}
		return commonSecretKey;
	}

	private ByteBuffer encryptMessage( ByteBuffer secretKey, String message ) {
		ByteBuffer encryptedMessage = null;
		try {
			SecretKeySpec keySpec = new SecretKeySpec( secretKey.array(), "DES" );
			Cipher cipher = Cipher.getInstance( "DES/ECB/PKCS5Padding" );
			cipher.init( Cipher.ENCRYPT_MODE, keySpec );
			encryptedMessage = ByteBuffer.wrap( cipher.doFinal( message.getBytes() ) );
		} catch( Exception e ) {
			e.printStackTrace();
		}
		return encryptedMessage;
	}

	private < S extends String > S decryptMessage( ByteBuffer secretKey, ByteBuffer encMessage ) {
		String message = null;
		try {
			SecretKeySpec keySpec = new SecretKeySpec( secretKey.array(), "DES" );
			Cipher cipher = Cipher.getInstance( "DES/ECB/PKCS5Padding" );
			cipher.init( Cipher.DECRYPT_MODE, keySpec );
			message = new String( cipher.doFinal( encMessage.array() ) );
		} catch( Exception e ) {
			e.printStackTrace();
		}
		return ( S ) message;
	}

	private PublicKey buildPublicKey( ByteBuffer bytePublicKey ){
		X509EncodedKeySpec ks = new X509EncodedKeySpec( bytePublicKey.array() );
		KeyFactory keyFactory = null;
		PublicKey publicKey = null;
		try {
			keyFactory = KeyFactory.getInstance( "DSA" );
			publicKey = keyFactory.generatePublic( ks );
		} catch( NoSuchAlgorithmException | InvalidKeySpecException e ) {
			e.printStackTrace();
		}
		return publicKey;
	}

	@Override
	public < S extends String > Unit com( S message ) {
		KeyPair senderKeys = generateKeys();
		channel.com( ByteBuffer.wrap( senderKeys.getPublic().getEncoded() ) );
		ByteBuffer receiverBytePublicKey = channel.com();
		PublicKey receiverPublicKey = buildPublicKey( receiverBytePublicKey );
		ByteBuffer encryptedMessage = encryptMessage(
				generateCommonSecret( senderKeys.getPrivate(), receiverPublicKey ),
				message
		);
		return channel.com( encryptedMessage );
	}

	@Override
	public < S extends String > S com( Unit m ) {
		return com();
	}

	@Override
	public < S extends String > S com() {
		KeyPair receiverKeys = generateKeys();
		ByteBuffer senderBytePublicKey = channel.com();
		channel.com( ByteBuffer.wrap( receiverKeys.getPublic().getEncoded() ) );
		PublicKey senderPublicKey = buildPublicKey( senderBytePublicKey );
		return decryptMessage(
				generateCommonSecret( receiverKeys.getPrivate(), senderPublicKey ),
				channel.com()
		);
	}
}
