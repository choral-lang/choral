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

package Benchmarks.Java.Karatsuba;

public class Karatsuba {

	public static Long multiply( Long n1, Long n2 ) {
		if( n1 < 10 || n2 < 10 ) {
			return n1 * n2;
		} else {
			double m = Math.max( Math.log10( n1 ), Math.log10( n2 ) ) + 1;
			int m2 = Double.valueOf( m / 2 ).intValue();
			Integer splitter = Double.valueOf( Math.pow( 10, m2 ) ).intValue();
			Long h1 = n1 / splitter;
			Long l1 = n1 % splitter;
			Long h2 = n2 / splitter;
			Long l2 = n2 % splitter;
			Long z0 = Karatsuba.multiply( l1, l2 );
			Long z2 = Karatsuba.multiply( h1, h2 );
			Long z1 = Karatsuba.multiply( l1 + h1, l2 + h2 ) - z2 - z0;
			return z2 * splitter * splitter + z1 * splitter + z0;
		}
	}

}
