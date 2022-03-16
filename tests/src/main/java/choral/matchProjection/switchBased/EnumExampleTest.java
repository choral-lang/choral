/*
 *     Copyright (C) 2019 by Saverio Giallorenzo <saverio.giallorenzo@gmail.com>
 *     Copyright (C) 2019 by Fabrizio Montesi <famontesi@gmail.com>
 *     Copyright (C) 2019 by Marco Peressotti <marco.peressotti@gmail.com>
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU Library General Public License as
 *     published by the Free Software Foundation; either version 2 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU Library General Public
 *     License along with this program; if not, write to the
 *     Free Software Foundation, Inc.,
 *     59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package choral.matchProjection.switchBased;

import java.util.Random;
import java.util.stream.IntStream;

public class EnumExampleTest {

	public static void main( String[] args ) {

		IntStream.range( 1, 10 ).forEach( ( e ) -> {
			String s = new EnumExampleTest().m();
			System.out.println( s );
		} );
	}


	String m() {
		String outer = "MONDO!";

		EnumExample< String > c;
		if( new Random().nextBoolean() ) {
			c = new EnumExample.EnumOption1<>( "CIAO" );
		} else {
			if( new Random().nextBoolean() ) {
				c = new EnumExample.EnumOption2<>();
			} else {
				c = new EnumExample.EnumOption3< String >( 1 );
			}
		}

		{
			Object t_exp1Eval = c;
			if( t_exp1Eval instanceof EnumExample.EnumOption1 ) {
				EnumExample.EnumOption1< String > x = (EnumExample.EnumOption1< String >) t_exp1Eval;
				return x.msg + " " + outer;
			}
			if( t_exp1Eval instanceof EnumExample.EnumOption3 ) {
				EnumExample.EnumOption3< String > x = (EnumExample.EnumOption3< String >) t_exp1Eval;
				return "Found error: " + x.error;
			}
		}
		return outer;
	}

}
