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

package generic;

public class StaticMethodTest< S > {

	static < S, R > R conv1(
			String i, S j
	) {    // <-- when m is static, there are no bindings between
		// the class-generics and the method generics.
		// TODO: this should be the same also for worlds, hence we cannot have them as parameters
		return null;
	}

	< R > R conv2( String i, S j ) {
		return null;
	}

//	static class MyTest< S >{
//
//		< R > R conv( String i, S j ){ // <-- this is instance-bound, so we can use the class-generics.
//			return null;
//		}
//
//	}
//
//	class MyTest2< S >{
//
//		< R > R conv( String i, S j ){ // <-- this is instance-bound, so we can use the class-generics
//			return null;
//		}
//
//	}

	interface Pair< T1, T2 > {
	}

	interface MyPair< T1 extends Integer, T2 > extends Pair< T2, T1 > {
	}

	public static void main( String[] args ) {
//		Test.< Integer, String >conv1( "ciao", 12 );
//		new Test< Integer >().< String >conv2( "ciao", 12 );


//		String s2 = new Test.MyTest< Integer >().< String >conv( "ciao", 12 );
//		String s3 = new Test< String >().new MyTest2< Integer >().< String >conv( "ciao", 12 );
	}

}
