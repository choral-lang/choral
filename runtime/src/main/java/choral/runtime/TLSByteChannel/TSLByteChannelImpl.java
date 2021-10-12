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

package choral.runtime.TLSByteChannel;

import choral.runtime.ChoralByteChannel.SymByteChannelImpl;
import choral.runtime.TLSByteChannel.tlschannel.TlsChannel;
import choral.lang.Unit;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public abstract class TSLByteChannelImpl implements SymByteChannelImpl {
	protected TlsChannel channel;

	@Override
	public < T extends ByteBuffer > T com( Unit u ) {
		return this.com();
	}

	private ByteBuffer recvLoop( ByteBuffer recv ) throws IOException {
		return channel.read( recv ) != -1 ? recv : recvLoop( recv );
	}

	@Override
	public < T extends ByteBuffer > Unit com( T m ) {
		try {
			channel.write( m );
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		return Unit.id;
	}

	@Override
	public ByteChannel byteChannel(){
		return this.channel;
	}

	@Override
	public < T extends ByteBuffer > T com () {
		try {
			int BUFFER_SIZE = 4096;
			return ( T ) recvLoop( ByteBuffer.allocate( BUFFER_SIZE ) );
		} catch ( IOException e ) {
			throw new RuntimeException( e.getMessage() );
		}
	}
}
