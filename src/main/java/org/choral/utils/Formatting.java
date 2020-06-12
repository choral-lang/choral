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

package org.choral.utils;

import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Formatting {

	public static Function< List< String >, String > joiningOxfordComma() {
		return Formatting::joiningOxfordComma;
	}

	public static String joiningOxfordComma( List< String > list ) {
		int last = list.size() - 1;
		if( last < 1 ) return String.join( "", list );
		if( last == 1 ) return String.join( " and ", list );
		return String.join( ", and ",
				String.join( ", ", list.subList( 0, last ) ),
				list.get( last ) );
	}

	public static Collector< CharSequence, ?, String > joining( CharSequence delimiter,
																CharSequence prefix,
																CharSequence suffix,
																String emptyValue ) {
		return Collector.of(
				() -> new StringJoiner( delimiter, prefix, suffix ).setEmptyValue( emptyValue ),
				StringJoiner::add, StringJoiner::merge,
				StringJoiner::toString );
	}

	public static String joiningQuotedAndOxfordComma( List< ? > list ) {
		return list.stream().map( x -> "'" + x + "'" )
				.collect( Collectors.collectingAndThen( Collectors.toList(),
						Formatting.joiningOxfordComma() ) );
	}

}
