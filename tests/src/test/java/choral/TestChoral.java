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

//package choral.examples;
package choral;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.sun.jdi.Mirror;

import choral.TestChoral.TestType;

//import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

//import org.junit.jupiter.api.Test;

//import choral.choralUnit.annotations.Test;

//import org.junit.jupiter.params.ParameterizedTest;

public class TestChoral {

	private static class CompilationRequest {
		private final List< String > sourceFolder;
		private final String targetFolder;
		private final List< String > headersFolders;
		private final String symbol;
		private final List< String > worlds;
		private final String[] expectedResults;

		public CompilationRequest(
				List< String > sourceFolder,
				String targetFolder,
				List< String > headersFolders,
				String symbol,
				List< String > worlds,
				String... expectedResults
		) {
			this.sourceFolder = sourceFolder;
			this.targetFolder = targetFolder;
			this.headersFolders = headersFolders;
			this.symbol = symbol;
			this.worlds = worlds;
			this.expectedResults = expectedResults;
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

	enum TestType {
		MUSTPASS,
		MUSTFAIL,
		RUNTIME
	}

	static final List< String > ALL_WORLDS = Collections.singletonList( "" );

	public static String subFolder( String sourceFolder, String subFolder ) {
		return sourceFolder + File.separator + subFolder;
	}

	static final String sourceFolder = "tests/src/main/choral/examples";
	static final String targetFolder = "tests/src/main/java";
	static final String choralMainFolder = "tests/src/main/choral";
	static final String runtimeMainFolder = "runtime/src/main/choral";
	static final String choralUnitMainFolder = "choral-unit/src/main/choral" ;

	static final String mustFailFolder = "tests/src/main/choral/MustFail";
	static final String mustPassFolder = "tests/src/main/choral/MustPass";
	static final String runtimeFolder = "tests/src/main/choral/Runtime";

	public static void main( String[] args ) {

		final String HelloRoles = "HelloRoles";
		final String BiPair = "BiPair";
		final String ConsumeItems = "ConsumeItems";
		final String ExtendsTest = "MyExtClass";
		final String MultiFoo = "MultiFoo";
		final String RemoteFunction = "RemoteFunction";
		final String AuthResult = "AuthResult";
		final String DistAuth = "DistAuth";
		final String DistAuthTest = "DistAuthTest";
		final String DistAuth5 = "DistAuth5";
		final String DistAuth10 = "DistAuth10";
		final String VitalsStreaming = "VitalsStreaming";
		final String VitalsStreamingTest = "VitalsStreamingTest";
		final String Mergesort = "Mergesort";
		final String MergesortTest = "MergesortTest";
		final String BuyerSellerShipper = "BuyerSellerShipper";
		final String Quicksort = "Quicksort";
		final String QuicksortTest = "QuicksortTest";
		final String Karatsuba = "Karatsuba";
		final String KaratsubaTest = "KaratsubaTest";
		final String DiffieHellman = "DiffieHellman";
		final String RetwisLoginManager = "RetwisLoginManager";
		final String Retwis = "Retwis";
		final String If_MultiWorld = "If_MultiWorld";
		final String TestSwitch = "TestSwitch";
		
		final String WrongType = "WrongType";
		final String SwitchTest = "SwitchTest";
		final String VariableDeclarations = "VariableDeclarations";
		final String CyclicInheritanceA = "CyclicInheritance_A"; 
		final String CyclicInheritanceB = "CyclicInheritance_B"; 
		final String LotsOfErrors = "A";
		final String MirrorChannel = "MirrorChannel";
		final String LoggerExample = "B";
		final String IfDesugar = "IfDesugarTest";
		final String IllegalInheritance = "TwoWorldList";
		final String NonMatchingReturnType = "C4";
		final String Channel = "JSONChannel";
		final String ChainingOperator = "ChainingExample";
		final String AutoBoxing = "Autoboxing";

		List< CompilationRequest > allCompilationRequests = Stream.of(
				new CompilationRequest(
						List.of( subFolder(mustFailFolder, "WrongType")),
						targetFolder,
						Collections.emptyList(),
						WrongType, ALL_WORLDS, "StaticVerificationException", "required type 'java.lang.String@(A)', found 'int@(A)'") 
				,
				new CompilationRequest(
						List.of( subFolder(mustPassFolder, "HelloRoles") ),
						targetFolder,
						Collections.emptyList(),
						HelloRoles, ALL_WORLDS )
				,
				new CompilationRequest(
						List.of( subFolder( sourceFolder, "BiPair") ),
						targetFolder,
						Collections.emptyList(),
						BiPair, ALL_WORLDS )
				,
				new CompilationRequest(
						List.of( subFolder(mustPassFolder, "ConsumeItems") ),
						targetFolder,
						Collections.emptyList(),
						ConsumeItems, ALL_WORLDS )
				,
				new CompilationRequest(
						List.of( subFolder( mustFailFolder, "MultiFoo") ),
						targetFolder,
						Collections.emptyList(),
						MultiFoo, ALL_WORLDS, "StaticVerificationException", "cannot reference 'super' before supertype constructor has been called" )
				,
				new CompilationRequest(
						List.of( subFolder(mustPassFolder, "ExtendsTest")),
						targetFolder,
						Collections.emptyList(),
						ExtendsTest, ALL_WORLDS )
				,
				new CompilationRequest(
						List.of( subFolder( mustPassFolder, "RemoteFunction") ),
						targetFolder,
						Collections.emptyList(),
						RemoteFunction, ALL_WORLDS )
				,
				new CompilationRequest(
						List.of( subFolder( mustPassFolder, "AuthResult" ) ),
						targetFolder,
						//Collections.emptyList(),
						List.of( subFolder( mustPassFolder, "DistAuth" ), subFolder( mustPassFolder, "BiPair" ) ),
						AuthResult, ALL_WORLDS )
				,
				new CompilationRequest(
						List.of( subFolder( mustPassFolder, "DistAuth" ),
								subFolder( mustPassFolder, "AuthResult" ),
								subFolder( mustPassFolder, "BiPair" )
						),
						targetFolder,
						List.of(
								subFolder( mustPassFolder, "DistAuth" ),
//								"src/tests/choral/examples/AuthResult",
//								"src/tests/choral/examples/BiPair",
								runtimeMainFolder,
								choralUnitMainFolder
						),
						DistAuth, ALL_WORLDS )
				,
				new CompilationRequest(
						List.of( subFolder( sourceFolder, "DistAuth" ) ),
						targetFolder,
						List.of(
								choralMainFolder + "/examples/DistAuth",
								choralMainFolder + "/examples/AuthResult",
								choralMainFolder + "/examples/BiPair",
								runtimeMainFolder,
								choralUnitMainFolder
						),
						DistAuthTest, ALL_WORLDS )
				,
				new CompilationRequest(
						List.of( subFolder( sourceFolder, "DistAuth" ) ),
						targetFolder,
						List.of(
								choralMainFolder + "/examples/DistAuth",
								choralMainFolder + "/examples/AuthResult",
								choralMainFolder + "/examples/BiPair",
								runtimeMainFolder,
								choralUnitMainFolder
						),
						DistAuth5, ALL_WORLDS )
				,
				new CompilationRequest(
						List.of( subFolder( sourceFolder, "DistAuth" ) ),
						targetFolder,
						List.of(
								choralMainFolder + "/examples/DistAuth",
								choralMainFolder + "/examples/AuthResult",
								choralMainFolder + "/examples/BiPair",
								runtimeMainFolder,
								choralUnitMainFolder
						),
						DistAuth10, ALL_WORLDS )
				,
				new CompilationRequest(
						List.of( subFolder( sourceFolder, "VitalsStreaming" ) ),
						targetFolder,
						List.of(
								choralMainFolder + "/examples/VitalsStreaming",
								runtimeMainFolder,
								choralUnitMainFolder
						),
						VitalsStreaming, ALL_WORLDS )
				,
				new CompilationRequest(
						List.of( subFolder( sourceFolder, "VitalsStreaming" ) ),
						targetFolder,
						List.of(
								choralMainFolder + "/examples/VitalsStreaming",
								runtimeMainFolder,
								choralUnitMainFolder
						),
						VitalsStreamingTest, ALL_WORLDS )
				,
				new CompilationRequest(
						List.of( subFolder( sourceFolder, "Mergesort" ) ),
						targetFolder,
						List.of(
								choralMainFolder + "/examples/Mergesort",
								runtimeMainFolder,
								choralUnitMainFolder
						),
						Mergesort, ALL_WORLDS )
				,
				new CompilationRequest(
						List.of( subFolder( sourceFolder, "Mergesort" ) ),
						targetFolder,
						List.of(
								choralMainFolder + "/examples/Mergesort",
								runtimeMainFolder,
								choralUnitMainFolder
						),
						MergesortTest, ALL_WORLDS )
				,
				new CompilationRequest(
						List.of( subFolder( mustPassFolder, "BuyerSellerShipper" ) ),
						targetFolder,
						List.of(
								subFolder( mustPassFolder, "BuyerSellerShipper" ),
								runtimeMainFolder,
								choralUnitMainFolder
						),
						BuyerSellerShipper, ALL_WORLDS )
				,
				new CompilationRequest(
						List.of( subFolder( sourceFolder, "Quicksort" ) ),
						targetFolder,
						List.of(
								choralMainFolder + "/examples/Quicksort",
								runtimeMainFolder,
								choralUnitMainFolder
						),
						Quicksort, ALL_WORLDS )
				,
				new CompilationRequest(
						List.of( subFolder( sourceFolder, "Quicksort" ) ),
						targetFolder,
						List.of(
								choralMainFolder + "/examples/Quicksort",
								runtimeMainFolder,
								choralUnitMainFolder
						),
						QuicksortTest, ALL_WORLDS )
				,
				new CompilationRequest(
						List.of( subFolder( sourceFolder, "Karatsuba" ) ),
						targetFolder,
						List.of(
								choralMainFolder + "/examples/Karatsuba",
								runtimeMainFolder,
								choralUnitMainFolder
						),
						Karatsuba, ALL_WORLDS )
				,
				new CompilationRequest(
				List.of( subFolder( sourceFolder, "Karatsuba" ) ),
				targetFolder,
				List.of(
						choralMainFolder + "/examples/Karatsuba",
						runtimeMainFolder,
						choralUnitMainFolder
				),
				KaratsubaTest, ALL_WORLDS )
				,
				new CompilationRequest( 
						List.of( subFolder(mustPassFolder, "DiffieHellman"), subFolder(mustPassFolder, "BiPair") ),
						targetFolder,
						Collections.emptyList(),
						DiffieHellman, ALL_WORLDS )
				// ,
				// new CompilationRequest(
				// 		List.of( subFolder( sourceFolder, "RetwisChoral" ) ),
				// 		targetFolder,
				// 		List.of(
				// 				choralMainFolder + "/examples/RetwisChoral",
				// 				runtimeMainFolder
				// 		),
				// 		RetwisLoginManager, ALL_WORLDS )
				,
				new CompilationRequest(
						List.of( subFolder( mustPassFolder, "RetwisChoral" ) ),
						targetFolder,
						List.of(
								subFolder( mustPassFolder, "RetwisChoral" ),
								runtimeMainFolder
						),
						Retwis, ALL_WORLDS )
				,
				new CompilationRequest(
					List.of( subFolder(mustFailFolder, "IfMultiWorld") ),
					targetFolder,
					List.of(
							runtimeMainFolder
					),
					If_MultiWorld, ALL_WORLDS, "ChoralException", "Found unprojectable expression. Right-hand side of short-circuited boolean expression contains multi-role objects: true@A && ch1.< Boolean >com( false@B )" )
				,
				new CompilationRequest(
						List.of( subFolder(mustPassFolder, "TestSwitch") ),
						targetFolder,
						Collections.emptyList(),
						TestSwitch, ALL_WORLDS )
				,
				new CompilationRequest(
						List.of( subFolder(mustFailFolder, "CyclicInheritance") ),
						targetFolder,
						Collections.emptyList(),
						CyclicInheritanceA, ALL_WORLDS, "StaticVerificationException", "Cyclic inheritance: 'CyclicInheritance_B@(W)' cannot extend 'CyclicInheritance_A@(W)'" )
				,
				new CompilationRequest(
						List.of( subFolder(mustFailFolder, "LotsOfErrors") ),
						targetFolder,
						Collections.emptyList(),
						LotsOfErrors, ALL_WORLDS, "StaticVerificationException", "duplicate role parameter 'W'" )
				,
				new CompilationRequest(
						List.of( subFolder(mustPassFolder, "SwitchTest") ),
						targetFolder,
						Collections.emptyList(),
						SwitchTest, ALL_WORLDS )
				,
				new CompilationRequest(
						List.of( subFolder(mustPassFolder, "VariableDeclarations") ),
						targetFolder,
						Collections.emptyList(),
						VariableDeclarations, ALL_WORLDS )
				,
				new CompilationRequest(
						List.of( subFolder(mustPassFolder, "MirrorChannel") ),
						targetFolder,
						Collections.emptyList(),
						MirrorChannel, ALL_WORLDS )
				,
				new CompilationRequest(
						List.of( subFolder(mustPassFolder, "LoggerExample") ),
						targetFolder,
						Collections.emptyList(),
						LoggerExample, ALL_WORLDS )
				,
				new CompilationRequest(
						List.of( subFolder(mustPassFolder, "IfDesugar") ),
						targetFolder,
						List.of(subFolder(mustPassFolder, "IfDesugar")),
						IfDesugar, ALL_WORLDS )
				,
				new CompilationRequest(
						List.of( subFolder(mustFailFolder, "IllegalInheritance") ),
						targetFolder,
						Collections.emptyList(),
						IllegalInheritance, ALL_WORLDS, "StaticVerificationException", "illegal inheritance, 'List@(W1)<Q>' and 'TwoWorldList@(W1,W2)<Q,R>' must have the same roles" )
				,
				new CompilationRequest(
						List.of( subFolder(mustFailFolder, "NonMatchingReturnType") ),
						targetFolder,
						Collections.emptyList(),
						NonMatchingReturnType, ALL_WORLDS, "StaticVerificationException", "method 'm(java.lang.Object@(X))' in 'foo.I3@(X)' clashes with method 'm(java.lang.Object@(X))' in 'foo.I0@(X)', attempting to use incompatible return type")
				// ,
				// new CompilationRequest(
				// 		List.of( subFolder(mustFailFolder, "Channel") ),
				// 		targetFolder,
				// 		Collections.emptyList(),
				// 		Channel, ALL_WORLDS)
				,
				new CompilationRequest(
						List.of( subFolder(mustPassFolder, "ChainingOperator") ),
						targetFolder,
						Collections.emptyList(),
						ChainingOperator, ALL_WORLDS)
				,
				new CompilationRequest(
						List.of( subFolder(mustPassFolder, "AutoBoxing") ),
						targetFolder,
						Collections.emptyList(),
						AutoBoxing, ALL_WORLDS)
		).toList();

		boolean notRun = true;

		List<String> passCompilationSymbols = Stream.of(
				HelloRoles,
				ConsumeItems,
				DiffieHellman,
				TestSwitch,
				RemoteFunction, 
				Retwis,
				AuthResult,
				BuyerSellerShipper,
				DistAuth,
				LoggerExample, 
//				VitalsStreaming//,
//				Mergesort//,
//				Quicksort//,
//				Karatsuba//,
//				DistAuth5//,
//				DistAuth10//,
				//ChainingOperator, // doesn't pass, test is poorly written
				IfDesugar,
				//VariableDeclarations,

				//SwitchTest, // https://github.com/choral-lang/choral/issues/29
				//MirrorChannel, // https://github.com/choral-lang/choral/issues/27
				//AutoBoxing, // https://github.com/choral-lang/choral/issues/28
				ExtendsTest
			).toList();

		List<String> failCompilationSymbols = Stream.of(
				IllegalInheritance,
				MultiFoo,
				CyclicInheritanceA,
				If_MultiWorld,
				LotsOfErrors,
				NonMatchingReturnType,
				WrongType
			).toList();

		if (Boolean.parseBoolean(System.getProperty("test.pass"))){
			notRun = false;
			runTests(passCompilationSymbols, allCompilationRequests, TestType.MUSTPASS);
		}
		if (Boolean.parseBoolean(System.getProperty("test.fail"))){
			notRun = false;
			runTests(failCompilationSymbols, allCompilationRequests, TestType.MUSTFAIL);
		}
		if (Boolean.parseBoolean(System.getProperty("test.runtime"))){
			notRun = false;
			System.out.println("Running runtime");
		}
		if (notRun){
			runTests(passCompilationSymbols, allCompilationRequests, TestType.MUSTPASS);
			runTests(failCompilationSymbols, allCompilationRequests, TestType.MUSTFAIL);
		}

//		generateCHH( headersRequest );
//		check( compilationRequests );
//		compilationRequests.forEach( c -> {
//			TestChoral.performanceProject( Collections.singletonList( c ), new HashMap<>() );
//		} );
//		projectionPerformance( compilationRequests );
//		compilationRequests.forEach( TestChoral::printProgramSizes );
//		version();

	}

	private static void runTests(List<String> symbols, List<CompilationRequest> compilationRequests, TestType testType){
		List< CompilationRequest > passCompilationRequests = symbols.stream()
		.map( s -> compilationRequests.stream().filter( c -> c.symbol.equalsIgnoreCase( s ) ).findFirst() )
		.filter( Optional::isPresent ).map( Optional::get ).toList();

		if (null != testType)
		switch (testType) {
                case MUSTPASS -> {
                    System.out.println("Now running tests that must pass");
                    passCompilationRequests.forEach( TestChoral::project );
                    System.out.println("Amount of tests ran: " + passCompilationRequests.size());
                    System.out.println("");
                    }
                case MUSTFAIL -> {
                    System.out.println("Now running tests that must fail");
                    passCompilationRequests.forEach( TestChoral::project );
                    System.out.println("Amount of tests ran: " + passCompilationRequests.size());
                    System.out.println("");
                    }
                case RUNTIME -> {
                    System.out.println("Now running runtime tests");
                    passCompilationRequests.forEach( TestChoral::project );
                    System.out.println("Amount of tests ran: " + passCompilationRequests.size());
                    System.out.println("");
                    }
                default -> {
                    }
            }
	}

	private static void printProgramSizes( CompilationRequest compilationRequest ){
		try {
			System.out.println( "Projecting");
			performanceProject( Collections.singletonList( compilationRequest ), new HashMap<>() );
			System.out.println( "Computing lines");
			Path sourcePath = Paths.get( compilationRequest.sourceFolder.get( 0 ) + "/" + compilationRequest.symbol + ".ch" );
			String source = Files.readString( sourcePath );
			int sourceSize = getProgramSize( source );
			System.out.println( "source " + sourcePath + " of size: " + sourceSize );
			Path targetPath = Paths.get( compilationRequest.targetFolder() + "/choral/examples/" + compilationRequest.symbol );

			int totalSize = Arrays.stream( targetPath.toFile().listFiles() )
					.filter( p -> p.getPath().indexOf( ".java" ) > 0 )
					.map( targetFile -> {
						try {
							String targetSource = Files.readString( targetFile.toPath().toAbsolutePath() );
							int psize = getProgramSize( targetSource );
							System.out.println( "source " + targetFile + " of size: " + psize );
							return psize;
						} catch( IOException e ) {
							e.printStackTrace();
							return 0;
						}
					} ).reduce( Integer::sum ).orElse( 0 );
			System.out.println( "Java total LOCS: " + totalSize );
			int ratio = Math.round( Math.round( ( ( (double) totalSize - sourceSize )/sourceSize )*10000 )/100 );
			System.out.println( "Java total increase vs source: " + ratio + "% more" );
			System.out.println( compilationRequest.symbol + " & " + sourceSize + " & " + totalSize + " & " + ratio + "\\%" );
			System.out.println( "- - - - - - - - - - - - - - - -" );
		} catch( IOException e ) {
			e.printStackTrace();
		}
	}

	private static int getProgramSize( String p ){
		p = p.replaceAll( "\\n{2,}", "\n" );
		return p.split( "\\n" ).length;
	}

	private static void projectionPerformance( List< CompilationRequest > compilationRequests ) {
		boolean firstRun = true;
		for( CompilationRequest compilationRequest : compilationRequests ) {
			int runs = firstRun ? 2000 : 1000;
			int skip = firstRun ? 1000 : 0;
			int interval = runs / 100;
			firstRun = false;
			Map< String, ArrayList< Long > > log = new HashMap<>();
			System.out.println(
					"Compilation performance for symbol: " + compilationRequest.symbol );
			for( int i = 0; i < runs; i++ ) {
				if( i % interval == 0 )
					System.out.print(
							"\rRun: " + i + " of " + runs + " (" + ( 100 * i / runs ) + "%)" );
				performanceProject( Collections.singletonList( compilationRequest ), log );
			}
			System.out.print( "\rDone\n" );
			log.forEach( ( key, values ) -> {
				System.out.println( key + ": " + values.stream().skip( skip ).mapToLong(
						i -> i ).average().orElse( 0 ) / Math.pow( 10, 6 ) );
			} );

			System.out.println( "- - - - - - - - - - - - - - - -" );
		}
	}

//	private static void check( List< CompilationRequest > compilationRequests ) {
//		for( CompilationRequest compilationRequest : compilationRequests ) {
//			Choral.main( (String[]) ArrayUtils.addAll(
//					new String[] {
//							"check",
//							"--verbosity=DEBUG",
////							"--headers=src/tests/choral/Prelude",
//							compilationRequest.sourceFolder().get( 0 ) + File.separator + compilationRequest.symbol + ".ch"
//					},
//					new String[] {}
//			) );
//		}
//	}

	private static void check( List< CompilationRequest > compilationRequests ) {
		try {
			for( CompilationRequest compilationRequest : compilationRequests ) {
				ArrayList< String > parameters = new ArrayList<>();
				parameters.add( "check" );
				parameters.add( "--verbosity=DEBUG" );
				if( !compilationRequest.headersFolders().isEmpty() )
					parameters.add( "--headers=" + String.join( ":",
							compilationRequest.headersFolders() ) );
				parameters.add(
						compilationRequest.sourceFolder().get( 0 )
								+ File.separator
								+ compilationRequest.symbol + ".ch"
				);
				System.out.println( "Issuing command " + String.join( " ", parameters ) );
				Choral.main( parameters.toArray( new String[ 0 ] ) );
			}
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	private static void project( CompilationRequest compilationRequest ) {
		try {
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
			System.out.println( "Issuing command " + String.join( " ", parameters ));
			Choral.main( parameters.toArray( new String[ 0 ] ), compilationRequest.expectedResults);
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	private static void performanceProject(
			List< CompilationRequest > compilationRequests, Map< String, ArrayList< Long > > log
	) {
		try {
			for( CompilationRequest compilationRequest : compilationRequests ) {
				ArrayList< String > parameters = new ArrayList<>();
				parameters.add( "epp" );
				parameters.add( "--verbosity=DEBUG" );
				if( !compilationRequest.headersFolders().isEmpty() )
					parameters.add( "--headers=" + String.join( ":",
							compilationRequest.headersFolders() ) );
				parameters.add( "-t" );
				parameters.add( compilationRequest.targetFolder() );
				parameters.add( "-s" );
				parameters.add( String.join( ":", compilationRequest.sourceFolder() ) );
				parameters.add( compilationRequest.symbol() );
				parameters.addAll( compilationRequest.worlds() );
//				parameters.add( "--annotate" );
//				parameters.add( "--dry-run" );
//				System.out.println( "Issuing command " + String.join( " ", parameters ) );
				Choral.mainProfiler( parameters.toArray( new String[ 0 ] ), log );

			}
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	private static void version() {
		ArrayList< String > parameters = new ArrayList<>();
		parameters.add( "--version" );
		Choral.main( parameters.toArray( new String[ 0 ] ) );
	}

}
