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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Mergesort {

	public static void main( String[] args ) {
		ArrayList< Integer > a = new ArrayList<>( Arrays.asList( 72, 15, 6, 19, 2, 13, 6 ) );
		System.out.println( new Mergesort().sort( a ) );
	}

	public List< Integer > sort( List< Integer > a ) {
		if( a.size() > 1 ) {
			Integer pivot = Double.valueOf( Math.floor( a.size() / 2 ) ).intValue();
			List< Integer > lhs = sort( a.subList( 0, pivot ) );
			List< Integer > rhs = sort( a.subList( pivot, a.size() ) );
			return merge( lhs, rhs );
		} else {
			return a;
		}

	}

	private List< Integer > merge( List< Integer > lhs, List< Integer > rhs ) {
		if( lhs.size() > 0 ) {
			if( rhs.size() > 0 ) {
				ArrayList< Integer > result = new ArrayList<>();
				if( lhs.get( 0 ) <= rhs.get( 0 ) ) {
					result.add( lhs.get( 0 ) );
					result.addAll( merge( lhs.subList( 1, lhs.size() ), rhs ) );
					return result;
				} else {
					result.add( rhs.get( 0 ) );
					result.addAll( merge( lhs, rhs.subList( 1, rhs.size() ) ) );
					return result;
				}
			} else {
				return lhs;
			}
		} else {
			return rhs;
		}
	}


}
