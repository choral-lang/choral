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

package choral.runtime.TLSByteChannel.tlschannel.impl;

import choral.runtime.TLSByteChannel.tlschannel.BufferAllocator;

import java.nio.ByteBuffer;
import java.util.Optional;

public class BufferHolder {

	private final static byte[] zeros = new byte[ TlsChannelImpl.maxTlsPacketSize ];

	public final String name;
	public final BufferAllocator allocator;
	public final boolean plainData;
	public final int maxSize;
	public final boolean opportunisticDispose;

	public ByteBuffer buffer;
	public int lastSize;

	public BufferHolder(
			String name, Optional< ByteBuffer > buffer, BufferAllocator allocator, int initialSize,
			int maxSize, boolean plainData, boolean opportunisticDispose
	) {
		this.name = name;
		this.allocator = allocator;
		this.buffer = buffer.orElse( null );
		this.maxSize = maxSize;
		this.plainData = plainData;
		this.opportunisticDispose = opportunisticDispose;
		this.lastSize = buffer.map( b -> b.capacity() ).orElse( initialSize );
	}

	public void prepare() {
		if( buffer == null ) {
			buffer = allocator.allocate( lastSize );
		}
	}

	public boolean release() {
		if( opportunisticDispose && buffer.position() == 0 ) {
			return dispose();
		} else {
			return false;
		}
	}

	public boolean dispose() {
		if( buffer != null ) {
			allocator.free( buffer );
			buffer = null;
			return true;
		} else {
			return false;
		}
	}

	public void resize( int newCapacity ) {
		if( newCapacity > maxSize )
			throw new IllegalArgumentException(
					String.format( "new capacity (%s) bigger than absolute max size (%s)",
							newCapacity, maxSize ) );
//        logger.trace("resizing buffer {}, increasing from {} to {} (manual sizing)", name, buffer.capacity(), newCapacity);
		resizeImpl( newCapacity );
	}

	public void enlarge() {
		if( buffer.capacity() >= maxSize ) {
			throw new IllegalStateException(
					String.format( "%s buffer insufficient despite having capacity of %d", name,
							buffer.capacity() ) );
		}
		int newCapacity = Math.min( buffer.capacity() * 2, maxSize );
//        logger.trace("enlarging buffer {}, increasing from {} to {} (automatic enlarge)", name, buffer.capacity(), newCapacity);
		resizeImpl( newCapacity );
	}

	private void resizeImpl( int newCapacity ) {
		ByteBuffer newBuffer = allocator.allocate( newCapacity );
		buffer.flip();
		newBuffer.put( buffer );
		if( plainData ) {
			zero();
		}
		allocator.free( buffer );
		buffer = newBuffer;
		lastSize = newCapacity;
	}

	/**
	 * Fill with zeros the remaining of the supplied buffer. This method does
	 * not change the buffer position.
	 * <p>
	 * Typically used for security reasons, with buffers that contains
	 * now-unused plaintext.
	 */
	public void zeroRemaining() {
		buffer.mark();
		buffer.put( zeros, 0, buffer.remaining() );
		buffer.reset();
	}

	/**
	 * Fill the buffer with zeros. This method does not change the buffer position.
	 * <p>
	 * Typically used for security reasons, with buffers that contains
	 * now-unused plaintext.
	 */
	public void zero() {
		buffer.mark();
		buffer.position( 0 );
		buffer.put( zeros, 0, buffer.remaining() );
		buffer.reset();
	}

	public boolean nullOrEmpty() {
		return buffer == null || buffer.position() == 0;
	}

}
