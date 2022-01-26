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

package choral.runtime.TLSByteChannel.tlschannel;

import choral.runtime.TLSByteChannel.tlschannel.util.DirectBufferDeallocator;

import java.nio.ByteBuffer;

/**
 * Allocator that creates direct buffers. The {@link #free(ByteBuffer)} method,
 * if called, deallocates the buffer immediately, without having to wait for GC
 * (and the finalizer) to run. Calling {@link #free(ByteBuffer)} is actually
 * optional, but should result in reduced memory consumption.
 * <p>
 * Direct buffers are generally preferred for using with I/O, to avoid an extra
 * user-space copy, or to reduce garbage collection overhead.
 */
public class DirectBufferAllocator implements BufferAllocator {

	private final DirectBufferDeallocator deallocator = new DirectBufferDeallocator();

	@Override
	public ByteBuffer allocate( int size ) {
		return ByteBuffer.allocateDirect( size );
	}

	@Override
	public void free( ByteBuffer buffer ) {
		// do not wait for GC (and finalizer) to run
		deallocator.deallocate( buffer );
	}

}
