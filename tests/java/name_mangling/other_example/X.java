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
class X@( A, B, C ) implements Ip@( B, C, A ), Ip@( C, A, B ){
	String@B m( String@C _1, String@A _2 ){...} m_13_21
	String@C m( String@A _1, String@B _2 ){...} m_11_22 <---\
	String@A m( String@C _1, String@B _2 ){...} m_13_22		| Same Signature, but that's
															| fine, same also for Java
	String@C m( String@A _1, String@B _2 ); m_11_22 <-------/
	String@A m( String@B _1, String@C _2 ); m_12_23
	String@B m( String@A _1, String@C _2 ); m_11_23
}
*/

public class X {

	public static class _1_2_3 implements Ip._2_3_1, Ip._3_1_2 {

		@Override
		public String m$3$1 ( String _1, String _2 ) {
			return null;
		}

		@Override
		public String m$1$2 ( String _1, String _2 ) {
			return null;
		}

		@Override
		public String m$2$3 ( String _1, String _2 ) {
			return null;
		}

		@Override
		public String m$1$3 ( String _1, String _2 ) {
			return null;
		}

		@Override
		public String m$3$2 ( String _1, String _2 ) {
			return null;
		}
	}

	public static class _1_3_2 implements Ip._3_2_1, Ip._2_1_3 {

		@Override
		public String m$3$2 ( String _1, String _2 ) {
			return null;
		}

		@Override
		public String m$1$2 ( String _1, String _2 ) {
			return null;
		}

		@Override
		public String m$2$1 ( String _1, String _2 ) {
			return null;
		}

		@Override
		public String m$1$3 ( String _1, String _2 ) {
			return null;
		}

		@Override
		public String m$2$3 ( String _1, String _2 ) {
			return null;
		}
	}


	public static class _2_1_3 implements Ip._1_3_2, Ip._3_2_1 {

		@Override
		public String m$3$2 ( String _1, String _2 ) {
			return null;
		}

		@Override
		public String m$2$1 ( String _1, String _2 ) {
			return null;
		}

		@Override
		public String m$1$3 ( String _1, String _2 ) {
			return null;
		}

		@Override
		public String m$2$3 ( String _1, String _2 ) {
			return null;
		}

		@Override
		public String m$3$1 ( String _1, String _2 ) {
			return null;
		}
	}

	public static class _2_3_1 implements Ip._3_1_2, Ip._1_2_3 {

		@Override
		public String m$3$1 ( String _1, String _2 ) {
			return null;
		}

		@Override
		public String m$2$1 ( String _1, String _2 ) {
			return null;
		}

		@Override
		public String m$1$2 ( String _1, String _2 ) {
			return null;
		}

		@Override
		public String m$2$3 ( String _1, String _2 ) {
			return null;
		}

		@Override
		public String m$1$3 ( String _1, String _2 ) {
			return null;
		}
	}

	public static class _3_1_2 implements Ip._1_2_3, Ip._2_3_1{

		@Override
		public String m$2$3 ( String _1, String _2 ) {
			return null;
		}

		@Override
		public String m$3$1 ( String _1, String _2 ) {
			return null;
		}

		@Override
		public String m$1$2 ( String _1, String _2 ) {
			return null;
		}

		@Override
		public String m$3$2 ( String _1, String _2 ) {
			return null;
		}

		@Override
		public String m$2$1 ( String _1, String _2 ) {
			return null;
		}
	}

	public static class _3_2_1 implements Ip._2_1_3, Ip._1_3_2 {

		@Override
		public String m$2$1 ( String _1, String _2 ) {
			return null;
		}

		@Override
		public String m$3$1 ( String _1, String _2 ) {
			return null;
		}

		@Override
		public String m$1$3 ( String _1, String _2 ) {
			return null;
		}

		@Override
		public String m$3$2 ( String _1, String _2 ) {
			return null;
		}

		@Override
		public String m$1$2 ( String _1, String _2 ) {
			return null;
		}
	}

}
