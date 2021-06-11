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

package choral.examples;

import org.apache.commons.lang.ArrayUtils;
import choral.compiler.Compiler;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestCompiler {

	static final List< String > ALL_WORLDS = Collections.singletonList( "" );

	public static void main( String[] args ) {

		String targetFolder = "src/tests/choral/examples";
		String destinationFolder = "src/tests/java/";

		Map< String, List< String > > targetTemplates = Stream.of(
//				new AbstractMap.SimpleEntry<>( "HelloRoles", ALL_WORLDS )
//				new AbstractMap.SimpleEntry<>( "BiPair", ALL_WORLDS )
//				new AbstractMap.SimpleEntry<>( "RemoteFunction", ALL_WORLDS ),
//				new AbstractMap.SimpleEntry<>( "ConsumeItems", ALL_WORLDS )
//				new AbstractMap.SimpleEntry<>( "AuthResult", ALL_WORLDS )
//				new AbstractMap.SimpleEntry<>( "BuyerSellerShipper", ALL_WORLDS )
//				new AbstractMap.SimpleEntry<>( "DistAuth5", ALL_WORLDS )
//				new AbstractMap.SimpleEntry<>( "DistAuth", ALL_WORLDS )
//				new AbstractMap.SimpleEntry<>( "DistAuthTest", ALL_WORLDS ),
//				new AbstractMap.SimpleEntry<>( "VitalsStreaming", ALL_WORLDS )
//				new AbstractMap.SimpleEntry<>( "VitalsStreamingTest", ALL_WORLDS ),
//				new AbstractMap.SimpleEntry<>( "MergeSort", ALL_WORLDS )
//				new AbstractMap.SimpleEntry<>( "MergesortTest", ALL_WORLDS ),
//				new AbstractMap.SimpleEntry<>( "QuickSort", ALL_WORLDS )
//				new AbstractMap.SimpleEntry<>( "QuicksortTest", ALL_WORLDS ),
//				new AbstractMap.SimpleEntry<>( "Karatsuba", ALL_WORLDS )
//				new AbstractMap.SimpleEntry<>( "KaratsubaTest", ALL_WORLDS )
				new AbstractMap.SimpleEntry<>( "HelloRoles", ALL_WORLDS )

		).collect( Collectors.toMap( Map.Entry::getKey, Map.Entry::getValue ) );
		try {
			for ( Map.Entry< String, List< String > > targetTemplate : targetTemplates.entrySet() ) {
				Compiler.main( ( String[] ) ArrayUtils.addAll(
						new String[]{
								"-annotate",
								"-d", destinationFolder,
								"-t", targetFolder,
								targetTemplate.getKey() },
						targetTemplate.getValue().toArray()
				) );
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}

	}

}
