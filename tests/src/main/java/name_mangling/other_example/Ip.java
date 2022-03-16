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
interface Ip@( A, B, C ){
	String@A m( String@B _1, String@C _2 );
	String@B m( String@C _1, String@A _2 );
	String@C m( String@B _1, String@A _2 );
}
*/

public interface Ip {
	// 					  ABC
	public interface _1_2_3 {
		//		String@A m( String@B _1, String@C _2 );
		String m$2$3( String _1, String _2 );

		//		String@B m( String@C _1, String@A _2 );
		String m$3$1( String _1, String _2 );

		//		String@C m( String@B _1, String@A _2 );
		String m$2$1( String _1, String _2 );
	}

	// 					  ACB
	public interface _1_3_2 {
		//		String@A m( String@C _1, String@B _2 );
		String m$3$2( String _1, String _2 );

		//		String@C m( String@B _1, String@A _2 );
		String m$2$1( String _1, String _2 );

		//		String@B m( String@C _1, String@A _2 );
		String m$3$1( String _1, String _2 );
	}

	//					  BAC
	public interface _2_1_3 {
		//		String@B m( String@A _1, String@C _2 );
		String m$1$3( String _1, String _2 );

		//		String@A m( String@C _1, String@B _2 );
		String m$3$2( String _1, String _2 );

		//		String@C m( String@A _1, String@B _2 );
		String m$1$2( String _1, String _2 );
	}

	//					  BCA
	public interface _2_3_1 {
		//		String@B m( String@C _1, String@A _2 );
		String m$3$1( String _1, String _2 );

		//		String@C m( String@A _1, String@B _2 );
		String m$1$2( String _1, String _2 );

		//		String@A m( String@C _1, String@B _2 );
		String m$3$2( String _1, String _2 );
	}

	//                    CAB
	public interface _3_1_2 {
		//		String@C m( String@A _1, String@B _2 );
		String m$1$2( String _1, String _2 );

		//		String@A m( String@B _1, String@C _2 );
		String m$2$3( String _1, String _2 );

		//		String@B m( String@A _1, String@C _2 );
		String m$1$3( String _1, String _2 );
	}

	//  				  CBA
	public interface _3_2_1 {
		//		String@C m( String@B _1, String@A _2 );
		String m$2$1( String _1, String _2 );

		//		String@B m( String@A _1, String@C _2 );
		String m$1$3( String _1, String _2 );

		//		String@A m( String@B _1, String@C _2 );
		String m$2$3( String _1, String _2 );
	}


}
