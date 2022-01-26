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
abstract class B@( S, T ) implements I@( S, T ) {
	abstract String@S m ( Object@T x );
	abstract String@S m ( CharSequence@T x );
}
 */

abstract public class B {

	static abstract public class _1_2 implements I._1_2 {
		abstract public String m$2( Object x );

		abstract public String m$2( CharSequence x );
	}

	static abstract public class _2_1 implements I._2_1 {
		abstract public String m$1( Object x );

		abstract public String m$1( CharSequence x );
	}

}
