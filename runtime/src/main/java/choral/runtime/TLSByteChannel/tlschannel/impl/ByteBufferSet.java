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

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ByteBufferSet {

	public final ByteBuffer[] array;
	public final int offset;
	public final int length;

	public ByteBufferSet(ByteBuffer[] array, int offset, int length) {
		if (array == null)
			throw new NullPointerException();
		if (array.length < offset)
			throw new IndexOutOfBoundsException();
		if (array.length < offset + length)
			throw new IndexOutOfBoundsException();
		for (int i = offset; i < offset + length; i++) {
			if (array[i] == null)
				throw new NullPointerException();
		}
		this.array = array;
		this.offset = offset;
		this.length = length;
	}

	public ByteBufferSet(ByteBuffer[] array) {
		this(array, 0, array.length);
	}

	public ByteBufferSet(ByteBuffer buffer) {
		this(new ByteBuffer[] { buffer });
	}

	public long remaining() {
		long ret = 0;
		for (int i = offset; i < offset + length; i++) {
			ret += array[i].remaining();
		}
		return ret;
	}

	public int putRemaining(ByteBuffer from) {
		int totalBytes = 0;
		for (int i = offset; i < offset + length; i++) {
			if (!from.hasRemaining())
				break;
			ByteBuffer dstBuffer = array[i];
			int bytes = Math.min(from.remaining(), dstBuffer.remaining());
			ByteBufferUtil.copy(from, dstBuffer, bytes);
			totalBytes += bytes;
		}
		return totalBytes;
	}


	public ByteBufferSet put(ByteBuffer from, int length) {
		if (from.remaining() < length) {
			throw new IllegalArgumentException();
		}
		if (remaining() < length) {
			throw new IllegalArgumentException();
		}
		int totalBytes = 0;
		for (int i = offset; i < offset + this.length; i++) {
			int pending = length - totalBytes;
            if (pending == 0)
                break;
			int bytes = Math.min(pending, (int) remaining());
			ByteBuffer dstBuffer = array[i];
			ByteBufferUtil.copy(from, dstBuffer, bytes);
			totalBytes += bytes;
		}
		return this;
	}

	public int getRemaining(ByteBuffer dst) {
		int totalBytes = 0;
		for (int i = offset; i < offset + length; i++) {
			if (!dst.hasRemaining())
				break;
			ByteBuffer srcBuffer = array[i];
			int bytes = Math.min(dst.remaining(), srcBuffer.remaining());
			ByteBufferUtil.copy(srcBuffer, dst, bytes);
			totalBytes += bytes;
		}
		return totalBytes;
	}

	public ByteBufferSet get(ByteBuffer dst, int length) {
		if (remaining() < length) {
			throw new IllegalArgumentException();
		}
		if (dst.remaining() < length) {
			throw new IllegalArgumentException();
		}
		int totalBytes = 0;
		for (int i = offset; i < offset + this.length; i++) {
			int pending = length - totalBytes;
			if (pending == 0)
			    break;
			ByteBuffer srcBuffer = array[i];
			int bytes = Math.min(pending, srcBuffer.remaining());
			ByteBufferUtil.copy(srcBuffer, dst, bytes);
			totalBytes += bytes;
		}
		return this;
	}

	public boolean hasRemaining() {
		return remaining() > 0;
	}

	public boolean isReadOnly() {
		for (int i = offset; i < offset + length; i++) {
			if (array[i].isReadOnly())
				return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "ByteBufferSet[array=" + Arrays.toString(array) + ", offset=" + offset + ", length=" + length + "]";
	}

}
