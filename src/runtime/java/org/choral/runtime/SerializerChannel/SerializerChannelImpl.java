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

package org.choral.runtime.SerializerChannel;

import org.choral.channels.SymChannelImpl;
import org.choral.channels.SymDataChannelImpl;
import org.choral.runtime.Serializers.ChoralSerializer;
import org.choral.lang.Unit;

import java.nio.ByteBuffer;

public class SerializerChannelImpl implements SymChannelImpl< Object > {
	private final ChoralSerializer< Object, ByteBuffer > serializer;
	private final SymDataChannelImpl< ByteBuffer > channel;

	public SerializerChannelImpl ( ChoralSerializer< Object, ByteBuffer > serializer, SymDataChannelImpl< ByteBuffer > channel ){
		this.serializer = serializer;
		this.channel = channel;
	}

	@Override
	public < M > Unit com( M m ) {
		channel.com( serializer.fromObject( m ) );
		return Unit.id;
	}

	@Override
	public < M > M com( Unit u ) {
		return com();
	}

	@Override
	public < S > S com () {
		return serializer.toObject( channel.com( Unit.id ) );
	}

	@Override
	public < T extends Enum< T > > Unit select ( T m ) {
		return this.com( m );
	}

	@Override
	public < T extends Enum< T > > T select ( Unit m ) {
		return select();
	}

	@Override
	public < T extends Enum< T > > T select () {
		return this.com();
	}

}
