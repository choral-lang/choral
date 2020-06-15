/*
 *   Copyright (C) 2019 by Saverio Giallorenzo <saverio.giallorenzo@gmail.com>
 *   Copyright (C) 2019 by Fabrizio Montesi <famontesi@gmail.com>
 *   Copyright (C) 2019 by Marco Peressotti <marco.peressotti@gmail.com>
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as
 *   published by the Free Software Foundation; either version 2 of the
 *   License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the
 *   Free Software Foundation, Inc.,
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 *   For details about the authors of this software, see the AUTHORS file.
 */

package org.choral.utils;

import java.util.Objects;
import java.util.function.Function;

public class Pair< L, R > {

	private final L left;
	private final R right;

	public Pair( L left, R right ) {
		this.left = left;
		this.right = right;
	}

	public static < L, R > Pair< L, R > of( L left, R right ) {
		return new Pair( left, right );
	}

	public static < L1, L2, R1, R2 > Function< Pair< L1, R1 >, Pair< L2, R2 > > lift(
			Function< L1, L2 > left, Function< R1, R2 > right
	) {
		return ( x ) -> Pair.of( left.apply( x.left ), right.apply( x.right ) );
	}

	@Override
	public boolean equals( Object o ) {
		if( this == o ) return true;
		if( o == null || getClass() != o.getClass() ) return false;
		Pair< ?, ? > pair = (Pair< ?, ? >) o;
		return Objects.equals( left, pair.left ) &&
				Objects.equals( right, pair.right );
	}

	@Override
	public int hashCode() {
		return Objects.hash( left, right );
	}

	public L left() {
		return left;
	}

	public R right() {
		return right;
	}
}
