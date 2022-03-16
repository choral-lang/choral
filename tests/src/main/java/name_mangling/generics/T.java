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

public class T@( A, B )< R@( X, Y ) extends Flippable@( X, Y ) >{

	R@( A, B ) p1;
	R@( B, A ) p2;

	T@( A, B )( R@( A, B ) p1, R@( B, A ) p2 ){
		this.p1 = p1;
		this.p2 = p2;
	}

	Flippable@( B, A ) flip( R@( A, B ) x ){
		return x.flip();
	}

}

*/

public class T {

	public static class _1_2< R extends Flippable._1_2 > {
		R p1;
		R p2;

		_1_2( R p1, R p2 ) {
			this.p1 = p1;
			this.p2 = p2;
		}

		Flippable._2_1 flip$1_2( R x ) {
			return x.flip();
		}

	}

	public static class _2_1< R extends Flippable._1_2 > {
		R p1;
		R p2;

		public _2_1( R p1, R p2 ) {
			this.p1 = p1;
			this.p2 = p2;
		}

		Flippable._2_1 flip$2_1( R x ) {
			return x.flip();
		}
	}

}
