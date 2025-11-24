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

package choral;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;


public class TestChoral {

	private record CompilationRequest(List< String > sourceFolder,
									  String targetFolder,
									  List< String > headersFolders,
									  String symbol,
									  List< String > worlds,
									  List< String > sourcePaths,
									  List< String > classPaths) {}

	enum TestType {
		MUSTPASS,
		MUSTFAIL,
		RUNTIME
	}

	static final List< String > ALL_WORLDS = Collections.singletonList( "" );

	public static String subFolder( String sourceFolder, String subFolder ) {
		return sourceFolder + File.separator + subFolder;
	}

	/**Thi */
	private static final String FILESEPARATOR = System.getProperty( "path.separator" );
	private static final String PATHSEPARATOR = File.separator;
	private static final String SOURCE_FOLDER = "tests/src/main/choral/examples";
	private static final String TARGET_FOLDER = "projectedOutput";
	private static final String EXPECTED_FOLDER = "expectedOutput";
	private static final String RUNTIME_MAIN_FOLDER = "../runtime/src/main/choral";
	private static final String CHORALUNIT_MAIN_FOLDER = "../choral-unit/src/main/choral";
	private static final String MUSTFAIL_FOLDER = "src/main/choral/MustFail";
	private static final String MUSTPASS_FOLDER = "src/main/choral/MustPass";
	private static final String BASE_PATH = "base/src/main/java";
	private static final String RUNTIME_PATH = "runtime/src/main/java";
	private static final String EXPECTEDOUTPUT_PATH = "expectedOutput";

	// formatting for terminal output
	private static final String GREEN = "\u001B[32m";
	private static final String RED = "\u001B[31m";
	private static final String RESET = "\u001B[0m";
	private static final int COLUMN_WIDTH = 30;

	public static void main( String[] args ) {

		final String HelloRoles = "HelloRoles";
		final String BiPair = "BiPair";
		final String ConsumeItems = "ConsumeItems";
		final String ExtendsTest = "MyExtClass";
		final String MultiFoo = "MultiFoo";
		final String RemoteFunction = "RemoteFunction";
		final String AuthResult = "AuthResult";
		final String DistAuth = "DistAuth";
		final String BuyerSellerShipper = "BuyerSellerShipper";
		final String DiffieHellman = "DiffieHellman";
		final String TestSwitch = "TestSwitch";

		final String WrongType = "WrongType";
		final String SwitchTest = "SwitchTest";
		final String VariableDeclarations = "VariableDeclarations";
		final String CyclicInheritanceA = "CyclicInheritance_A";
		final String LotsOfErrors = "LotsOfErrors";
		final String MirrorChannel = "MirrorChannel";
		final String LoggerExample = "LoggerExample";
		final String IfDesugar = "IfDesugarTest";
		final String IllegalInheritance = "TwoWorldList";
		final String NonMatchingReturnType = "NonMatchingReturnType";
		final String ChainingOperator = "ChainingExample";
		final String AutoBoxing = "Autoboxing";
		final String BookSellingSoloist = "BuyBook2";

		List< CompilationRequest > allCompilationRequests = Stream.of(
				new CompilationRequest(
						List.of( subFolder( MUSTFAIL_FOLDER, "WrongType" ) ),
						TARGET_FOLDER,
						Collections.emptyList(),
						WrongType, ALL_WORLDS,
						List.of( BASE_PATH ),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder( MUSTPASS_FOLDER, "HelloRoles" ) ),
						TARGET_FOLDER,
						Collections.emptyList(),
						HelloRoles, ALL_WORLDS,
						List.of( BASE_PATH ),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder( SOURCE_FOLDER, "BiPair" ) ),
						TARGET_FOLDER,
						Collections.emptyList(),
						BiPair, ALL_WORLDS,
						List.of( BASE_PATH ),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder( MUSTPASS_FOLDER, "ConsumeItems" ) ),
						TARGET_FOLDER,
						Collections.emptyList(),
						ConsumeItems, ALL_WORLDS,
						List.of( BASE_PATH, RUNTIME_PATH ),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder( MUSTFAIL_FOLDER, "MultiFoo" ) ),
						TARGET_FOLDER,
						Collections.emptyList(),
						MultiFoo, ALL_WORLDS,
						Collections.emptyList(),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder( MUSTPASS_FOLDER, "ExtendsTest" ) ),
						TARGET_FOLDER,
						Collections.emptyList(),
						ExtendsTest, ALL_WORLDS,
						List.of( BASE_PATH ),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder( MUSTPASS_FOLDER, "RemoteFunction" ) ),
						TARGET_FOLDER,
						Collections.emptyList(),
						RemoteFunction, ALL_WORLDS,
						List.of( BASE_PATH, RUNTIME_PATH ),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder( MUSTPASS_FOLDER, "AuthResult" ),
								subFolder( MUSTPASS_FOLDER, "DistAuthUtils" ) ),
						TARGET_FOLDER,
						List.of( subFolder( MUSTPASS_FOLDER, "BiPair" ) ),
						AuthResult, ALL_WORLDS,
						List.of( BASE_PATH, EXPECTEDOUTPUT_PATH, RUNTIME_PATH ),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder( MUSTPASS_FOLDER, "DistAuth" ),
								subFolder( MUSTPASS_FOLDER, "DistAuthUtils" )
						),
						TARGET_FOLDER,
						List.of(
								subFolder( MUSTPASS_FOLDER, "DistAuth" ),
								subFolder( MUSTPASS_FOLDER, "AuthResult" ),
								subFolder( MUSTPASS_FOLDER, "BiPair" ),
								RUNTIME_MAIN_FOLDER,
								CHORALUNIT_MAIN_FOLDER
						),
						DistAuth, ALL_WORLDS,
						List.of( BASE_PATH, RUNTIME_PATH, EXPECTEDOUTPUT_PATH ),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder( MUSTPASS_FOLDER, "BuyerSellerShipper" ) ),
						TARGET_FOLDER,
						List.of(
								subFolder( MUSTPASS_FOLDER, "BuyerSellerShipper" ),
								RUNTIME_MAIN_FOLDER,
								CHORALUNIT_MAIN_FOLDER
						),
						BuyerSellerShipper, ALL_WORLDS,
						List.of( BASE_PATH, RUNTIME_PATH, EXPECTEDOUTPUT_PATH ),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder( MUSTPASS_FOLDER, "DiffieHellman" ),
								subFolder( MUSTPASS_FOLDER, "BiPair" ) ),
						TARGET_FOLDER,
						Collections.emptyList(),
						DiffieHellman, ALL_WORLDS,
						List.of( BASE_PATH, RUNTIME_PATH, EXPECTEDOUTPUT_PATH ),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder( MUSTPASS_FOLDER, "TestSwitch" ) ),
						TARGET_FOLDER,
						Collections.emptyList(),
						TestSwitch, ALL_WORLDS,
						List.of( BASE_PATH, RUNTIME_PATH ),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder( MUSTFAIL_FOLDER, "CyclicInheritance" ) ),
						TARGET_FOLDER,
						Collections.emptyList(),
						CyclicInheritanceA, ALL_WORLDS, Collections.emptyList(),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder( MUSTFAIL_FOLDER, "LotsOfErrors" ) ),
						TARGET_FOLDER,
						Collections.emptyList(),
						LotsOfErrors, ALL_WORLDS, Collections.emptyList(), Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder( MUSTPASS_FOLDER, "SwitchTest" ) ),
						TARGET_FOLDER,
						Collections.emptyList(),
						SwitchTest, ALL_WORLDS,
						List.of( BASE_PATH ),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder( MUSTFAIL_FOLDER, "VariableDeclarations" ) ),
						TARGET_FOLDER,
						Collections.emptyList(),
						VariableDeclarations, ALL_WORLDS,
						List.of( BASE_PATH ),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder( MUSTPASS_FOLDER, "MirrorChannel" ) ),
						TARGET_FOLDER,
						Collections.emptyList(),
						MirrorChannel, ALL_WORLDS,
						List.of( BASE_PATH ),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder( MUSTPASS_FOLDER, "LoggerExample" ) ),
						TARGET_FOLDER,
						Collections.emptyList(),
						LoggerExample, ALL_WORLDS,
						List.of( BASE_PATH ),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder( MUSTPASS_FOLDER, "IfDesugar" ) ),
						TARGET_FOLDER,
						List.of( subFolder( MUSTPASS_FOLDER, "IfDesugar" ) ),
						IfDesugar, ALL_WORLDS,
						List.of( BASE_PATH ),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder( MUSTFAIL_FOLDER, "IllegalInheritance" ) ),
						TARGET_FOLDER,
						Collections.emptyList(),
						IllegalInheritance, ALL_WORLDS,
						Collections.emptyList(),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder( MUSTFAIL_FOLDER, "NonMatchingReturnType" ) ),
						TARGET_FOLDER,
						Collections.emptyList(),
						NonMatchingReturnType, ALL_WORLDS,
						Collections.emptyList(),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder( MUSTPASS_FOLDER, "ChainingOperator" ) ),
						TARGET_FOLDER,
						Collections.emptyList(),
						ChainingOperator, ALL_WORLDS,
						List.of( BASE_PATH, RUNTIME_PATH ),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder( MUSTPASS_FOLDER, "AutoBoxing" ) ),
						TARGET_FOLDER,
						Collections.emptyList(),
						AutoBoxing, ALL_WORLDS,
						List.of( BASE_PATH ),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder( MUSTPASS_FOLDER, "BookSellingSoloist" ) ),
						TARGET_FOLDER,
						List.of(
								RUNTIME_MAIN_FOLDER,
								CHORALUNIT_MAIN_FOLDER
						),
						BookSellingSoloist, ALL_WORLDS,
						List.of( BASE_PATH ),
						Collections.emptyList() )
		).toList();

		List< String > passCompilationSymbols = Stream.of(
				HelloRoles,
				ConsumeItems,
				DiffieHellman,
				TestSwitch,
				RemoteFunction,
				BuyerSellerShipper,
				ChainingOperator,
				IfDesugar,
				LoggerExample,
				// SwitchTest, // https://github.com/choral-lang/choral/issues/29
				// MirrorChannel, // https://github.com/choral-lang/choral/issues/27
				// AutoBoxing, // https://github.com/choral-lang/choral/issues/28
				BookSellingSoloist,
				ExtendsTest,
				AuthResult,
				DistAuth
		).toList();

		List< String > failCompilationSymbols = Stream.of(
				IllegalInheritance,
				MultiFoo,
				CyclicInheritanceA,
				LotsOfErrors,
				WrongType,
				VariableDeclarations,
				NonMatchingReturnType
		).toList();

		runTests( passCompilationSymbols, allCompilationRequests, TestType.MUSTPASS );
		runTests( failCompilationSymbols, allCompilationRequests, TestType.MUSTFAIL );
	}

	private static void runTests(
			List< String > symbols, List< CompilationRequest > compilationRequests,
			TestType testType
	) {
		List< CompilationRequest > finalCompilationRequests = compilationRequests.stream()
				.filter( c -> symbols.contains(c.symbol))
				.toList();

		switch( testType ) {
			case MUSTPASS -> {
				System.out.println( "\nNow running tests that must pass\n" );
				finalCompilationRequests.forEach( TestChoral::project );
				System.out.println(
						"\u001B[32m" + "Amount of tests ran: " + finalCompilationRequests.size() + "\u001B[0m" );
				System.out.println();
			}
			case MUSTFAIL -> {
				System.out.println( "Now running tests that must fail\n" );
				finalCompilationRequests.forEach( TestChoral::projectFail );
				System.out.println();
				System.out.println(
						"\u001B[32m" + "Amount of tests ran: " + finalCompilationRequests.size() + "\u001B[0m" );
				System.out.println();
			}
			default -> {
			}
		}
	}

	@Test
	public void mvnTestMethod() {
		main( new String[ 10 ] );
	}

	private static ArrayList< String > generateParameters(CompilationRequest compilationRequest){
		ArrayList< String > parameters = new ArrayList<>();
		parameters.add( "epp" );
		parameters.add( "--verbosity=DEBUG" );
		if( !compilationRequest.headersFolders().isEmpty() )
			parameters.add(
					"--headers=" + String.join( FILESEPARATOR, compilationRequest.headersFolders() ) );
		parameters.add( "-t" );
		parameters.add( compilationRequest.targetFolder() );
		parameters.add( "-s" );
		parameters.add( String.join( FILESEPARATOR, compilationRequest.sourceFolder() ) );
		parameters.add( compilationRequest.symbol() );
		parameters.addAll( compilationRequest.worlds() );
		parameters.add( "--annotate" );
		return parameters;
	}

	private static void project( CompilationRequest compilationRequest ) {
		try {
			ArrayList< String > parameters = generateParameters(compilationRequest);
			
			int exitCode = Choral.compile( parameters.toArray( new String[ 0 ] ) );
			if( exitCode != 0 )
				System.err.print( "Got " + exitCode + " as exit code when 0 was expected" );

			boolean errorOccured = false;
			boolean diffError = false;
			boolean fileCountError = false;
			boolean expectedFilesFailed = false;
			List< List< String >> diffOutputs = new ArrayList<>();

			List< String > javaCompilationErrors = new ArrayList<>();

			List< String > alreadyCheckedPaths = new ArrayList<>();
			for( String folder : compilationRequest.sourceFolder() ) {
				Path path = Path.of( folder );
				List< Path > sourceFiles = Files.walk( path ).filter(
						file -> file.toString().endsWith( ".ch" ) ).toList();
				for( Path file : sourceFiles ) {
					String fileContent = Files.readString( file );
					String pathString = fileContent.substring(
							fileContent.indexOf( "package " ) + 7, fileContent.indexOf(
									";" ) ).trim(); // this finds the package declared at the top of the file
					String innerPathString = PATHSEPARATOR + pathString.replace( ".", PATHSEPARATOR );
					String targetFolderString = TARGET_FOLDER + innerPathString;

					if( alreadyCheckedPaths.contains( targetFolderString ) ) continue;

					alreadyCheckedPaths.add( targetFolderString );

					try {
						Path projectFolder = Path.of( targetFolderString );
						List< Path > projectedJavaFiles = Files.walk( projectFolder ).filter(
								javaFile -> javaFile.toString().endsWith( ".java" ) ).toList();

						String expectedFolderString = EXPECTED_FOLDER + innerPathString;
						Path expectedFolderPath = Path.of( expectedFolderString );
						List< Path > expectedFiles = Files.walk( expectedFolderPath ).filter(
								expectedFile -> expectedFile.toString().endsWith(
										".java" ) ).toList();

						if( projectedJavaFiles.size() != expectedFiles.size() ) {
							fileCountError = true;
							errorOccured = true;
							continue;
						}

						for( int i = 0; i < expectedFiles.size(); i++ ) {
							List< String > original = Files.readAllLines( expectedFiles.get( i ) );
							List< String > revised = Files.readAllLines( projectedJavaFiles.get( i ) );

							Patch< String > patch = DiffUtils.diff( original, revised );

							List<String> diffOutput = UnifiedDiffUtils.generateUnifiedDiff(
									expectedFiles.get( i ).toString(),
									projectedJavaFiles.get( i ).toString(),
									original,
									patch,
									3
							);

							if( !diffOutput.isEmpty() ) {
								errorOccured = true;
								diffError = true;
								diffOutputs.add(diffOutput);
							}
						}


						JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
						DiagnosticCollector< JavaFileObject > diagnostics = new DiagnosticCollector<>();
						StandardJavaFileManager fileManager = compiler.getStandardFileManager(
								diagnostics, null, null );
						Iterable< ? extends JavaFileObject > compilationUnits = fileManager.getJavaFileObjects(
								expectedFiles.toArray( new Path[ 0 ] ) );

						String sourcePath = String.join( FILESEPARATOR,
								compilationRequest.sourcePaths );
						String classPath = String.join( FILESEPARATOR, compilationRequest.classPaths );

						List< String > options = new ArrayList<>();

						if( !sourcePath.isEmpty() )
							options.addAll( Arrays.asList( "-sourcepath", sourcePath ) );
						if( !classPath.isEmpty() )
							options.addAll( Arrays.asList( "-classpath", classPath ) );

						options.addAll( Arrays.asList( "-d", "bin" ) );

						JavaCompiler.CompilationTask task = compiler.getTask( null, fileManager,
								diagnostics, options, null, compilationUnits );

						if( !task.call() ) {
							expectedFilesFailed = true;
							System.out.println( "compilation error" );
							errorOccured = true;
							for( Diagnostic< ? extends JavaFileObject > diagnostic : diagnostics.getDiagnostics() ) {
								javaCompilationErrors.add(
										String.format( "Error on line %d in %s%n",
												diagnostic.getLineNumber(),
												diagnostic.getSource().toUri() ) );
								javaCompilationErrors.add( diagnostic.getMessage( null ) );
							}
						}

					} catch( InvalidPathException e ) {
						System.err.println( "Invalid package definition in: " + file );
						System.err.println( "Remember to define a package at the top of the file" );
					} catch( Exception e ) {
						e.printStackTrace();
					}
				}
			}

			if( errorOccured ) {
				System.out.printf( "%-" + COLUMN_WIDTH + "s %s[ERROR]%s%n",
						compilationRequest.symbol, RED, RESET );
				if( diffError ) {
					System.out.println(
							RED + "\tError: " + RESET + "There was a difference between the expected output and the generated output, now printing diff: " );
					for (List<String> diffOutput : diffOutputs){
						diffOutput.forEach( item -> System.out.println( "\t" + item ) );
					}
				}
				if( fileCountError ) {
					System.err.println(
							RED + "\tError: " + RESET + "Expected files and projected files are not even in count! Ensure that the expected files is up to date" );
				}
				if( expectedFilesFailed ) {
					System.out.println( "printing correctly" );
					System.err.println(
							RED + "\tError: " + RESET + "Not all files could be compiled" );
					javaCompilationErrors.forEach(
							errorLine -> System.err.println( "\t" + errorLine ) );
				}
			} else
				System.out.printf( "%-" + COLUMN_WIDTH + "s %s[OK]%s%n", compilationRequest.symbol,
						GREEN, RESET );

		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	private static void projectFail( CompilationRequest compilationRequest ) {
		try {
			ArrayList< String > parameters = generateParameters(compilationRequest);

			ByteArrayOutputStream testOutput = new ByteArrayOutputStream();
			ByteArrayOutputStream testError = new ByteArrayOutputStream();
			PrintStream originalOutput = System.out;
			PrintStream originalError = System.err;

			int exitCode;

			try {
				System.setOut( new PrintStream( testOutput ) );
				System.setErr( new PrintStream( testError ) );
				exitCode = Choral.compile( parameters.toArray( new String[ 0 ] ) );
			} finally {
				System.setOut( originalOutput );
				System.setErr( originalError );
			}
			if( exitCode == 0 ) System.err.println(
					"Program received 0 as exitcode, which means no errors were found. This test is expected to have errors" );

			String stringTestOutput = testOutput.toString();
			String[] outputLines = stringTestOutput.split( "\n" );


			Path directoryPath = Path.of( compilationRequest.sourceFolder().get( 0 ) );
			if( Files.isDirectory( directoryPath ) ) {
				List< Path > testFiles = Files.walk( directoryPath )
						.filter( path -> path.toString().endsWith( ".ch" ) )
						.toList();
				String[] fileContent = Files.readString( testFiles.get( 0 ) ).split( "\n" );

				List< Map.Entry< Integer, String > > expectedErrorsFound = new ArrayList<>();

				for( int i = 0; i < fileContent.length; i++ ) {
					if( fileContent[ i ].contains( "//!" ) ) {
						int nextOccurence = fileContent[ i ].indexOf( "//!" );
						int endOfError = fileContent[ i ].indexOf( "//", nextOccurence + 1 );
						if( endOfError == -1 ) endOfError = fileContent[ i ].length();

						while( nextOccurence != -1 ) {
							expectedErrorsFound.add( new AbstractMap.SimpleEntry<>( i,
									fileContent[ i ].substring( nextOccurence + 3,
											endOfError ).trim() ) ); // '//!' = 3 characters
							nextOccurence = fileContent[ i ].indexOf( "//!", endOfError );
							endOfError = fileContent[ i ].indexOf( "//", nextOccurence + 1 );

							if( endOfError == -1 ) endOfError = fileContent[ i ].length();
						}
					}
				}

				int errorsFound = 0;
				int nextErrorLine = 0;
				int start = outputLines[ nextErrorLine ].indexOf( "ch:" ) + 3;
				int end = outputLines[ nextErrorLine ].indexOf( ":", start );

				int errorLineNumber = Integer.parseInt(
						outputLines[ nextErrorLine ].substring( start, end ) ) - 1;
				List< Map.Entry< Integer, String > > foundErrors = new ArrayList<>();
				List< String > missedErrors = new ArrayList<>();

				boolean endOfOutputReached = false;

				for( Map.Entry< Integer, String > line : expectedErrorsFound ) {
					if( errorLineNumber == line.getKey() ) {
						boolean errorFound = outputLines[ nextErrorLine ].contains(
								line.getValue() );
						if( errorFound ) {
							errorsFound++;
							foundErrors.add( line );
						}
					} else {
						System.out.println(
								"Error line number doesn't match, did you put the expected error on the wrong line?" );
						System.out.println(
								"Got: " + errorLineNumber + " expected: " + line.getKey() );
					}

					for( int i = nextErrorLine; i < outputLines.length; i++ ) {
						if( outputLines[ i ].equals( "compilation failed." ) ) {
							nextErrorLine = i + 2;
							break;
						}
					}

					if( nextErrorLine >= outputLines.length ) {
						endOfOutputReached = true;
						break; // end of error output reached
					}

					start = outputLines[ nextErrorLine ].indexOf( "ch:" ) + 3;
					end = outputLines[ nextErrorLine ].indexOf( ":", start );

					if( end == -1 || start == -1 ) {
						endOfOutputReached = true;
						break; // end of error output reached 
					}

					errorLineNumber = Integer.parseInt(
							outputLines[ nextErrorLine ].substring( start, end ) ) - 1;
				}

				List< String > unExpectedErrors = new ArrayList<>();
				if( !endOfOutputReached ) {
					while( start != -1 || end != -1 ) {
						unExpectedErrors.add( outputLines[ nextErrorLine ] );

						for( int i = nextErrorLine; i < outputLines.length; i++ ) {
							if( outputLines[ i ].equals( "compilation failed." ) ) {
								nextErrorLine = i + 2;
								break;
							}
						}

						if( nextErrorLine >= outputLines.length ) break;

						start = outputLines[ nextErrorLine ].indexOf( "ch:" ) + 3;
						end = outputLines[ nextErrorLine ].indexOf( ":", start );
					}
				}

				boolean allErrorsFound = errorsFound != expectedErrorsFound.size();
				boolean anyErrorsMissed = !missedErrors.isEmpty();
				boolean anyErrorsNotExpected = !unExpectedErrors.isEmpty();

				if( allErrorsFound || anyErrorsMissed || anyErrorsNotExpected ) {
					System.out.printf( "%-" + COLUMN_WIDTH + "s %s[ERROR]%s%n",
							compilationRequest.symbol, RED, RESET );

					if( allErrorsFound ) {
						System.out.println(
								"\t\u001B[31m" + "Error" + "\u001B[0m: " + ( expectedErrorsFound.size() - errorsFound ) + " errors not found" );
						for( Map.Entry< Integer, String > line : expectedErrorsFound ) {
							if( !foundErrors.contains( line ) ) System.out.println(
									"\t\tExpected to find: " + line.getValue() + " on line " + line.getKey() );
						}
					}
					if( anyErrorsMissed ) {
						System.out.println(
								"Found " + missedErrors.size() + " errors not expected" );
						for( String error : missedErrors ) {
							System.out.println( error );
						}
					}
					if( anyErrorsNotExpected ) {
						System.out.println(
								"Found errors reported by the compiler not annotated in test:" );
						for( String error : unExpectedErrors ) {
							System.out.println( error );
						}
					}
				} else System.out.printf( "%-" + COLUMN_WIDTH + "s %s[OK]%s%n",
						compilationRequest.symbol, GREEN, RESET );
			} else
				System.err.printf( "Directory not found: '%s'%n", directoryPath );
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	

	

}
