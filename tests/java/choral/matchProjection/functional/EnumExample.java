/*
 *     Copyright (C) 2019 by Saverio Giallorenzo <saverio.giallorenzo@gmail.com>
 *     Copyright (C) 2019 by Fabrizio Montesi <famontesi@gmail.com>
 *     Copyright (C) 2019 by Marco Peressotti <marco.peressotti@gmail.com>
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU Library General Public License as
 *     published by the Free Software Foundation; either version 2 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU Library General Public
 *     License along with this program; if not, write to the
 *     Free Software Foundation, Inc.,
 *     59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package choral.matchProjection.functional;

import java.util.Optional;
import java.util.function.Function;

public abstract class EnumExample< T > {

	private EnumExample(){}

	public abstract < R > Optional< R > match( Function< EnumOption1< T >, Optional< R > > f1, Function< EnumOption2< T >, Optional< R > > f2 );

	public static final class EnumOption1< T > extends EnumExample< T > {

		final T msg;

		public EnumOption1( T msg ) {
			this.msg = msg;
		}

		public T msg() {
			return msg;
		}

		@Override
		public < R > Optional< R > match( Function< EnumOption1< T >, Optional< R > > f1, Function< EnumOption2< T >, Optional< R > > f2 ) {
			System.out.println( "Invoking match on EnumOption1" );
			return f1.apply( this );
		}
	}

	public static final class EnumOption2< T > extends EnumExample< T > {

		public EnumOption2(){}

		@Override
		public < R > Optional< R > match( Function< EnumOption1< T >, Optional< R > > f1, Function< EnumOption2< T >, Optional< R > > f2 ) {
			System.out.println( "Invoking match on EnumOption2" );
			return f2.apply( this );
		}

	}


}
