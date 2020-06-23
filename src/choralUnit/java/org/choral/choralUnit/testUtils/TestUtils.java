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

package org.choral.choralUnit.testUtils;

import org.choral.channels.SymChannel_A;
import org.choral.channels.SymChannel_B;
import org.choral.runtime.LocalChannel.LocalChannel_A;
import org.choral.runtime.LocalChannel.LocalChannel_B;
import org.choral.runtime.Media.MessageQueue;
import org.choral.runtime.Media.PipedByteChannel;
import org.choral.runtime.TLSByteChannel.TSLByteChannel_A;
import org.choral.runtime.TLSByteChannel.TSLByteChannel_B;
import org.choral.runtime.Serializers.KryoSerializer;
import org.choral.runtime.TLSChannel.TLSChannel_A;
import org.choral.runtime.TLSChannel.TLSChannel_B;
import org.choral.runtime.WrapperByteChannel.WrapperByteChannel_A;
import org.choral.runtime.WrapperByteChannel.WrapperByteChannel_B;
import org.choral.utils.Pair;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

public class TestUtils {

	private final static Map< String, Pair< SymChannel_A< Object >, SymChannel_B< Object > > > channels = new HashMap<>();
	private final static Map< String, Pair< TLSChannel_A< Object >, TLSChannel_B< Object > > > tlsChannels = new HashMap<>();

	public static synchronized Pair< SymChannel_A< Object >, SymChannel_B< Object > > newLocalChannel( String id ){
		if( ! channels.containsKey( id ) ){
			MessageQueue m1 = new MessageQueue();
			MessageQueue m2 = new MessageQueue();
			channels.put( id, new Pair<>( new LocalChannel_A( m1, m2 ), new LocalChannel_B( m2, m1 ) ) );
			return channels.get( id );
		} else {
			return channels.remove( id );
		}
	}

	public static synchronized Pair< TLSChannel_A< Object >, TLSChannel_B< Object > > newLocalTLSChannel( String id ){
		if( ! tlsChannels.containsKey( id ) ){
			try {
				Pair< PipedByteChannel, PipedByteChannel > localChannels = PipedByteChannel.getConnectedChannels();
				WrapperByteChannel_A localChannel1 = new WrapperByteChannel_A( localChannels.left() );
				WrapperByteChannel_B localChannel2 = new WrapperByteChannel_B( localChannels.right() );
				SSLContext sslContext = getSSLContext();
				tlsChannels.put( id, new Pair<>(
						new TLSChannel_A<>( new TSLByteChannel_A( localChannel1, sslContext ), KryoSerializer.getInstance() ),
						new TLSChannel_B<>( new TSLByteChannel_B( localChannel2, sslContext ), KryoSerializer.getInstance() )
				) );
			} catch ( IOException | CertificateException | NoSuchAlgorithmException
					| UnrecoverableKeyException | KeyStoreException | KeyManagementException e ) {
				e.printStackTrace();
			}
			return tlsChannels.get( id );
		} else {
			return tlsChannels.remove( id );
		}
	}

	private static SSLContext getSSLContext() throws
			NoSuchAlgorithmException, KeyStoreException, CertificateException,
			UnrecoverableKeyException, KeyManagementException, IOException
	{
		KeyStore ks = KeyStore.getInstance( "JKS" );
		KeyStore ts = KeyStore.getInstance( "JKS" );
		String password = "password";
		String keyStoreFile = "src/tests/java/choral/channels/keystore.jks";
		String trustStoreFile = "src/tests/java/choral/channels/truststore.ts";
		ks.load( new FileInputStream( keyStoreFile ), password.toCharArray() );
		ts.load( new FileInputStream( trustStoreFile ), password.toCharArray() );
		KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
		kmf.init( ks, password.toCharArray() );
		TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
		tmf.init( ts );
		SSLContext sslContext = SSLContext.getInstance( "TLSv1.3" );
		sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );
		return sslContext;
	}

}
