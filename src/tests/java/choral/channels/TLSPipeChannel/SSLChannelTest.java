/*
 * Copyright (C) 2019 by Saverio Giallorenzo <saverio.giallorenzo@gmail.com>
 * Copyright (C) 2019 by Fabrizio Montesi <famontesi@gmail.com>
 * Copyright (C) 2019 by Marco Peressotti <marco.peressotti@gmail.com>
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

package choral.channels.TLSPipeChannel;

import choral.runtime.Media.PipedByteChannel;
import choral.runtime.TLSByteChannel.TSLByteChannel_A;
import choral.runtime.TLSByteChannel.TSLByteChannel_B;
import choral.runtime.Serializers.KryoSerializer;
import choral.lang.Unit;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_A;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_B;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class SSLChannelTest {

	public static SSLContext getSSLContext() throws NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, KeyManagementException, IOException {
			KeyStore keyStore = KeyStore.getInstance( KeyStore.getDefaultType() );
			KeyStore trustStore = KeyStore.getInstance( KeyStore.getDefaultType() );
			String password = "password";
			String keyStoreFile = "src/tests/java/choral/channels/keystore.jks";
			String trustStoreFile = "src/tests/java/choral/channels/truststore.ts";
			keyStore.load( new FileInputStream( keyStoreFile ), password.toCharArray() );
			trustStore.load( new FileInputStream( trustStoreFile ), password.toCharArray() );
			KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
			kmf.init( keyStore, password.toCharArray() );
			TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
			tmf.init( trustStore );
			SSLContext sslContext = SSLContext.getInstance( "TLSv1.3" );
			sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );
			return sslContext;
		}

	public static void main( String[] args ) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {

		SSLContext sslContext = getSSLContext();

		choral.utils.Pair< PipedByteChannel, PipedByteChannel > channels = PipedByteChannel.getConnectedChannels();

		new Thread( () -> {
			TSLByteChannel_A c = new TSLByteChannel_A( new WrapperByteChannel_A( channels.left() ), sslContext );
			c.com( KryoSerializer.getInstance().fromObject( new MyPair<>( "Hello", "World!" ) ) );
		}).start();
		new Thread( () -> {
			TSLByteChannel_B c = new TSLByteChannel_B( new WrapperByteChannel_B( channels.right() ), sslContext );
			MyPair<String, String> p = KryoSerializer.getInstance().toObject( c.com( Unit.id ) );
			System.out.println( "Server received " + p.left() + " " + p.right() );
		}).start();

	}
}
