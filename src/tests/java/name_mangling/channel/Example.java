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

package name_mangling.channel;

/*
public class Example@( A, B, C ){

	public static void main( String@A[] args ){

		LocalChannel@( A, B ) c1 = new LocalChannel@( A, B )();
		LocalChannel@( B, C ) c2 = new LocalChannel@( B, C )();
		LocalChannel@( A, C ) c3 = new LocalChannel@( A, C )();
		LocalChannel@( C, A ) c4 = new LocalChannel@( C, A )();

		Integer@B int_b = c1.< Integer >com( 5@A );
		Integer@C int_c = c2.< Integer >com( int_b );
		Integer@A int_a = c3.< Integer >com( int_c );
		int_c = c4.< Integer >com( int_a );

	}
}
*/

public class Example {

	public static class _1_2_3 {
		public static void main$1( String args ){
			LocalChannel._1_2 c1 = new LocalChannel._1_2(); // <--\
			LocalChannel._1_2 c2 = new LocalChannel._1_2(); // <---| How do we know the if constructor is _12 or _21?
			LocalChannel._1_2 c3 = new LocalChannel._1_2(); // <---| Here it is symmetric, but what happens if it is not the case (see other_example)?
			LocalChannel._1_2 c4 = new LocalChannel._1_2(); // <--/

			Integer int_b = c1.< Integer >com$1( 5 );
			Integer int_c = c2.< Integer >com$1( int_b );
			Integer int_a = c3.< Integer >com$2( int_c ); // <--- e.g., we need to know the type of int_c (Integer@B)
			                                               // and c3 (LocalChannel@( A, B )) to know that it's
			                                               // com_12
			int_c = c4.< Integer >com$2( int_a );
		}
	}

	public static class _1_3_2 {
		public static void main$1( String args ){
			Example._1_2_3.main$1( args );
		}
	}

	public static class _2_1_3 {
		public static void main$2( String args ){
			Example._1_2_3.main$1( args );
		}
	}

	public static class _2_3_1 {
		public static void main$2( String args ){
			Example._1_2_3.main$1( args );
		}
	}

	public static class _312 {
		public static void main$3( String args ){
			Example._1_2_3.main$1( args );
		}
	}

	public static class _3_2_1 {
		public static void main$3( String args ){
			Example._1_2_3.main$1( args );
		}
	}

}
