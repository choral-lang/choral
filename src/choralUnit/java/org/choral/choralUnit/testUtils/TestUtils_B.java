/*
 * Copyright (C) 2019-2020 by Saverio Giallorenzo <saverio.giallorenzo@gmail.com>
 * Copyright (C) 2019-2020 by Fabrizio Montesi <famontesi@gmail.com>
 * Copyright (C) 2019-2020 by Marco Peressotti <marco.peressotti@gmail.com>
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

package org.choral.choralUnit.testUtils;

import org.choral.channels.SymChannel_B;
import org.choral.runtime.TLSChannel.TLSChannel_B;
import org.choral.lang.Unit;

public class TestUtils_B {

	public static SymChannel_B< Object > newLocalChannel( String id ){
		return TestUtils.newLocalChannel( id ).right();
	}

	public static TLSChannel_B< Object > newLocalTLSChannel( String id ){
		return TestUtils.newLocalTLSChannel( id ).right();
	}

	public static SymChannel_B< Object > newLocalChannel( Unit o, String id ){
		return newLocalChannel( id );
	}

	public static TLSChannel_B< Object > newLocalTLSChannel( Unit o, String id ){
		return newLocalTLSChannel( id );
	}

}
