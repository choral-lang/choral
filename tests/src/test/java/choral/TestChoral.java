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

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.Assertions;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import org.junit.jupiter.api.function.Executable;

public class TestChoral {


	///////////////////////////////// DATATYPES /////////////////////////////////////


	private record CompilationRequest(List< String > sourceFolder,
									  String targetFolder,
									  List< String > headersFolders,
									  String symbol,
									  List< String > worlds,
									  List< String > sourcePaths,
									  List< String > classPaths) {}

	private record CompilationResults(int exitCode, String stdout, String stderr) {}

	private record TestError(String path, int line, String message) {
		/** Errors match if they occur on the same line and one message is a substring of the other. */
		boolean matches( TestError that ) {
			return this.path.equals(that.path) &&
				this.line == that.line &&
				( this.message.contains( that.message ) || that.message.contains( this.message ) );
		}
	}

	private record MustFailTest(CompilationRequest compilationRequest) implements Executable {
		@Override
		public void execute() {
			projectFail( compilationRequest );
		}
	}

	private record MustPassTest(CompilationRequest compilationRequest) implements Executable {
		@Override
		public void execute() {
			project( compilationRequest );
		}
	}

	///////////////////////////////// CONSTANTS /////////////////////////////////////


	static final List< String > ALL_WORLDS = Collections.singletonList( "" );
	private static final String FILESEPARATOR = File.pathSeparator;
	private static final String TARGET_FOLDER = "projectedOutput";
	private static final String EXPECTED_FOLDER = "expectedOutput";
	private static final String RUNTIME_MAIN_FOLDER = Paths.get("..", "runtime", "src", "main", "choral").toString();
	private static final String CHORALUNIT_MAIN_FOLDER = Paths.get("..", "choral-unit", "src", "main", "choral").toString();
	private static final String MUSTFAIL_FOLDER = Paths.get("src", "main", "choral", "MustFail").toString();
	private static final String MUSTPASS_FOLDER = Paths.get("src", "main", "choral", "MustPass").toString();
	private static final String BASE_PATH = Paths.get("base", "src", "main", "java").toString();
	private static final String RUNTIME_PATH = Paths.get("runtime", "src", "main", "java").toString();
	private static final String EXPECTEDOUTPUT_PATH = "expectedOutput";

	// formatting for terminal output
	private static final String GREEN = "\u001B[32m";
	private static final String RED = "\u001B[31m";
	private static final String RESET = "\u001B[0m";
	private static final int COLUMN_WIDTH = 30;


	///////////////////////////////// HELPERS /////////////////////////////////////


	/** Tries to compile the test and returns the results produced by the Choral compiler. */
	private static CompilationResults compile(CompilationRequest compilationRequest){
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

		int exitCode;
		ByteArrayOutputStream testOutput = new ByteArrayOutputStream();
		ByteArrayOutputStream testError = new ByteArrayOutputStream();
		PrintStream originalOutput = System.out;
		PrintStream originalError = System.err;

		try {
			System.setOut( new PrintStream( testOutput ) );
			System.setErr( new PrintStream( testError ) );
			exitCode = Choral.compile( parameters.toArray( new String[ 0 ] ) );
		} finally {
			System.setOut( originalOutput );
			System.setErr( originalError );
		}

		return new CompilationResults( exitCode, testOutput.toString(), testError.toString() );
	}


	public static String subFolder( String sourceFolder, String subFolder ) {
		return sourceFolder + File.separator + subFolder;
	}


	///////////////////////////////// ADD TESTS HERE /////////////////////////////////////


	@TestFactory
	public Stream<DynamicTest> mainTests(  ) {

        //////// MustPass Tests ////////

        Stream< CompilationRequest > mustPassRequests = Stream.of(
                new CompilationRequest(
                        List.of( subFolder(MUSTPASS_FOLDER, "HelloRoles" ) ),
                        TARGET_FOLDER,
                        Collections.emptyList(),
                        "HelloRoles", ALL_WORLDS,
                        List.of( BASE_PATH ),
                        Collections.emptyList() )
                ,
                new CompilationRequest(
                        List.of( subFolder(MUSTPASS_FOLDER, "BiPair" ) ),
                        TARGET_FOLDER,
                        Collections.emptyList(),
                        "BiPair", ALL_WORLDS,
                        List.of( BASE_PATH ),
                        Collections.emptyList() )
                ,
                new CompilationRequest(
                        List.of( subFolder(MUSTPASS_FOLDER, "ConsumeItems" ) ),
                        TARGET_FOLDER,
                        Collections.emptyList(),
                        "ConsumeItems", ALL_WORLDS,
                        List.of( BASE_PATH, RUNTIME_PATH ),
                        Collections.emptyList() )
                ,
                new CompilationRequest(
                        List.of( subFolder(MUSTPASS_FOLDER, "ExtendsTest") ),
                        TARGET_FOLDER,
                        Collections.emptyList(),
                        "MyExtClass", ALL_WORLDS,
                        List.of( BASE_PATH ),
                        Collections.emptyList() )
                ,
                new CompilationRequest(
                        List.of( subFolder(MUSTPASS_FOLDER, "RemoteFunction" ) ),
                        TARGET_FOLDER,
                        Collections.emptyList(),
                        "RemoteFunction", ALL_WORLDS,
                        List.of( BASE_PATH, RUNTIME_PATH ),
                        Collections.emptyList() )
                ,
                new CompilationRequest(
                        List.of( subFolder(MUSTPASS_FOLDER, "AuthResult" ),
                                subFolder(MUSTPASS_FOLDER, "DistAuthUtils") ),
                        TARGET_FOLDER,
                        List.of( subFolder(MUSTPASS_FOLDER, "BiPair" ) ),
                        "AuthResult", ALL_WORLDS,
                        List.of( BASE_PATH, EXPECTEDOUTPUT_PATH, RUNTIME_PATH ),
                        Collections.emptyList() )
                ,
                new CompilationRequest(
                        List.of( subFolder(MUSTPASS_FOLDER, "DistAuth" ),
                                subFolder(MUSTPASS_FOLDER, "DistAuthUtils")
                        ),
                        TARGET_FOLDER,
                        List.of(
                                subFolder(MUSTPASS_FOLDER, "DistAuth" ),
                                subFolder(MUSTPASS_FOLDER, "AuthResult" ),
                                subFolder(MUSTPASS_FOLDER, "BiPair" ),
                                RUNTIME_MAIN_FOLDER,
                                CHORALUNIT_MAIN_FOLDER
                        ),
                        "DistAuth", ALL_WORLDS,
                        List.of( BASE_PATH, RUNTIME_PATH, EXPECTEDOUTPUT_PATH ),
                        Collections.emptyList() )
                ,
                new CompilationRequest(
                        List.of( subFolder(MUSTPASS_FOLDER, "BuyerSellerShipper" ) ),
                        TARGET_FOLDER,
                        List.of(
                                subFolder(MUSTPASS_FOLDER, "BuyerSellerShipper" ),
                                RUNTIME_MAIN_FOLDER,
                                CHORALUNIT_MAIN_FOLDER
                        ),
                        "BuyerSellerShipper", ALL_WORLDS,
                        List.of( BASE_PATH, RUNTIME_PATH, EXPECTEDOUTPUT_PATH ),
                        Collections.emptyList() )
                ,
                new CompilationRequest(
                        List.of( subFolder(MUSTPASS_FOLDER, "DiffieHellman" ),
                                subFolder(MUSTPASS_FOLDER, "BiPair" ) ),
                        TARGET_FOLDER,
                        Collections.emptyList(),
                        "DiffieHellman", ALL_WORLDS,
                        List.of( BASE_PATH, RUNTIME_PATH, EXPECTEDOUTPUT_PATH ),
                        Collections.emptyList() )
                ,
                new CompilationRequest(
                        List.of( subFolder(MUSTPASS_FOLDER, "TestSwitch" ) ),
                        TARGET_FOLDER,
                        Collections.emptyList(),
                        "TestSwitch", ALL_WORLDS,
                        List.of( BASE_PATH, RUNTIME_PATH ),
                        Collections.emptyList() )
                ,
// https://github.com/choral-lang/choral/issues/29
//                new CompilationRequest(
//                        List.of( subFolder(MUSTPASS_FOLDER, "SwitchTest" ) ),
//                        TARGET_FOLDER,
//                        Collections.emptyList(),
//                        "SwitchTest", ALL_WORLDS,
//                        List.of( BASE_PATH ),
//                        Collections.emptyList() )
//                ,
// https://github.com/choral-lang/choral/issues/27
//                new CompilationRequest(
//                        List.of( subFolder(MUSTPASS_FOLDER, "MirrorChannel" ) ),
//                        TARGET_FOLDER,
//                        Collections.emptyList(),
//                        "MirrorChannel", ALL_WORLDS,
//                        List.of( BASE_PATH ),
//                        Collections.emptyList() )
//                ,
                new CompilationRequest(
                        List.of( subFolder(MUSTPASS_FOLDER, "LoggerExample" ) ),
                        TARGET_FOLDER,
                        Collections.emptyList(),
                        "LoggerExample", ALL_WORLDS,
                        List.of( BASE_PATH ),
                        Collections.emptyList() )
                ,
                new CompilationRequest(
                        List.of( subFolder(MUSTPASS_FOLDER, "IfDesugar") ),
                        TARGET_FOLDER,
                        List.of( subFolder(MUSTPASS_FOLDER, "IfDesugar") ),
                        "IfDesugarTest", ALL_WORLDS,
                        List.of( BASE_PATH ),
                        Collections.emptyList() )
                ,
                new CompilationRequest(
                        List.of( subFolder(MUSTPASS_FOLDER, "ChainingOperator") ),
                        TARGET_FOLDER,
                        Collections.emptyList(),
                        "ChainingExample", ALL_WORLDS,
                        List.of( BASE_PATH, RUNTIME_PATH ),
                        Collections.emptyList() )
                ,
// https://github.com/choral-lang/choral/issues/28
//                new CompilationRequest(
//                        List.of( subFolder(MUSTPASS_FOLDER, "Autoboxing" ) ),
//                        TARGET_FOLDER,
//                        Collections.emptyList(),
//                        "Autoboxing", ALL_WORLDS,
//                        List.of( BASE_PATH ),
//                        Collections.emptyList() )
//                ,
                new CompilationRequest(
                        List.of( subFolder(MUSTPASS_FOLDER, "BookSellingSoloist") ),
                        TARGET_FOLDER,
                        List.of(
                                RUNTIME_MAIN_FOLDER,
                                CHORALUNIT_MAIN_FOLDER
                        ),
                        "BuyBook2", ALL_WORLDS,
                        List.of( BASE_PATH ),
                        Collections.emptyList() )
        );


        //////// MustFail Tests ////////

		Stream< CompilationRequest > mustFailRequests = Stream.of(
				new CompilationRequest(
						List.of( subFolder(MUSTFAIL_FOLDER, "WrongType" ) ),
						TARGET_FOLDER,
						Collections.emptyList(),
						"WrongType", ALL_WORLDS,
						List.of( BASE_PATH ),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder(MUSTFAIL_FOLDER, "MultiFoo" ) ),
						TARGET_FOLDER,
						Collections.emptyList(),
						"MultiFoo", ALL_WORLDS,
						Collections.emptyList(),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder(MUSTFAIL_FOLDER, "CyclicInheritance") ),
						TARGET_FOLDER,
						Collections.emptyList(),
						"CyclicInheritance_A", ALL_WORLDS, Collections.emptyList(),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder(MUSTFAIL_FOLDER, "LotsOfErrors" ) ),
						TARGET_FOLDER,
						Collections.emptyList(),
						"LotsOfErrors", ALL_WORLDS, Collections.emptyList(), Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder(MUSTFAIL_FOLDER, "VariableDeclarations" ) ),
						TARGET_FOLDER,
						Collections.emptyList(),
						"VariableDeclarations", ALL_WORLDS,
						List.of( BASE_PATH ),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder(MUSTFAIL_FOLDER, "IllegalInheritance") ),
						TARGET_FOLDER,
						Collections.emptyList(),
						"TwoWorldList", ALL_WORLDS,
						Collections.emptyList(),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of( subFolder(MUSTFAIL_FOLDER, "NonMatchingReturnType" ) ),
						TARGET_FOLDER,
						Collections.emptyList(),
						"NonMatchingReturnType", ALL_WORLDS,
						Collections.emptyList(),
						Collections.emptyList() )
				,
				new CompilationRequest(
						List.of(subFolder(MUSTFAIL_FOLDER, "MultiFileError" ),
								subFolder(MUSTFAIL_FOLDER, "MultiFileErrorUtil" )),
						TARGET_FOLDER,
						Collections.emptyList(),
						"MultiFileError", ALL_WORLDS,
						List.of(BASE_PATH),
						Collections.emptyList())
		);

        Stream<DynamicTest> mustPassTests = mustPassRequests
            .map(request -> dynamicTest(request.symbol, new MustPassTest( request )));

        Stream<DynamicTest> mustFailTests = mustFailRequests
            .map(request -> dynamicTest(request.symbol, new MustFailTest( request )));

		return Stream.concat(mustPassTests, mustFailTests);
	}

	/** Returns list of package names declared in the source folders of the compilation request */
	private static HashSet<String> getPackageNames( CompilationRequest compilationRequest ) throws IOException {
		HashSet< String > packages = new HashSet<>();
		for( String folder : compilationRequest.sourceFolder() ) {
			Path path = Path.of( folder );
			List< Path > choralFiles = Files.walk( path ).filter(
				file -> file.toString().endsWith( ".ch" )
			).toList();
			for( Path file : choralFiles ) {
				String fileContent = Files.readString( file );
				// Find the package declared at the top of the file
				int i = fileContent.indexOf( "package " );
				int j = fileContent.indexOf( ";" );
				if ( i == -1 || j == -1 ) {
					System.err.println( "Missing package declaration in file: " + file );
					continue;
				}
				String pathString = fileContent.substring(i + 7, j).trim(); // 'package' = 7 characters
				packages.add( pathString );
			}
		}
		return packages;
	}

    /** Compiles the test, expecting it to succeed. */
	private static void project( CompilationRequest compilationRequest ) {
		ArrayList< String > errors = new ArrayList<>();

		CompilationResults results = compile(compilationRequest);
		if( results.exitCode != 0 )
			errors.add( "Compilation failed unexpectedly with exit code " + results.exitCode +
					"\n" + results.stdout );

		try {
			for( String packageName : getPackageNames(compilationRequest) ) {
				String[] packageList = packageName.split( "\\." );

				// Get all the projected and expected Java files
				Path projectFolder = Path.of( TARGET_FOLDER, packageList );
				List< Path > projectedJavaFiles = Files.walk( projectFolder ).filter(
						javaFile -> javaFile.toString().endsWith( ".java" )
				).sorted().toList();

				Path expectedFolderPath = Path.of( EXPECTED_FOLDER, packageList );
				List< Path > expectedFiles = Files.walk( expectedFolderPath ).filter(
						expectedFile -> expectedFile.toString().endsWith( ".java" )
				).sorted().toList();

				// PHASE 1: CHECK IF EXPECTED AND PROJECTED CODE DIFFER

				if( projectedJavaFiles.size() != expectedFiles.size() ) {
					errors.add("The number of projected files does not equal the number of expected files");
					continue;
				}

				for( int i = 0; i < expectedFiles.size(); i++ ) {
					List< String > original = Files.readAllLines( expectedFiles.get( i ) );
					List< String > projected = Files.readAllLines( projectedJavaFiles.get( i ) );

					Patch< String > patch = DiffUtils.diff( original, projected );

					List<String> diffOutput = UnifiedDiffUtils.generateUnifiedDiff(
							expectedFiles.get( i ).toString(),
							projectedJavaFiles.get( i ).toString(),
							original,
							patch,
							3
					);

					if( !diffOutput.isEmpty() ) {
						String diff = String.join( "\n", diffOutput );
						errors.add("There was a difference between the expected output and " +
								"the generated output, now printing diff:\n" + diff);
					}
				}

				// PHASE 2: TRY COMPILING THE EXPECTED JAVA CODE

				JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
				DiagnosticCollector< JavaFileObject > diagnostics = new DiagnosticCollector<>();
				StandardJavaFileManager fileManager = compiler.getStandardFileManager(
						diagnostics, null, null );
				Iterable< ? extends JavaFileObject > compilationUnits = fileManager.getJavaFileObjects(
						expectedFiles.toArray( new Path[ 0 ] ) );

				List< String > options = new ArrayList<>();
				if( !compilationRequest.sourcePaths.isEmpty() ) {
					options.add( "-sourcepath" );
					options.add( String.join( FILESEPARATOR, compilationRequest.sourcePaths ) );
				}
				if( !compilationRequest.classPaths.isEmpty() ) {
					options.add( "-classpath" );
					options.add( String.join( FILESEPARATOR, compilationRequest.classPaths ) );
				}
				options.addAll( Arrays.asList( "-d", "bin" ) );

				JavaCompiler.CompilationTask task = compiler.getTask( null, fileManager,
						diagnostics, options, null, compilationUnits );

				if( !task.call() ) {
					List< String > javaCompilationErrors = new ArrayList<>();
					for( Diagnostic< ? extends JavaFileObject > diagnostic : diagnostics.getDiagnostics() ) {
						javaCompilationErrors.add(
								String.format( "Error on line %d in %s%n",
										diagnostic.getLineNumber(),
										diagnostic.getSource().toUri() ) );
						javaCompilationErrors.add( diagnostic.getMessage( null ) );
					}
					String javaErrors = String.join( "", javaCompilationErrors );
					errors.add( "Expected Java code does not compile:\n" + javaErrors );
				}
			}
		} catch( IOException e ) {
			errors.add( e.getMessage() );
		}

		if( !errors.isEmpty() ) {
			System.out.printf( "%-" + COLUMN_WIDTH + "s %s[ERROR]%s%n", compilationRequest.symbol, RED, RESET );
			String errorMessages = String.join("\n", errors);
			Assertions.fail(errorMessages);
		} else {
			System.out.printf( "%-" + COLUMN_WIDTH + "s %s[OK]%s%n", compilationRequest.symbol, GREEN, RESET );
		}
	}

    /**
     * Finds all of the expected errors in the given file.
     * An expected error is declared by a comment starting with '//!'
     */
	private static List<TestError> findExpectedErrors(String path, String[] fileContent){
		List<TestError> testErrors = new ArrayList<>();

		for( int i = 0; i < fileContent.length; i++ ) {
            int lineNumber = i + 1;
            String line = fileContent[i];
            String[] chunks = line.split("//!");

            for (int j = 1; j < chunks.length; j++) {
                String message = chunks[j].trim();
                testErrors.add(new TestError(path, lineNumber, message));
            }
		}
		return testErrors;
	}

    /** Searches the compiler output for actual errors produced during compilation. */
	private static List<TestError> findActualErrors(String[] outputLines){
		// Choral's error messages look like this:
		// src/main/choral/MustFail/WrongType/WrongType.ch:7:14: error: Required type 'int@(A)', found 'java.lang.String@(A)'.
		//
		//   6 |         int@A lol = 5@A;
		//   7 |         int@A lul = "Hello"@A; //! Required type 'int@(A)', found 'java.lang.String@(A)'
		//     | --------------^
		//   8 |         a = lol;
		//
		//compilation failed.

		// Use a regex to get the path to the Choral file, the line number, and the error message.
		Pattern pattern = Pattern.compile( "(.*\\.ch):(\\d+):(\\d+): error: (.*)" );
		List<TestError> actualErrors = new ArrayList<>();

		for (String line : outputLines) {
			Matcher matcher = pattern.matcher( line );
			if( !matcher.matches() )
				continue;

			String filePath = matcher.group( 1 );
			int lineNumber = Integer.parseInt( matcher.group( 2 ) );
			String errorMessage = matcher.group( 4 );

			actualErrors.add(new TestError(filePath, lineNumber, errorMessage));
		}

		return actualErrors;
	}

	/** Compiles the test, expecting it to fail. */
	private static void projectFail( CompilationRequest compilationRequest ) {
		List<String> errors = new ArrayList<>();
		CompilationResults results = compile(compilationRequest);

		if( results.exitCode == 0 )
			errors.add("Program compiled with exit code 0, which means no errors were found." +
				"This test is expected to have errors" );

		String[] outputLines = results.stdout.split( "\n" );
		List<TestError> actualErrors = findActualErrors(outputLines);

		// Concatenate all the expected errors in all the files in the source folders
		ArrayList<TestError> expectedErrors = new ArrayList<>();
		for (String path : compilationRequest.sourceFolder()){
			Path directoryPath = Path.of(path);
			if (!Files.isDirectory(directoryPath)){
				errors.add( "Directory not found: " + directoryPath );
				continue;
			}
			try (Stream<Path> testFiles = Files.walk(directoryPath)) {
				testFiles
					.filter(file -> file.toString().endsWith(".ch"))
					.forEach(file -> {
						try {
							String[] fileContent = Files.readString(file).split("\n");
							expectedErrors.addAll(findExpectedErrors(file.toString(), fileContent));
						} catch (IOException e) {
							errors.add("Error reading file '" + file + "': " + e.getMessage());
						}
					});
			} catch (IOException e) {
				errors.add("Error reading file '" + directoryPath + "': " + e.getMessage());
			}
		}

		// Add an error for each expected error not found in actual errors, and vice versa.
		// The expected error can be a substring of the actual error.
		for (TestError expected : subtract(expectedErrors, actualErrors)) {
			errors.add("Expected to find the following error on line " + expected.line() +
				": " + expected.message());
		}
		for (TestError actual : subtract(actualErrors, expectedErrors)) {
			errors.add("Got an unexpected error on line " + actual.line() +
				": " + actual.message());
		}

		if (errors.isEmpty()){
			System.out.printf( "%-" + COLUMN_WIDTH + "s %s[OK]%s%n", compilationRequest.symbol, GREEN, RESET );
		}
		else {
			System.out.printf( "%-" + COLUMN_WIDTH + "s %s[ERROR]%s%n", compilationRequest.symbol, RED, RESET );
			String errorMessages = String.join("\n", errors);
			Assertions.fail(errorMessages);
		}
	}

	/** Returns a list of errors in xs that are not in ys. */
	private static List<TestError> subtract(List<TestError> original, List<TestError> removed) {
		ArrayList<TestError> result = new ArrayList<>();
		for( TestError x : original ) {
			boolean found = false;
			for( TestError y : removed ) {
				if( x.matches(y) ) {
					found = true;
					break;
				}
			}
			if( !found )
				result.add( x );
		}
		return result;
	}
}
