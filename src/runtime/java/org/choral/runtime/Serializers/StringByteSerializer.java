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

package org.choral.runtime.Serializers;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringByteSerializer implements ChoralSerializer< String, ByteBuffer > {

	private final Charset CHARSET = StandardCharsets.UTF_8;
	private static final StringByteSerializer INSTANCE = new StringByteSerializer();

	private StringByteSerializer(){}

	public static StringByteSerializer getInstance(){
		return INSTANCE;
	}

	@Override
	public < M extends String > ByteBuffer fromObject( M o ) {
		return ByteBuffer.wrap( o.getBytes( CHARSET ) );
	}

	@Override
	public < M extends String > M toObject( ByteBuffer byteBuffer ) {
		return ( M ) new String( byteBuffer.array(), CHARSET );
	}

}
