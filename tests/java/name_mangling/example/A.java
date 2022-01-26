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

package name_mangling.example;

/*
class A@( S, T ) extends B@( S, T ) implements I@( S, T ), I@( T, S ) {
	String@S m ( Object@T x ) {}
	String@T m ( String@S x ) {}
}
 */

public class A {

	public static class _1_2 extends B._1_2 implements I._1_2, I._2_1 {

		@Override
		public String m$2( Object x ) {
			return null;
		}

		@Override
		public String m$2( CharSequence x ) {
			return null;
		}

		@Override
		public String m$1( Object x ) {
			return null;
		}
	}

	public static class _2_1 extends B._2_1 implements I._1_2, I._2_1 {

		@Override
		public String m$1( Object x ) {
			return null;
		}

		@Override
		public String m$1( CharSequence x ) {
			return null;
		}

		@Override
		public String m$2( Object x ) {
			return null;
		}
	}

}
