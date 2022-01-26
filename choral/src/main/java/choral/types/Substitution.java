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

package choral.types;

public abstract class Substitution {

//    public abstract Collection<? extends World> worldParameters();
//
//    public abstract Collection<? extends HigherTypeParameter> typeParameters();

	public World get( World placeHolder ) {
		return placeHolder;
	}

	public HigherReferenceType get( HigherTypeParameter placeHolder ) {
		return placeHolder;
	}

	public static Substitution ID = new Substitution() {
	};

	Substitution andThen( Substitution s ) {
		return new Substitution() {
			private final Substitution s1 = Substitution.this;
			private final Substitution s2 = s;

			@Override
			public World get( World placeHolder ) {
				return s2.get( s1.get( placeHolder ) );
			}

			@Override
			public HigherReferenceType get( HigherTypeParameter placeHolder ) {
				HigherReferenceType x = s1.get( placeHolder );
				if( x instanceof HigherTypeParameter ) {
					x = s2.get( (HigherTypeParameter) x );
				}
				return x;
			}
		};
	}
}