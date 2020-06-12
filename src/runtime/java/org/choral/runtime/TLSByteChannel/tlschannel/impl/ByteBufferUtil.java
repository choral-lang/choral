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

package org.choral.runtime.TLSByteChannel.tlschannel.impl;

import java.nio.ByteBuffer;

public class ByteBufferUtil {

    public static void copy(ByteBuffer src, ByteBuffer dst, int length) {
        if (length < 0) {
            throw new IllegalArgumentException("negative length");
        }
        if (src.remaining() < length) {
            throw new IllegalArgumentException(
                    String.format("source buffer does not have enough remaining capacity (%d < %d)", src.remaining(), length));
        }
        if (dst.remaining() < length) {
            throw new IllegalArgumentException(
                    String.format("destination buffer does not have enough remaining capacity (%d < %d)", dst.remaining(), length));
        }
        if (length == 0) {
            return;
        }
        ByteBuffer tmp = src.duplicate();
        tmp.limit(src.position() + length);
        dst.put(tmp);
        src.position(src.position() + length);
    }

}
