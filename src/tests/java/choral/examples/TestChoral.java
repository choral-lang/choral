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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestChoral {

	private static class CompilationRequest {
		private final List< String > sourceFolder;
		private final String targetFolder;
		private final List< String > headersFolders;
		private final String symbol;
		private final List< String > worlds;

		public CompilationRequest(
				List< String > sourceFolder, String targetFolder, List< String > headersFolders, String symbol,
				List< String > worlds
		) {
			this.sourceFolder = sourceFolder;
			this.targetFolder = targetFolder;
			this.headersFolders = headersFolders;
			this.symbol = symbol;
			this.worlds = worlds;
		}

		public List< String > sourceFolder() {
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

		public List< String > headersFolders() {
			return headersFolders;
		}
	}

	static final List< String > ALL_WORLDS = Collections.singletonList( "" );

	public static String subFolder( String sourceFolder, String subFolder ){
		return sourceFolder + File.separator + subFolder;
	}

	static String sourceFolder = "src/tests/choral/examples";
	static String targetFolder = "src/tests/java/";

	public static void main ( String[] args ) {

		List< CompilationRequest > compilationRequests = Stream.of(

//				new CompilationRequest(
//						List.of( subFolder( sourceFolder, "HelloRoles" ) ),
//						targetFolder,
//						Collections.emptyList(),
//						"HelloRoles", ALL_WORLDS )

//				new CompilationRequest(
//						List.of( subFolder( sourceFolder, "BiPair") ),
//						targetFolder,
//						Collections.emptyList(),
//						"BiPair", ALL_WORLDS )

//				new CompilationRequest(
//						List.of( subFolder( sourceFolder, "Foo") ),
//						targetFolder,
//						Collections.emptyList(),
//						"Foo", ALL_WORLDS )

//				new CompilationRequest(
//						List.of( subFolder( sourceFolder, "ConsumeItems" ) ),
//						targetFolder,
//						Collections.emptyList(),
//						"ConsumeItems", ALL_WORLDS )

//				new CompilationRequest(
//						List.of( subFolder( sourceFolder, "MultiFoo") ),
//						targetFolder,
//						Collections.emptyList(),
//						"MultiFoo", ALL_WORLDS )

//				new CompilationRequest(
//						List.of( subFolder( sourceFolder, "RemoteFunction") ),
//						targetFolder,
//						Collections.emptyList(),
//						"RemoteFunction", ALL_WORLDS )

//				new CompilationRequest(
//						List.of( subFolder( sourceFolder, "AuthResult" ) )
//						targetFolder,
//						List.of( "src/tests/choral/examples/BiPair", ""src/tests/choral/examples/DistAuth"" ),
//						"AuthResult", ALL_WORLDS )

//				new CompilationRequest(
//						List.of( subFolder( sourceFolder, "DistAuth" ) ),
//						targetFolder,
//						List.of(
//								"src/tests/choral/examples/DistAuth",
//								"src/tests/choral/examples/AuthResult",
//								"src/tests/choral/examples/BiPair",
//								"src/choralUnit/choral/TestUtils.chh"
//						),
//						"DistAuth", ALL_WORLDS )

//				new CompilationRequest(
//						List.of( subFolder( sourceFolder, "DistAuth" ) ),
//						targetFolder,
//						List.of(
//								"src/tests/choral/examples/DistAuth",
//								"src/tests/choral/examples/AuthResult",
//								"src/tests/choral/examples/BiPair",
//								"src/runtime/choral",
//								"src/choralUnit/choral"
//						),
//						"DistAuthTest", ALL_WORLDS )

//				new CompilationRequest(
//						List.of( subFolder( sourceFolder, "DistAuth" ) ),
//						targetFolder,
//						List.of(
//								"src/tests/choral/examples/DistAuth",
//								"src/tests/choral/examples/AuthResult",
//								"src/tests/choral/examples/BiPair",
//								"src/runtime/choral",
//								"src/choralUnit/choral"
//						),
//						"DistAuth5", ALL_WORLDS )

//				new CompilationRequest(
//						List.of( subFolder( sourceFolder, "DistAuth" ) ),
//						targetFolder,
//						List.of(
//								"src/tests/choral/examples/DistAuth",
//								"src/tests/choral/examples/AuthResult",
//								"src/tests/choral/examples/BiPair",
//								"src/runtime/choral",
//								"src/choralUnit/choral"
//						),
//						"DistAuth10", ALL_WORLDS )

//				new CompilationRequest(
//						List.of( subFolder( sourceFolder, "VitalsStreaming" ) ),
//						targetFolder,
//						List.of(
//								"src/tests/choral/examples/VitalsStreaming",
//								"src/runtime/choral",
//								"src/choralUnit/choral"
//						),
//						"VitalsStreaming", ALL_WORLDS )

//				new CompilationRequest(
//						List.of( subFolder( sourceFolder, "VitalsStreaming" ) ),
//						targetFolder,
//						List.of(
//								"src/tests/choral/examples/VitalsStreaming",
//								"src/runtime/choral",
//								"src/choralUnit/choral"
//						),
//						"VitalsStreamingTest", ALL_WORLDS )

//				new CompilationRequest(
//						List.of( subFolder( sourceFolder, "Mergesort" ) ),
//						targetFolder,
//						List.of(
//								"src/tests/choral/examples/Mergesort",
//								"src/runtime/choral",
//								"src/choralUnit/choral"
//						),
//						"Mergesort", ALL_WORLDS )


//				new CompilationRequest(
//						List.of( subFolder( sourceFolder, "Mergesort" ) ),
//						targetFolder,
//						List.of(
//								"src/tests/choral/examples/Mergesort",
//								"src/runtime/choral",
//								"src/choralUnit/choral"
//						),
//						"MergesortTest", ALL_WORLDS )

//				new CompilationRequest(
//						List.of( subFolder( sourceFolder, "BuyerSellerShipper" ) ),
//						targetFolder,
//						List.of(
//								"src/tests/choral/examples/BuyerSellerShipper",
//								"src/runtime/choral",
//								"src/choralUnit/choral"
//						),
//						"BuyerSellerShipper", ALL_WORLDS )

//				new CompilationRequest(
//						List.of( subFolder( sourceFolder, "Quicksort" ) ),
//						targetFolder,
//						List.of(
//								"src/tests/choral/examples/Quicksort",
//								"src/runtime/choral",
//								"src/choralUnit/choral"
//						),
//						"Quicksort", ALL_WORLDS )

//				new CompilationRequest(
//						List.of( subFolder( sourceFolder, "Quicksort" ) ),
//						targetFolder,
//						List.of(
//								"src/tests/choral/examples/Quicksort",
//								"src/runtime/choral",
//								"src/choralUnit/choral"
//						),
//						"QuicksortTest", ALL_WORLDS )

		new CompilationRequest(
				List.of( subFolder( sourceFolder, "Karatsuba" ) ),
				targetFolder,
				List.of(
						"src/tests/choral/examples/Karatsuba",
						"src/runtime/choral",
						"src/choralUnit/choral"
				),
				"Karatsuba", ALL_WORLDS )

//		new CompilationRequest(
//				List.of( subFolder( sourceFolder, "Karatsuba" ) ),
//				targetFolder,
//				List.of(
//						"src/tests/choral/examples/Karatsuba",
//						"src/runtime/choral",
//						"src/choralUnit/choral"
//				),
//				"KaratsubaTest", ALL_WORLDS )

		).collect( Collectors.toList() );

//		generateCHH( headersRequest ); // use exclusively because they call exit
//		check( compilationRequests ); // use exclusively because they call exit
		project( compilationRequests ); // use exclusively because they call exit
		System.out.println( "Projected Classes" );
	}

	private static void check( List< CompilationRequest > compilationRequests ){
		for ( CompilationRequest compilationRequest : compilationRequests ) {
			Choral.main( ( String[] ) ArrayUtils.addAll(
					new String[]{
							"check",
							"--verbosity=DEBUG",
//							"--headers=src/tests/choral/Prelude",
							compilationRequest.sourceFolder() + File.separator + compilationRequest.symbol + ".ch"
					},
					new String[]{}
			) );
		}
	}

	private static void project( List< CompilationRequest > compilationRequests ){
		try {
			for ( CompilationRequest compilationRequest : compilationRequests ) {
				ArrayList< String > parameters = new ArrayList<>();
				parameters.add( "epp" );
				parameters.add( "--verbosity=DEBUG" );
				if( !compilationRequest.headersFolders().isEmpty() )
					parameters.add( "--headers=" + String.join( ":", compilationRequest.headersFolders() ) );
				parameters.add( "-t" );
				parameters.add( compilationRequest.targetFolder() );
				parameters.add( "-s" );
				parameters.add( String.join( ":", compilationRequest.sourceFolder() ) );
				parameters.add( compilationRequest.symbol() );
				parameters.addAll( compilationRequest.worlds() );
				parameters.add( "--annotate" );
				System.out.println( "Issuing command " + String.join( " ", parameters ) );
				Choral.main( parameters.toArray( new String[ 0 ] ) );
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

}
