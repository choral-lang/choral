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

package name_mangling.other_example;

/*
public class Y@( D, E, F, G ){
	main( String@D args[] ){
		X@( D, E, F ) def = new X@( D, E, F )();
		X@( D, F, E ) dfe = new X@( D, F, E )();
		X@( E, D, F ) edf = new X@( E, D, F )();
		X@( E, F, D ) efd = new X@( E, F, D )();
		X@( F, D, E ) fde = new X@( F, D, E )();
		X@( F, E, D ) fed = new X@( F, E, D )();
		X@( G, D, E ) gde = new X@( G, D, E )();
		def.m( ""@F, ""@D ); def.m( ""@D, ""@E ); def.m( ""@F, ""@E ); def.m( ""@E, ""@F ), def.m( ""@D, ""@F );
		dfe.m( ""@E, ""@D ); dfe.m( ""@D, ""@F ); dfe.m( ""@E, ""@F ); dfe.m( ""@F, ""@E ), dfe.m( ""@D, ""@E );
		edf.m( ""@F, ""@E ); edf.m( ""@E, ""@F ); edf.m( ""@F, ""@D ); edf.m( ""@D, ""@F ), edf.m( ""@E, ""@F );
		efd.m( ""@D, ""@E ); efd.m( ""@E, ""@D ); efd.m( ""@D, ""@F ); efd.m( ""@F, ""@D ); efd.m( ""@E, ""@D );
		fde.m( ""@E, ""@F ); fde.m( ""@F, ""@D ); fde.m( ""@E, ""@D ); fde.m( ""@D, ""@E ); fde.m( ""@F, ""@E );
		fed.m( ""@D, ""@F ); fed.m( ""@F, ""@E ); fed.m( ""@D, ""@E ); fed.m( ""@E, ""@D ); fed.m( ""@F, ""@D );
		gde.m( ""@E, ""@G ); gde.m( ""@G, ""@D ); gde.m( ""@E, ""@D ); gde.m( ""@D, ""@E ); gde.m( ""@G, ""@E );
	}
}
*/

public class Z {

	public static class _1_2_3_4 {

		public static void main$1( String[] args ) {
			X._1_2_3 def = new X._1_2_3();
			X._1_2_3 dfe = new X._1_2_3();
			X._1_2_3 edf = new X._1_2_3();
			X._1_2_3 efd = new X._1_2_3();
			X._1_2_3 fde = new X._1_2_3();
			X._1_2_3 fed = new X._1_2_3();
			X._1_2_3 gde = new X._1_2_3();
			def.m$3$1( "", "" );
			def.m$1$2( "", "" );
			def.m$3$2( "", "" );
			def.m$2$3( "", "" );
			def.m$1$3( "", "" );
			dfe.m$3$1( "", "" );
			dfe.m$1$2( "", "" );
			dfe.m$3$2( "", "" );
			dfe.m$2$3( "", "" );
			dfe.m$1$3( "", "" );
			edf.m$3$1( "", "" );
			edf.m$1$2( "", "" );
			edf.m$3$2( "", "" );
			edf.m$2$3( "", "" );
			edf.m$1$3( "", "" );
			efd.m$3$1( "", "" );
			efd.m$1$2( "", "" );
			efd.m$3$2( "", "" );
			efd.m$2$3( "", "" );
			efd.m$1$3( "", "" );
			fde.m$3$1( "", "" );
			fde.m$1$2( "", "" );
			fde.m$3$2( "", "" );
			fde.m$2$3( "", "" );
			fde.m$1$3( "", "" );
			fed.m$3$1( "", "" );
			fed.m$1$2( "", "" );
			fed.m$3$2( "", "" );
			fed.m$2$3( "", "" );
			fed.m$1$3( "", "" );
			gde.m$3$1( "", "" );
			gde.m$1$2( "", "" );
			gde.m$3$2( "", "" );
			gde.m$2$3( "", "" );
			gde.m$1$3( "", "" );

		}
	}

	public static class _1_2_4_3 {
		public static void main$1( String[] args ) {
			_1_2_3_4.main$1( args );
		}
	}

	public static class _1_3_2_4 {
		public static void main$1( String[] args ) {
			_1_2_3_4.main$1( args );
		}
	}

	public static class _1_3_4_2 {
		public static void main$1( String[] args ) {
			_1_2_3_4.main$1( args );
		}
	}

	public static class _1_4_2_3 {
		public static void main$1( String[] args ) {
			_1_2_3_4.main$1( args );
		}
	}

	public static class _1_4_3_2 {
		public static void main$1( String[] args ) {
			_1_2_3_4.main$1( args );
		}
	}

	public static class _2_1_3_4 {
		public static void main$2( String[] args ) {
			_1_2_3_4.main$1( args );
		}
	}

	public static class _2_1_4_3 {
		public static void main$2( String[] args ) {
			_1_2_3_4.main$1( args );
		}
	}

	public static class _2_3_1_4 {
		public static void main$2( String[] args ) {
			_1_2_3_4.main$1( args );
		}
	}

	public static class _2_3_4_1 {
		public static void main$2( String[] args ) {
			_1_2_3_4.main$1( args );
		}
	}

	public static class _2_4_1_3 {
		public static void main$2( String[] args ) {
			_1_2_3_4.main$1( args );
		}
	}

	public static class _2_4_3_1 {
		public static void main$2( String[] args ) {
			_1_2_3_4.main$1( args );
		}
	}

	public static class _3_1_2_4 {
		public static void main$3( String[] args ) {
			_1_2_3_4.main$1( args );
		}
	}

	public static class _3_1_4_2 {
		public static void main$3( String[] args ) {
			_1_2_3_4.main$1( args );
		}
	}

	public static class _3_2_1_4 {
		public static void main$3( String[] args ) {
			_1_2_3_4.main$1( args );
		}
	}

	public static class _3_2_4_1 {
		public static void main$3( String[] args ) {
			_1_2_3_4.main$1( args );
		}
	}

	public static class _3_4_1_2 {
		public static void main$3( String[] args ) {
			_1_2_3_4.main$1( args );
		}
	}

	public static class _3_4_2_1 {
		public static void main$3( String[] args ) {
			_1_2_3_4.main$1( args );
		}
	}

	public static class _4_1_2_3 {
		public static void main$4( String[] args ) {
			_1_2_3_4.main$1( args );
		}
	}

	public static class _4_1_3_2 {
		public static void main$4( String[] args ) {
			_1_2_3_4.main$1( args );
		}
	}

	public static class _4_2_1_3 {
		public static void main$4( String[] args ) {
			_1_2_3_4.main$1( args );
		}
	}

	public static class _4_2_3_1 {
		public static void main$4( String[] args ) {
			_1_2_3_4.main$1( args );
		}
	}

	public static class _4_3_1_2 {
		public static void main$4( String[] args ) {
			_1_2_3_4.main$1( args );
		}
	}

	public static class _4_3_2_1 {
		public static void main$4( String[] args ) {
			_1_2_3_4.main$1( args );
		}
	}

}
