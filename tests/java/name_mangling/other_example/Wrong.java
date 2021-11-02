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
THIS IS WRONG
class Wrong@( D, E, F, G ){
	main(){
		X@( D, E, F ) def = new X@( D, E, F )();
		X@( D, F, E ) dfe = new X@( D, F, E )();
		X@( E, D, F ) edf = new X@( E, D, F )();
		X@( E, F, D ) efd = new X@( E, F, D )();
		X@( F, D, E ) fde = new X@( F, D, E )();
		X@( F, E, D ) fed = new X@( F, E, D )();
		X@( G, D, E ) gde = new X@( G, D, E )();
		def.m( ""@F, ""@D ); def.m( ""@D, ""@E ); def.m( ""@F, ""@E ); def.m( ""@E, ""@F ), def.m( ""@D, ""@F );
				  13    21             11    22             13    22             12    23             11    23
		dfe.m( ""@E, ""@D ); dfe.m( ""@D, ""@F ); dfe.m( ""@E, ""@F ); dfe.m( ""@F, ""@E ), dfe.m( ""@D, ""@E );
		          12    21             11    23             12    23             13    22             11    22
		edf.m( ""@F, ""@E ); edf.m( ""@E, ""@F ); edf.m( ""@F, ""@D ); edf.m( ""@D, ""@F ), edf.m( ""@E, ""@F );
		          13    22             12    23             13    21             11    23             12    23
		efd.m( ""@D, ""@E ); efd.m( ""@E, ""@D ); efd.m( ""@D, ""@F ); efd.m( ""@F, ""@D ); efd.m( ""@E, ""@D );
		          11    22             12    21             11    23             13    21             12    21
		fde.m( ""@E, ""@F ); fde.m( ""@F, ""@D ); fde.m( ""@E, ""@D ); fde.m( ""@D, ""@E ); fde.m( ""@F, ""@E );
		          12    23             13    21             12    21             11    22             13    22
		fed.m( ""@D, ""@F ); fed.m( ""@F, ""@E ); fed.m( ""@D, ""@E ); fed.m( ""@E, ""@D ); fed.m( ""@F, ""@D );
		          11    23             13    22             11    22             12    21             13    21
		gde.m( ""@E, ""@G ); gde.m( ""@G, ""@D ); gde.m( ""@E, ""@D ); gde.m( ""@D, ""@E ); gde.m( ""@G, ""@E );
				  13    21             11    22             13    22             12    23             11    23
	}
}
*/

public class Wrong {
	public static class _1_2_3 {
		public static void main ( String[] args ) {
		 // HOW DO WE KNOW WHAT INNER CLASS WE SHOULD USE HERE?
//          THE INSTANTIATION BELOW IS WRONG,
//          HOW DO WE KNOW WHAT NESTED CLASS
//          WE SHOULD USE AT INSTANTIATION SITE?
			X._1_2_3 def = new X._1_2_3();
			X._1_3_2 dfe = new X._1_3_2();
			X._2_1_3 edf = new X._2_1_3();
			X._2_3_1 efd = new X._2_3_1();
			X._3_1_2 fde = new X._3_1_2();
			X._3_2_1 fed = new X._3_2_1();
			X._1_2_3 gde = new X._1_2_3(); // ????
			def.m$3$1( "", "" ); def.m$1$2( "", "" ); def.m$3$2( "", "" ); def.m$2$3( "", "" ); def.m$1$3( "", "" );
			dfe.m$2$1( "", "" ); dfe.m$1$3( "", "" ); dfe.m$2$3( "", "" ); dfe.m$3$2( "", "" ); def.m$1$2( "", "" );
			edf.m$3$2( "", "" ); edf.m$2$3( "", "" ); edf.m$3$1( "", "" ); edf.m$1$3( "", "" ); edf.m$2$3( "", "" );
			efd.m$1$2( "", "" ); efd.m$2$1( "", "" ); efd.m$1$3( "", "" ); efd.m$3$1( "", "" ); efd.m$2$1( "", "" );
			fde.m$2$3( "", "" ); fde.m$3$1( "", "" ); fde.m$2$1( "", "" ); fde.m$1$2( "", "" ); fde.m$3$2( "", "" );
			fed.m$1$3( "", "" ); fed.m$3$2( "", "" ); fed.m$1$2( "", "" ); fed.m$2$1( "", "" ); fed.m$3$1( "", "" );
			gde.m$3$1( "", "" ); gde.m$1$2( "", "" ); gde.m$3$2( "", "" ); gde.m$2$3( "", "" ); gde.m$1$3( "", "" ); // ???

		}
	}


}
