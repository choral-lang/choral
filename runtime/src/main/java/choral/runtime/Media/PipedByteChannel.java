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

package choral.runtime.Media;

import choral.utils.Pair;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class PipedByteChannel implements BlockingByteChannel {

	private final ReadableByteChannel in;
	private final WritableByteChannel out;
	private boolean isOpen = true;

	public static Pair< PipedByteChannel, PipedByteChannel > getConnectedChannels() throws IOException {
		Pipe p1 = Pipe.open();
		Pipe p2 = Pipe.open();
		return new Pair<>(
				new PipedByteChannel( p1.source(), p2.sink() ),
				new PipedByteChannel( p2.source(), p1.sink() )
		);
	}

	private PipedByteChannel( ReadableByteChannel in, WritableByteChannel out ) {
		this.in = in;
		this.out = out;
	}

	@Override
	public int read( ByteBuffer dst ) throws IOException {
		return in.read( dst );
	}

	@Override
	public int write( ByteBuffer src ) throws IOException {
		return out.write( src );
	}

	@Override
	public boolean isOpen() {
		return isOpen;
	}

	@Override
	public void close() throws IOException {
		out.close();
		isOpen = false;
	}

	@Override
	public int recvTransmissionLength() throws IOException {
		ByteBuffer recv = ByteBuffer.allocate( 4 );
		in.read( recv );
		return recv.asIntBuffer().get();
	}

	@Override
	public void sendTransmissionLength( int length ) throws IOException {
		ByteBuffer snd = ByteBuffer.allocate( 4 ).putInt( length );
		out.write( snd );
	}
}
