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
 * but WITHOUT ANY WARRANTY; without even the ied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package choral.BookSelling;

import choral.BookSelling.com.books.Catalogue;
import choral.channels.SSLChannelTest;
import choral.BookSelling.com.books.Price;
import org.choral.runtime.Media.PipedByteChannel;
import org.choral.runtime.WrapperByteChannel.WrapperByteChannel1;
import org.choral.runtime.WrapperByteChannel.WrapperByteChannel2;
import org.choral.utils.Pair;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

//import choral.BookSelling.BookSelling.BuyBook1;
//import choral.BookSelling.BookSelling.BuyBook3;

public class runChoreography< T > {

	public static void main( String[] args ) throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

//		MessageQueue m1 = new MessageQueue();
//		MessageQueue m2 = new MessageQueue();
//		LocalChannel1 c1 = new LocalChannel1( m1, m2 );
//		LocalChannel2 c2 = new LocalChannel2( m2, m1 );

		Pair< PipedByteChannel, PipedByteChannel > channels = PipedByteChannel.getConnectedChannels();
		WrapperByteChannel1 localChannel1 = new WrapperByteChannel1( channels.left() );
		WrapperByteChannel2 localChannel2 = new WrapperByteChannel2( channels.right() );
		SSLContext sslContext = SSLChannelTest.getSSLContext();

		Pair< PipedByteChannel, PipedByteChannel > channels2 = PipedByteChannel.getConnectedChannels();
		WrapperByteChannel1 localChannel21 = new WrapperByteChannel1( channels2.left() );
		WrapperByteChannel2 localChannel22 = new WrapperByteChannel2( channels2.right() );
		SSLContext sslContext1 = SSLChannelTest.getSSLContext();

		Catalogue catalogue = new Catalogue();
		catalogue.addTitle( "Kim", new Price( 100, "dkk" ) );

//		BuyBook1 seller = new BuyBook1(
//			new SerializerChannel1(
//				JSONSerializer.getInstance(), //				KryoSerializer.getInstance(),
//				new SSLChannel1( localChannel1, sslContext )
//			), Unit.id );
//		BuyBook2 buyer  = new BuyBook2(
//			new SerializerChannel2(
//				JSONSerializer.getInstance(), //				KryoSerializer.getInstance(),
//				new SSLChannel2( localChannel2, sslContext )
//			),
//			new SerializerChannel2(
//							//				KryoSerializer.getInstance(),
//							JSONSerializer.getInstance(),
//							new SSLChannel2( localChannel22, sslContext1 )
//			)
//		);
//		BuyBook3 bank  = new BuyBook3(
//						Unit.id,
//						new SerializerChannel1(
//							JSONSerializer.getInstance(), //				KryoSerializer.getInstance(),
//							new SSLChannel1( localChannel21, sslContext1 )
//						)
//		);
//		new Thread( () -> seller.run( catalogue, Unit.id ) ).start();
//		new Thread( () -> buyer.run( Unit.id, new Customer( "User", "123, Fake Street" ) ) ).start();
//		new Thread( () -> bank.run( Unit.id, Unit.id ) ).start();


	}

}
