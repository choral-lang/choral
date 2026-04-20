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
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import org.junit.jupiter.api.function.Executable;

public class TestChoral {

	private static final String RUNTIME = Paths.get("..", "runtime", "src", "main", "choral").toString();
	private static final String CHORALUNIT = Paths.get("..", "choral-unit", "src", "main", "choral").toString();
	private static final String MUSTFAIL = Paths.get("src", "main", "choral", "MustFail").toString();
	private static final String MUSTPASS = Paths.get("src", "main", "choral", "MustPass").toString();
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
	private static final int COLUMN_WIDTH = 50;


	///////////////////////////////// ADD TESTS HERE /////////////////////////////////////

	@TestFactory
	public Stream< DynamicTest > Misc() {
		return Stream.concat(
				discoverTests( subFolder( MUSTPASS, "Misc" ) ),
				discoverTests( subFolder( MUSTFAIL, "Misc" ) )
		);
	}

	@TestFactory
	public Stream< DynamicTest > Typer() {
		return Stream.concat(
				discoverTests( subFolder( MUSTPASS, "Typer" ) ),
				discoverTests( subFolder( MUSTFAIL, "Typer" ) )
		);
	}

	@TestFactory
	public Stream< DynamicTest > ClassLifter() {
		return Stream.concat(
				discoverTests( subFolder( MUSTPASS, "ClassLifter" ) ),
				discoverTests( subFolder( MUSTFAIL, "ClassLifter" ) )
		);
	}

	@TestFactory
	public Stream< DynamicTest > MoveMeant() {
		return Stream.concat(
				discoverTests( subFolder( MUSTPASS, "MoveMeant" ), "--infer-comms" ),
				discoverTests( subFolder( MUSTFAIL, "MoveMeant" ), "--infer-comms" )
		);
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
									  List< String > flags) {}

	private static Stream< DynamicTest > discoverTests( String categoryRoot, String... flags ) {
		Path categoryPath = Path.of( categoryRoot );
		Path sharedPath = categoryPath.resolve( "shared" );
		List< DynamicTest > requests = new ArrayList<>();

		boolean isMustPass;
		if ( categoryRoot.startsWith( MUSTPASS ) ) {
			isMustPass = true;
		} else if ( categoryRoot.startsWith( MUSTFAIL ) ) {
			isMustPass = false;
		} else {
			throw new IllegalArgumentException( "Invalid test directory " + categoryRoot +
					": should be contained in '" + MUSTPASS + "' or '" + MUSTFAIL + "'" );
		}

		try( Stream< Path > entries = Files.list( categoryPath ) ) {
			// Make sure the shared/ directory doesn't contain .ch files, since those don't
			// actually get projected!
			if( Files.isDirectory( sharedPath ) ) {
				try( Stream< Path > sharedFiles = Files.walk( sharedPath ) ) {
					List< Path > sharedChoralSources = sharedFiles
							.filter( file -> Files.isRegularFile( file ) && file.toString().endsWith( ".ch" ) )
							.sorted()
							.toList();
					if( !sharedChoralSources.isEmpty() ) {
						throw new IllegalStateException(
								"Shared directory '" + sharedPath + "' contains .ch files. "
										+ "Move shared Choral sources into test-specific roots. Found:\n"
										+ sharedChoralSources.stream().map( Path::toString )
										.collect( Collectors.joining( "\n" ) )
						);
					}
				}
			}

			for( Path test : entries.sorted( Comparator.comparing( path -> path.getFileName().toString() ) ).toList() ) {
				String name = test.getFileName().toString();
				if( name.equals( "shared" ) || name.equals( "xKnownBugs" ) ) {
					continue;
				}

				String symbol;
				String sourcePath;
				if( Files.isRegularFile( test ) && name.endsWith( ".ch" ) ) {
					symbol = name.substring( 0, name.length() - 3 );
					sourcePath = test.toString();
				} else if( Files.isDirectory( test ) ) {
					symbol = name;
					Path mainFile = test.resolve( symbol + ".ch" );
					if( !Files.isRegularFile( mainFile ) ) {
						throw new IllegalStateException(
								"Malformed test directory '" + test
								+ "': expected to find a file called '" + symbol + ".ch'"
						);
					}
					sourcePath = test.toString();
				} else {
					continue;
				}

				List< String > sources = new ArrayList<>();
				sources.add( sourcePath );
				if( Files.isDirectory( sharedPath ) ) {
					sources.add( sharedPath.toString() );
				}

				CompilationRequest request = new CompilationRequest(
						symbol,
						sources,
						List.of(),
						List.of(),
						List.of(),
						List.of( flags )
				);
				if( isMustPass ) {
					requests.add( dynamicTest( symbol, new MustPassTest( request ) ) );
				} else {
					requests.add( dynamicTest( symbol, new MustFailTest( request ) ) );
				}
			}
		} catch( IOException e ) {
			throw new UncheckedIOException( "Unable to discover tests in " + categoryRoot, e );
		}

		return requests.stream();
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

	/**
	 * Resolves Choral source files from a source path.
	 *
	 * <p>The path can point either to a folder (searched recursively) or directly to a
	 * single {@code .ch} file.
	 */
	private static List< Path > getChoralFiles( String sourcePath ) throws IOException {
		Path path = Path.of( sourcePath );
		if( Files.isDirectory( path ) ) {
			try( Stream< Path > files = Files.walk( path ) ) {
				return files
						.filter( file -> file.toString().endsWith( ".ch" ) )
						.toList();
			}
		}

		if( Files.isRegularFile( path ) && path.toString().endsWith( ".ch" ) ) {
			return List.of( path );
		}

		throw new NoSuchFileException( "Choral source path not found (or not a .ch file): " + sourcePath );
	}

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
		for ( String flag : compilationRequest.flags() ) {
			parameters.add( flag );
		}

		int exitCode;
		ByteArrayOutputStream testOutput = new ByteArrayOutputStream();
		ByteArrayOutputStream testError = new ByteArrayOutputStream();
		PrintStream originalError = System.err;

		try {
			System.setErr( new PrintStream( testError ) );
			exitCode = Choral.compile( parameters.toArray( new String[ 0 ] ) );
		} finally {
			System.setErr( originalError );
		}

		return new CompilationResults( exitCode, testOutput.toString(), testError.toString() );
	}


	public static String subFolder( String sourceFolder, String subFolder ) {
		return sourceFolder + File.separator + subFolder;
	}

	/**
	 * Fails the current test, printing full error details to stdout (so they appear in the
	 * clean per-test section) and throwing an {@link AssertionError} with just the symbol
	 * name and an empty stack trace (so the Surefire failure block stays minimal).
	 */
	private static void fail( String symbol, List< String > errors ) {
		String details = errors.stream()
				.map( e -> "    " + e.replace( "\n", "\n    " ) )
				.collect( Collectors.joining( "\n" ) );
		AssertionError e = new AssertionError( symbol + "\n\n" + details + "\n" );
		e.setStackTrace( new StackTraceElement[ 0 ] );
		throw e;
	}

	/**
	 * Returns {@code true} if the expected output for this compilation request should be
	 * updated instead of diffed, based on the {@code choral.updateExpected} system property.
	 *
	 * <p>The property value is a comma-separated list of test names.
	 * Example: {@code mvn test -Dchoral.updateExpected=MyTest1,MyTest2}
	 */
	private static boolean shouldUpdate( CompilationRequest req ) {
		String prop = System.getProperty( "choral.updateExpected", "" ).trim();
		if( prop.isEmpty() ) return false;
		Set< String > targets = Arrays.stream( prop.split( "," ) )
				.map( String::trim )
				.filter( s -> !s.isEmpty() )
				.collect( Collectors.toSet() );
        return targets.contains("all") || targets.contains(req.symbol());
    }

	/**
	 * Replaces {@code expectedDir} with a fresh recursive copy of {@code projectedDir},
	 * deleting any stale files that no longer exist in the projection.
	 */
	private static void updateSnapshot( Path projectedDir, Path expectedDir ) throws IOException {
		if( Files.exists( expectedDir ) ) {
			deleteRecursively( expectedDir );
		}
		try( var walk = Files.walk( projectedDir ) ) {
			walk.forEach( src -> {
				Path dest = expectedDir.resolve( projectedDir.relativize( src ) );
				try {
					if( Files.isDirectory( src ) ) Files.createDirectories( dest );
					else Files.copy( src, dest, StandardCopyOption.REPLACE_EXISTING );
				} catch( IOException e ) {
					throw new UncheckedIOException( e );
				}
			} );
		}
	}

	private static void deleteRecursively( Path root ) throws IOException {
		if( !Files.exists( root ) ) {
			return;
		}
		try( var walk = Files.walk( root ) ) {
			walk.sorted( Comparator.reverseOrder() )
					.forEach( p -> p.toFile().delete() );
		}
	}

	/** Compiles the test, expecting it to succeed. */
	private static void project( CompilationRequest compilationRequest ) {
		ArrayList< String > errors = new ArrayList<>();
		boolean update = shouldUpdate( compilationRequest );

		CompilationResults results = compile(compilationRequest);
		if( results.exitCode != 0 )
			errors.add( "Compiling Choral files failed unexpectedly with exit code " + results.exitCode +
					"\n" + results.stderr );

		try {
			// Get the package names declared in the source folders of the compilation request
			HashSet< String > packages = new HashSet<>();
			for( String sourcePath : compilationRequest.sourceFolder() ) {
				List< Path > choralFiles;
				try {
					choralFiles = getChoralFiles( sourcePath );
				} catch( IOException e ) {
					errors.add( "Source path not found: " + sourcePath );
					continue;
				}
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
			List< Path > missingProjectedFiles = new ArrayList<>();
			List< Path > newProjectedFiles = new ArrayList<>();
			List< String > fileDiffs = new ArrayList<>();
			boolean filesDiffer = false;
			for( String packageName : new TreeSet<>( packages ) ) {
				String[] packageList = packageName.split( "\\." );
				List< Path > projectedJavaFiles;
				List< Path > expectedFiles;
				Path expectedFolderPath = Path.of( EXPECTED, packageList );

				// Get all the projected Java files
				Path projectFolder = Path.of( PROJECTED, packageList );
				try {
					projectedJavaFiles = Files.walk( projectFolder ).filter(
							javaFile -> javaFile.toString().endsWith( ".java" )
					).sorted().toList();
				} catch ( NoSuchFileException e ) {
					projectedJavaFiles = List.of();
					if( update ) {
						deleteRecursively( expectedFolderPath );
					}
				}

				// If updating, overwrite expectedOutput with the fresh projection now,
				// so that the diff below is a no-op and javac compiles the new files.
				if( update && Files.isDirectory( projectFolder ) ) {
					updateSnapshot( projectFolder, expectedFolderPath );
					System.out.printf( "%-" + COLUMN_WIDTH + "s %s[SNAPSHOT UPDATED]%s%n",
							compilationRequest.symbol, GREEN, RESET );
				}

				// Get the expected Java files
				try {
					expectedFiles = Files.walk( expectedFolderPath ).filter(
							expectedFile -> expectedFile.toString().endsWith( ".java" )
					).sorted().toList();
				} catch ( NoSuchFileException e ) {
					if( projectedJavaFiles.isEmpty() ) {
						continue;
					}
					errors.add(
							"No snapshot found for '" + compilationRequest.symbol() + "' in /tests/expectedOutput/.\n\n"
							+ projectedJavaFiles.stream()
									.map( p -> {
										try {
											return "=== " + p + " ===\n" + Files.readString( p );
										} catch( IOException ex ) {
											return "=== " + p + " === (could not read: " + ex.getMessage() + ")";
										}
									} )
									.collect( Collectors.joining( "\n" ) )
							+ "\n=============================================================================="
							+ "\nTo accept the new snapshot, run: mvn test -Dchoral.updateExpected="
							+ compilationRequest.symbol() );
					continue;
				}

				// PHASE 1: CHECK IF EXPECTED AND PROJECTED CODE DIFFER

				Map< String, Path > projectedByName = projectedJavaFiles.stream().collect(
						Collectors.toMap(
								p -> p.getFileName().toString(),
								p -> p,
								(a, b) -> a,
								TreeMap::new
						)
				);
				Map< String, Path > expectedByName = expectedFiles.stream().collect(
						Collectors.toMap(
								p -> p.getFileName().toString(),
								p -> p,
								(a, b) -> a,
								TreeMap::new
						)
				);

				Set< String > missingProjected = new TreeSet<>( expectedByName.keySet() );
				missingProjected.removeAll( projectedByName.keySet() );
				if( !missingProjected.isEmpty() ) {
					filesDiffer = true;
					for( String fileName : missingProjected ) {
						missingProjectedFiles.add( expectedByName.get( fileName ) );
					}
				}

				Set< String > extraProjected = new TreeSet<>( projectedByName.keySet() );
				extraProjected.removeAll( expectedByName.keySet() );
				if( !extraProjected.isEmpty() ) {
					filesDiffer = true;
					for( String fileName : extraProjected ) {
						newProjectedFiles.add( projectedByName.get( fileName ) );
					}
				}

				Set< String > commonFiles = new TreeSet<>( expectedByName.keySet() );
				commonFiles.retainAll( projectedByName.keySet() );
				for( String fileName : commonFiles ) {
					Path expectedFile = expectedByName.get( fileName );
					Path projectedFile = projectedByName.get( fileName );
					List< String > original = Files.readAllLines( expectedFile );
					List< String > projected = Files.readAllLines( projectedFile );

					Patch< String > patch = DiffUtils.diff( original, projected );

					List<String> diffOutput = UnifiedDiffUtils.generateUnifiedDiff(
							expectedFile.toString(),
							projectedFile.toString(),
							original,
							patch,
							3
					);
					if ( !diffOutput.isEmpty() ) {
						filesDiffer = true;
						fileDiffs.add( String.join( "\n", diffOutput ) );
					}
				}

				if( filesDiffer ) {
					continue;
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

			for( Path missingProjectedFile : missingProjectedFiles ) {
				errors.add( "Missing projected file: " + missingProjectedFile + "\n" );
			}

			for( Path newProjectedFile : newProjectedFiles ) {
				String content;
				try {
					content = Files.readString( newProjectedFile );
				} catch( IOException e ) {
					content = "(could not read: " + e.getMessage() + ")";
				}
				errors.add( "Unexpected projected file:\n=== " + newProjectedFile + " ===\n" + content );
			}

			for( String diff : fileDiffs ) {
				errors.add( "Projected output differs from expected output:\n" + diff + "\n" );
			}

			if ( filesDiffer ) {
				errors.add( "Accept changes by running: mvn test -Dchoral.updateExpected="
						+ compilationRequest.symbol() + "\n" );
			}

		} catch( Throwable e ) {
			errors.add( e.toString() );
		}

		if( !errors.isEmpty() ) {
			System.out.printf( "%-" + COLUMN_WIDTH + "s %s[ERROR]%s%n", compilationRequest.symbol, RED, RESET );
			fail( compilationRequest.symbol, errors );
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
			errors.add("Compilation succeeded unexpectedly. This test should have compilation errors." );

		String[] outputLines = results.stderr.split( "\n" );
		List<TestError> actualErrors = findActualErrors(outputLines);

		// Concatenate all the expected errors in all the files in the source folders
		ArrayList<TestError> expectedErrors = new ArrayList<>();
		for( String sourcePath : compilationRequest.sourceFolder() ) {
			List< Path > testFiles;
			try {
				testFiles = getChoralFiles( sourcePath );
			} catch (IOException e) {
				errors.add( "Source path not found: " + sourcePath );
				continue;
			}

			testFiles.forEach( file -> {
				try {
					String[] fileContent = Files.readString( file ).split( "\n" );
					expectedErrors.addAll( findExpectedErrors( file.toString(), fileContent ) );
				} catch( IOException e ) {
					errors.add( "Error reading file '" + file + "': " + e.getMessage() );
				}
			} );
		}

		// Add an error for each expected error not found in actual errors, and vice versa.
		// The expected error can be a substring of the actual error.
		for (TestError expected : subtract(expectedErrors, actualErrors)) {
			errors.add("Expected error on line " + expected.line() +
				":\t" + expected.message());
		}
		for (TestError actual : subtract(actualErrors, expectedErrors)) {
			errors.add("Unexpected error on line " + actual.line() +
				":\t" + actual.message());
		}

		if (errors.isEmpty()){
			System.out.printf( "%-" + COLUMN_WIDTH + "s %s[OK]%s%n", compilationRequest.symbol, GREEN, RESET );
		}
		else {
			System.out.printf( "%-" + COLUMN_WIDTH + "s %s[ERROR]%s%n", compilationRequest.symbol, RED, RESET );
			fail( compilationRequest.symbol, errors );
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
