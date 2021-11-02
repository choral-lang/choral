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

package name_mangling.BiPair;

/*
public class AuthResult@( A, B ) extends BiPair@( B, A )< Optional@B< AuthToken@B >, Optional@A< AuthToken@A > > {
}
*/

import choral.examples.DistAuthUtils.AuthToken;
import java.util.Optional;

public class AuthResult {

	public static class _1_2 extends BiPair._2_1< Optional< AuthToken > > {

		public _1_2 ( AuthToken left, AuthToken right ) {
			super( Optional.of( left ), Optional.of( right ) );
		}
		public _1_2 (){ super( Optional.empty(), Optional.empty() ); }
	}

	public static class _2_1 extends BiPair._1_2< Optional< AuthToken > > {
		public _2_1 ( AuthToken left, AuthToken right ) { super( Optional.of( left ), Optional.of( right ) ); }
		public _2_1 (){ super( Optional.empty(), Optional.empty() ); }
	}
}
