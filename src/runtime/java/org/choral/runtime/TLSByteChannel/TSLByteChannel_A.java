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

package org.choral.runtime.TLSByteChannel;

import org.choral.lang.DataChannels.SymDataChannel1;
import org.choral.runtime.ChoralByteChannel.SymByteChannelImpl;
import org.choral.runtime.TLSByteChannel.tlschannel.ClientTlsChannel;

import javax.net.ssl.SSLContext;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public class TSLByteChannel_A extends TSLByteChannelImpl implements SymDataChannel1< ByteBuffer > {

	public TSLByteChannel_A( SymByteChannelImpl channel, SSLContext sslContext ) {
		this.channel = ClientTlsChannel
			.newBuilder( channel.byteChannel(), sslContext )
			.build();
	}

	public TSLByteChannel_A( ByteChannel channel, SSLContext sslContext ) {
		this.channel = ClientTlsChannel
			.newBuilder( channel, sslContext )
			.build();
	}

}
