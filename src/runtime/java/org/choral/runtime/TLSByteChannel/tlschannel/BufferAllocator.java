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

package org.choral.runtime.TLSByteChannel.tlschannel;

import java.nio.ByteBuffer;

/**
 * A factory for {@link ByteBuffer}s. Implementations are free to return heap or
 * direct buffers, or to do any kind of pooling. They are also expected to be
 * thread-safe.
 */
public interface BufferAllocator {

	/**
	 * Allocate a {@link ByteBuffer} with the given initial capacity.
	 */
	ByteBuffer allocate(int size);

	/**
	 * Deallocate the given {@link ByteBuffer}.
	 *
	 * @param buffer
	 *            the buffer to deallocate, that should have been allocated using
	 *            the same {@link BufferAllocator} instance
	 */
	void free(ByteBuffer buffer);

}
