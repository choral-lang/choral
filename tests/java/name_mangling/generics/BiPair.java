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

package name_mangling.generics;

/*
public class BiPair@( A, B )< T@( X, Y ) > implements Flippable( A, B ){
	private T@( A, B ) left;
	private T@( B, A ) right;
	public BiPair@( A, B )( T@( A, B ) left, T@( B, A ) right ){
		this.left = left;
		this.right = right;
	}
	public BiPair@( B, A )< T@( A, B ) > flip(){
		return new BiPair@( B, A )< T@( A, B ) >( this.right, this.left );
	}
}

*/

public class BiPair implements Flippable {
	public static class _1_2< T > implements Flippable._1_2 {
		private T left;
		private T right;

		public _1_2( T left, T right ){
			this.left = left;
			this.right = right;
		}

		@Override
		public _2_1< T > flip () {
			return new _2_1< T >( right, left );
		}

	}

	public static class _2_1< T > implements Flippable._2_1 {
		private T left;
		private T right;

		public _2_1 ( T left, T right ) {
			this.left = left;
			this.right = right;
		}

		@Override
		public _1_2< T > flip () {
			return new _1_2< T >( right, left );
		}
	}

}
