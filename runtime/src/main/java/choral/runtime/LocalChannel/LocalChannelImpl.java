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

package choral.runtime.LocalChannel;

import choral.channels.SymChannelImpl;
import choral.lang.Unit;
import choral.runtime.Media.MessageQueue;

import java.util.concurrent.ExecutionException;

public class LocalChannelImpl implements SymChannelImpl< Object > {

	private final MessageQueue queueOut;
	private final MessageQueue queueIn;

	public LocalChannelImpl( MessageQueue queueOut, MessageQueue queueIn ) {
		this.queueOut = queueOut;
		this.queueIn = queueIn;
	}

	@Override
	public < M > M com( Unit x ) {
		return this.com();
	}

	@Override
	public < S > S com() {
		try {
			return queueIn.recv();
		} catch( ExecutionException | InterruptedException e ) {
			e.printStackTrace();
		}
		return null; // it should never happen
	}

	@Override
	public < M > Unit com( M m ) {
		queueOut.send( m );
		return Unit.id;
	}

	@Override
	public < M extends Enum< M > > Unit select( M m ) {
		return this.com( m );
	}

	@Override
	public < M extends Enum< M > > M select( Unit m ) {
		return this.select();
	}

	@Override
	public < T extends Enum< T > > T select() {
		return this.com();
	}

}
