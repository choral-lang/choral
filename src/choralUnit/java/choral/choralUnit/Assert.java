/*
 * Copyright (C) 2019-2020 by Saverio Giallorenzo <saverio.giallorenzo@gmail.com>
 * Copyright (C) 2019-2020 by Fabrizio Montesi <famontesi@gmail.com>
 * Copyright (C) 2019-2020 by Marco Peressotti <marco.peressotti@gmail.com>
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

package choral.choralUnit;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Assert {

	private static final String NEWLINE = "\n";
	private static final String TAB = "\t";
	private static final String SUCCESS = "ChoralUnit Test Success:";
	private static final String FAILURE = "ChoralUnit Test Failure:";

	public static void assertEquals ( Object o1, Object o2, String successMessage, String failureMessage ) {
		if ( o1 == null ) {
			throw new AssertException(
					FAILURE + failureMessage + NEWLINE
							+ "the first object of assertEquals cannot be not null" );
		}
		if ( !o1.equals( o2 ) ) {
			throw new AssertException(
					FAILURE + failureMessage + NEWLINE
							+ "assertion negated: "
							+ o1 + " of class " + o1.getClass()
							+ " is not equal to object "
							+ o2 + " of class " + o2.getClass()
					);
		}
		System.out.println( SUCCESS + NEWLINE + indent( successMessage ) );
	}

	public static void assertNotNull ( Object o1, String successMessage, String failureMessage ) {
		if ( o1 == null ) {
			throw new AssertException(
					FAILURE + failureMessage + NEWLINE
							+ "assertion negated: object is null"
			);
		}
		System.out.println( SUCCESS + NEWLINE + indent( successMessage ) );
	}

	private static String indent ( String s ) {
		return Stream.of( s.split( NEWLINE ) ).map( l -> TAB + l ).collect( Collectors.joining( NEWLINE ) );
	}
}
