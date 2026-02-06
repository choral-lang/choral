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
import java.nio.file.NoSuchFileException;
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

	private static final String RUNTIME = Paths.get("..", "runtime", "src", "main", "choral").toString();
	private static final String CHORALUNIT = Paths.get("..", "choral-unit", "src", "main", "choral").toString();
	private static final String MUSTFAIL = Paths.get("src", "main", "choral", "MustFail").toString();
	private static final String MUSTPASS = Paths.get("src", "main", "choral", "MustPass").toString();
	private static final String MOVEMEANT = subFolder( MUSTPASS, "MoveMeant" );
	private static final String BASE_PATH = Paths.get("base", "src", "main", "java").toString();
	private static final String RUNTIME_PATH = Paths.get("runtime", "src", "main", "java").toString();
	/** Location of Java code produced by the Choral compiler. */
	private static final String PROJECTED = "projectedOutput";
	/** Location of Java code that we compare against projected code.  */
	private static final String EXPECTED = "expectedOutput";
	/** List of paths to search for Java sources when compiling projected code. */
	private static final List<String> JAVA_SOURCES = List.of( BASE_PATH, RUNTIME_PATH, EXPECTED );

	// Formatting for terminal output
	private static final String GREEN = "\u001B[32m";
	private static final String RED = "\u001B[31m";
	private static final String RESET = "\u001B[0m";
	private static final int COLUMN_WIDTH = 30;


	///////////////////////////////// ADD TESTS HERE /////////////////////////////////////

	@TestFactory
	public Stream< DynamicTest > mustPass() {
		CompilationRequestBuilder builder = new CompilationRequestBuilder();
		builder.addSources( "HelloRoles", subFolder( MUSTPASS, "HelloRoles" ) );
		builder.addSources( "BiPair", subFolder( MUSTPASS, "BiPair" ) );
		builder.addSources( "ConsumeItems", subFolder( MUSTPASS, "ConsumeItems" ) );
		builder.addSources( "MyExtClass", subFolder( MUSTPASS, "ExtendsTest" ) );
		builder.addSources( "RemoteFunction", subFolder( MUSTPASS, "RemoteFunction" ) );
		builder.addSources( "AuthResult", subFolder( MUSTPASS, "AuthResult" ) );
		builder.addSources( "AuthResult", subFolder( MUSTPASS, "DistAuthUtils" ) );
		builder.addSources( "DistAuth", subFolder( MUSTPASS, "DistAuth" ) );
		builder.addSources( "DistAuth", subFolder( MUSTPASS, "DistAuthUtils" ) );
		builder.addSources( "BuyerSellerShipper", subFolder( MUSTPASS, "BuyerSellerShipper" ) );
		builder.addSources( "DiffieHellman", subFolder( MUSTPASS, "DiffieHellman" ) );
		builder.addSources( "DiffieHellman", subFolder( MUSTPASS, "BiPair" ) );
		builder.addSources( "TestSwitch", subFolder( MUSTPASS, "TestSwitch" ) );
		builder.addSources( "LoggerExample", subFolder( MUSTPASS, "LoggerExample" ) );
		builder.addSources( "IfDesugarTest", subFolder( MUSTPASS, "IfDesugar" ) );
		builder.addSources( "ChainingExample", subFolder( MUSTPASS, "ChainingOperator" ) );
		builder.addSources( "BuyBook2", subFolder( MUSTPASS, "BookSellingSoloist" ) );
		//// https://github.com/choral-lang/choral/issues/29
		// builder.addSources( "SwitchTest", subFolder( MUSTPASS, "SwitchTest" ) );
		//// https://github.com/choral-lang/choral/issues/27
		// builder.addSources( "MirrorChannel", subFolder( MUSTPASS, "MirrorChannel" ) );
		//// https://github.com/choral-lang/choral/issues/28
		// builder.addSources( "Autoboxing", subFolder( MUSTPASS, "Autoboxing" ) );

		return builder.build().map(request ->
				dynamicTest(request.symbol, new MustPassTest( request )));
	}

	@TestFactory
	public Stream< DynamicTest > mustFail() {
		CompilationRequestBuilder builder = new CompilationRequestBuilder();
		builder.addSources( "WrongType", subFolder( MUSTFAIL, "WrongType" ) );
		builder.addSources( "MultiFoo", subFolder( MUSTFAIL, "MultiFoo" ) );
		builder.addSources( "CyclicInheritance_A", subFolder( MUSTFAIL, "CyclicInheritance" ) );
		builder.addSources( "LotsOfErrors", subFolder( MUSTFAIL, "LotsOfErrors" ) );
		builder.addSources( "VariableDeclarations", subFolder( MUSTFAIL, "VariableDeclarations" ) );
		builder.addSources( "TwoWorldList", subFolder( MUSTFAIL, "IllegalInheritance" ) );
		builder.addSources( "NonMatchingReturnType", subFolder( MUSTFAIL, "NonMatchingReturnType" ) );
		builder.addSources( "MultiFileError", subFolder( MUSTFAIL, "MultiFileError" ) );
		builder.addSources( "MultiFileError", subFolder( MUSTFAIL, "MultiFileErrorUtil" ) );

		return builder.build().map(request ->
				dynamicTest(request.symbol, new MustFailTest( request )));
	}

	@TestFactory
	public Stream< DynamicTest > mustPassCommInfer() {
		CompilationRequestBuilder builder = new CompilationRequestBuilder("--infer-comms");
		builder.addSources( "BiPair", subFolder( MOVEMEANT, "BiPair" ) );
		builder.addSources( "BuyerSellerShipper", subFolder( MOVEMEANT, "BuyerSellerShipper" ) );
		builder.addSources( "ChannelsAsArgs", subFolder( MOVEMEANT, "ChannelsAsArgs" ) );
		builder.addSources( "ChannelsAsArgs", subFolder( MOVEMEANT, "utils" ) );
		builder.addSources( "ChannelsAsFields", subFolder( MOVEMEANT, "ChannelsAsFields" ) );
		builder.addSources( "ChannelsAsFields", subFolder( MOVEMEANT, "utils" ) );
		builder.addSources( "ChannelTypesExample", subFolder( MOVEMEANT, "ChannelTypesExample" ) );
		builder.addSources( "ConsumeItems", subFolder( MOVEMEANT, "ConsumeItems" ) );
		builder.addSources( "DiffieHellman", subFolder( MOVEMEANT, "DiffieHellman" ) );
		builder.addSources( "DiffieHellman", subFolder( MUSTPASS, "BiPair" ) );
		//// Mysterious OOM
		// builder.addSources( "DistributedAuthentication", subFolder( MOVEMEANT, "DistributedAuthentication" ) );
		// builder.addSources( "DistributedAuthentication", subFolder( MUSTPASS, "BiPair" ) );
		builder.addSources( "DownloadFile", subFolder( MOVEMEANT, "DownloadFile" ) );
		builder.addSources( "DownloadFile", subFolder( MOVEMEANT, "SendPackets" ) );
		builder.addSources( "HelloRoles", subFolder( MOVEMEANT, "HelloRoles" ) );
		builder.addSources( "Increments", subFolder( MOVEMEANT, "Increments" ) );
		builder.addSources( "Karatsuba", subFolder( MOVEMEANT, "Karatsuba" ) );
		builder.addSources( "Mergesort", subFolder( MOVEMEANT, "Mergesort" ) );
		builder.addSources( "NestedBlocks", subFolder( MOVEMEANT, "NestedBlocks" ) );
		builder.addSources( "OverloadOnRoles", subFolder( MOVEMEANT, "OverloadOnRoles" ) );
		builder.addSources( "PingPong", subFolder( MOVEMEANT, "PingPong" ) );
		builder.addSources( "Quicksort", subFolder( MOVEMEANT, "Quicksort" ) );
		builder.addSources( "RemoteFunction", subFolder( MOVEMEANT, "RemoteFunction" ) );
		builder.addSources( "SendPackets", subFolder( MOVEMEANT, "SendPackets" ) );
		builder.addSources( "SimpleArithmetic", subFolder( MOVEMEANT, "SimpleArithmetic" ) );
		builder.addSources( "SimpleIf3", subFolder( MOVEMEANT, "SimpleIf3" ) );
		builder.addSources( "SimpleIfStatements", subFolder( MOVEMEANT, "SimpleIfStatements" ) );
		builder.addSources( "SimpleKOC", subFolder( MOVEMEANT, "SimpleKOC" ) );
		builder.addSources( "SimpleMethodCalls", subFolder( MOVEMEANT, "SimpleMethodCalls" ) );
		builder.addSources( "SimpleReturns", subFolder( MOVEMEANT, "SimpleReturns" ) );
		builder.addSources( "SimpleVariableReplacement", subFolder( MOVEMEANT, "SimpleVariableReplacement" ) );
		builder.addSources( "SplitAndCombine", subFolder( MOVEMEANT, "SplitAndCombine" ) );
		builder.addSources( "SsoWithRetry", subFolder( MOVEMEANT, "SsoWithRetry" ) );
		builder.addSources( "VitalsStreaming", subFolder( MOVEMEANT, "VitalsStreaming" ) );

		return builder.build().map(request ->
				dynamicTest(request.symbol, new MustPassTest( request )));
	}

	///////////////////////////////// DATATYPES /////////////////////////////////////

	/**
	 * Container representing a Choral black-box test case.
	 *
	 * @param symbol Name of the Choral class we're trying to compile
	 * @param sourceFolder Location of the Choral class and its dependencies
	 * @param javaSources Location of the Java sources needed to compile the expected output (e.g., runtime library)
	 * @param worlds Which worlds to project. Defaults to all worlds.
	 * @param classPaths Classpaths needed to compile the expected output (e.g., external libraries).
	 * @param flags Extra flags to pass to the Choral compiler (e.g., --infer-comms).
	 */
	private record CompilationRequest(String symbol,
									  List< String > sourceFolder,
									  List< String > javaSources,
									  List< String > worlds,
									  List< String > classPaths,
									  String flags) {}

	/**
	 * Helper class for building CompilationRequest objects.
	 */
	private static class CompilationRequestBuilder {
		private final Map<String, CompilationRequestData> requests = new LinkedHashMap<>();
		private final String flags;

		public CompilationRequestBuilder(String flags) {
			this.flags = flags;
		}

		public CompilationRequestBuilder() {
			this("");
		}

		private class CompilationRequestData {
			final String symbol;
			final List<String> sourceFolders = new ArrayList<>();
			final List<String> javaSources = new ArrayList<>();
			final List<String> worlds = new ArrayList<>();
			final List<String> classPaths = new ArrayList<>();

			CompilationRequestData(String symbol) {
				this.symbol = symbol;
			}

			CompilationRequest build() {
				return new CompilationRequest(symbol, sourceFolders, javaSources, worlds, classPaths, flags);
			}
		}

		public CompilationRequestBuilder addSources(String symbol, String sourceFolder) {
			requests.computeIfAbsent(symbol, CompilationRequestData::new)
					.sourceFolders.add(sourceFolder);
			return this;
		}

		public CompilationRequestBuilder addJavaSources(String symbol, String javaSources) {
			requests.computeIfAbsent(symbol, CompilationRequestData::new)
					.javaSources.add(javaSources);
			return this;
		}

		public CompilationRequestBuilder addWorlds(String symbol, String... worlds) {
			requests.computeIfAbsent(symbol, CompilationRequestData::new)
					.worlds.addAll(Arrays.asList(worlds));
			return this;
		}

		public CompilationRequestBuilder addClassPaths(String symbol, String... classPaths) {
			requests.computeIfAbsent(symbol, CompilationRequestData::new)
					.classPaths.addAll(Arrays.asList(classPaths));
			return this;
		}

		public Stream<CompilationRequest> build() {
			return requests.values().stream().map(CompilationRequestData::build);
		}
	}

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


	///////////////////////////////// HELPERS /////////////////////////////////////

	/** Tries to compile the test and returns the results produced by the Choral compiler. */
	private static CompilationResults compile(CompilationRequest compilationRequest){
		ArrayList< String > parameters = new ArrayList<>();
		parameters.add( "epp" );
		parameters.add( "--verbosity=DEBUG" );
		parameters.add( "--headers=" +
				String.join( File.pathSeparator, compilationRequest.sourceFolder() ) +
				File.pathSeparator + RUNTIME + File.pathSeparator + CHORALUNIT );
		parameters.add( "-t" );
		parameters.add( PROJECTED );
		parameters.add( "-s" );
		parameters.add( String.join( File.pathSeparator, compilationRequest.sourceFolder() ) );
		parameters.add( compilationRequest.symbol() );
		parameters.addAll( compilationRequest.worlds() );
		parameters.add( "--annotate" );
		parameters.add( compilationRequest.flags() );

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

	/** Compiles the test, expecting it to succeed. */
	private static void project( CompilationRequest compilationRequest ) {
		ArrayList< String > errors = new ArrayList<>();

		CompilationResults results = compile(compilationRequest);
		if( results.exitCode != 0 )
			errors.add( "Compilation failed unexpectedly with exit code " + results.exitCode +
					"\n" + results.stdout );

		try {
			// Get the package names declared in the source folders of the compilation request
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
						errors.add( "Missing package declaration in file: " + file );
						continue;
					}
					String pathString = fileContent.substring(i + 7, j).trim(); // 'package' = 7 characters
					packages.add( pathString );
				}
			}

			// For each package, compare the projected Java files with the expected ones,
			// and try compiling the expected ones.
			for( String packageName : packages ) {
				String[] packageList = packageName.split( "\\." );
				List< Path > projectedJavaFiles;
				List< Path > expectedFiles;

				// Get all the projected and expected Java files
				Path projectFolder = Path.of( PROJECTED, packageList );
				projectedJavaFiles = Files.walk( projectFolder ).filter(
						javaFile -> javaFile.toString().endsWith( ".java" )
				).sorted().toList();

				try {
					Path expectedFolderPath = Path.of( EXPECTED, packageList );
					expectedFiles = Files.walk( expectedFolderPath ).filter(
							expectedFile -> expectedFile.toString().endsWith( ".java" )
					).sorted().toList();
				}
				catch ( NoSuchFileException e ) {
					errors.add("Missing files in the expectedOutput directory: " + e.getMessage());
					continue;
				}

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

				options.add( "-sourcepath" );
				var sources = new ArrayList<>( JAVA_SOURCES );
				sources.addAll( compilationRequest.javaSources() );
				options.add( String.join( File.pathSeparator, sources ) );

				if( !compilationRequest.classPaths.isEmpty() ) {
					options.add( "-classpath" );
					options.add( String.join( File.pathSeparator, compilationRequest.classPaths ) );
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
					String javaErrors = String.join( "\n", javaCompilationErrors );
					errors.add( "Expected Java code does not compile:\n" + javaErrors );
				}
			}
		} catch( Throwable e ) {
			errors.add( e.toString() );
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
