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
public class Usage@( A, B ){
 public static void main( String@A[] args ){
 	AuthResult@( A, B ) a1 = new AuthResult@( A, B )();
 	AuthResult@( A, B ) a2 = new AuthResult@( A, B )( new AuthToken@A( "1"@B ), new AuthToken@B( "1"@B ) );
 	AuthResult@( B, A ) a3 = new AuthResult@( B, A )();
 	AuthResult@( B, A ) a4 = new AuthResult@( B, A )( new AuthToken@B( "2"@B ), new AuthToken@A( "2"@A ) );
 }
}
*/

import choral.examples.DistAuthUtils.AuthToken;

public class Example {

	public static class _1_2 {
		public static void main ( String[] args ) {
			AuthResult._1_2 a1 = new AuthResult._1_2();
			AuthResult._1_2 a2 = new AuthResult._1_2( new AuthToken( "1" ), new AuthToken( "1" ) );
			AuthResult._1_2 a3 = new AuthResult._1_2();
			AuthResult._1_2 a4 = new AuthResult._1_2( new AuthToken( "2" ), new AuthToken( "2" ) );
		}
	}

}
