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
import org.choral.Choral;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestChoral {

	private static class CompilationRequest {
		private final String sourceFolder;
		private final String targetFolder;
		private final String symbol;
		private final List< String > worlds;

		public CompilationRequest(
				String sourceFolder, String targetFolder, String symbol,
				List< String > worlds
		) {
			this.sourceFolder = sourceFolder;
			this.targetFolder = targetFolder;
			this.symbol = symbol;
			this.worlds = worlds;
		}

		public String sourceFolder() {
			return sourceFolder;
		}

		public String targetFolder() {
			return targetFolder;
		}

		public String symbol() {
			return symbol;
		}

		public List< String > worlds() {
			return worlds;
		}
	}

	static final List< String > ALL_WORLDS = Collections.singletonList( "" );

	public static String subFolder( String sourceFolder, String subFolder ){
		return sourceFolder + File.separator + subFolder;
	}

	public static void main ( String[] args ) {

		String sourceFolder = "src/tests/choral/examples";
		String targetFolder = "src/tests/java/";

		List< CompilationRequest > compilationRequests = Stream.of(

//				new CompilationRequest( subFolder( sourceFolder, "HelloRoles" ), targetFolder, "HelloRoles", ALL_WORLDS )

//				new CompilationRequest( subFolder( sourceFolder, "BiPair"), targetFolder, "BiPair", ALL_WORLDS )

//				new CompilationRequest( subFolder( sourceFolder, "Foo"), targetFolder, "Foo", ALL_WORLDS )

				new CompilationRequest( subFolder( sourceFolder, "ConsumeItems"), targetFolder, "ConsumeItems", ALL_WORLDS )

//				new CompilationRequest( subFolder( sourceFolder, "RemoteFunction"), targetFolder, "RemoteFunction", ALL_WORLDS )

//				new CompilationRequest( subFolder( sourceFolder, "AuthResult"), targetFolder, "AuthResult", ALL_WORLDS )

//				new CompilationRequest( subFolder( sourceFolder, "BuyerSellerShipper"), targetFolder, "BuyerSellerShipper", ALL_WORLDS )

//				new CompilationRequest( subFolder( sourceFolder, "DistAuth"), targetFolder, "DistAuth", ALL_WORLDS )
//				new CompilationRequest( subFolder( sourceFolder, "DistAuth"), targetFolder, "DistAuth5", ALL_WORLDS )
//				new CompilationRequest( subFolder( sourceFolder, "DistAuth"), targetFolder, "DistAuth10", ALL_WORLDS )
//				new CompilationRequest( subFolder( sourceFolder, "DistAuth"), targetFolder, "DistAuthTest", ALL_WORLDS )

//				new CompilationRequest( subFolder( sourceFolder, "VitalsStreaming"), targetFolder, "VitalsStreaming", ALL_WORLDS )
//				new CompilationRequest( subFolder( sourceFolder, "VitalsStreaming"), targetFolder, "VitalsStreamingTest", ALL_WORLDS )

//				new CompilationRequest( subFolder( sourceFolder, "MergeSort"), targetFolder, "MergeSort", ALL_WORLDS )
//				new CompilationRequest( subFolder( sourceFolder, "MergeSort"), targetFolder, "MergeSortTest", ALL_WORLDS )

//				new CompilationRequest( subFolder( sourceFolder, "QuickSort"), targetFolder, "QuickSort", ALL_WORLDS )
//				new CompilationRequest( subFolder( sourceFolder, "QuickSort"), targetFolder, "QuickSortTest", ALL_WORLDS )

//				new CompilationRequest( subFolder( sourceFolder, "Karatsuba"), targetFolder, "Karatsuba", ALL_WORLDS )
//				new CompilationRequest( subFolder( sourceFolder, "Karatsuba"), targetFolder, "KaratsubaTest", ALL_WORLDS )

		).collect( Collectors.toList() );

//		check( compilationRequests );
		project( compilationRequests );
	}

	private static void check( List< CompilationRequest > compilationRequests ){
		for ( CompilationRequest compilationRequest : compilationRequests ) {
			Choral.main( ( String[] ) ArrayUtils.addAll(
					new String[]{
							"check",
							"--verbosity=DEBUG",
							"--headers=src/tests/choral/Prelude",
							compilationRequest.sourceFolder() + File.separator + compilationRequest.symbol + ".ch"
					},
					new String[]{}
			) );
		}
	}

	private static void project( List< CompilationRequest > compilationRequests ){
		try {
			for ( CompilationRequest compilationRequest : compilationRequests ) {
				Choral.main( ( String[] ) //ArrayUtils.addAll(
						new String[]{
								"epp",
								"--verbosity=DEBUG",
								"--headers=src/tests/choral/Prelude",
								"-t", compilationRequest.targetFolder(),
								"-s", compilationRequest.sourceFolder(),
								compilationRequest.symbol() }
//								,
//						compilationRequest.worlds().toArray()
//				)
			);
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

}
