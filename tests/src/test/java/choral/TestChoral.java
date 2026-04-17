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
	private static final String MUSTPASS_MISC = subFolder( MUSTPASS, "Misc" );
	private static final String MUSTFAIL_MISC = subFolder( MUSTFAIL, "Misc" );
	private static final String MOVEMEANT_PASS = subFolder( MUSTPASS, "MoveMeant" );
	private static final String MOVEMEANT_FAIL = subFolder( MUSTFAIL, "MoveMeant" );
	private static final String TYPER_PASS = subFolder( MUSTPASS, "Typer" );
	private static final String TYPER_FAIL = subFolder( MUSTFAIL, "Typer" );
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
	public Stream< DynamicTest > mustPass() {
		CompilationRequestBuilder builder = new CompilationRequestBuilder();
		builder.addSources( "HelloRoles", subFolder( MUSTPASS_MISC, "HelloRoles.ch" ) );
		builder.addSources( "BiPair", subFolder( MUSTPASS_MISC, "BiPair.ch" ) );
		builder.addSources( "ConsumeItems", subFolder( MUSTPASS_MISC, "ConsumeItems" ) );
		builder.addSources( "MyExtClass", subFolder( MUSTPASS_MISC, "ExtendsTest.ch" ) );
		builder.addSources( "RemoteFunction", subFolder( MUSTPASS_MISC, "RemoteFunction.ch" ) );
		builder.addSources( "AuthResult", subFolder( MUSTPASS_MISC, "AuthResult" ) );
		builder.addSources( "AuthResult", subFolder( MUSTPASS_MISC, "AuthToken.ch" ) );
		builder.addSources( "DistAuth", subFolder( MUSTPASS_MISC, "DistAuth" ) );
		builder.addSources( "DistAuth", subFolder( MUSTPASS_MISC, "AuthToken.ch" ) );
		builder.addSources( "BuyerSellerShipper", subFolder( MUSTPASS_MISC, "BuyerSellerShipper" ) );
		builder.addSources( "DiffieHellman", subFolder( MUSTPASS_MISC, "DiffieHellman.ch" ) );
		builder.addSources( "DiffieHellman", subFolder( MUSTPASS_MISC, "BiPair.ch" ) );
		builder.addSources( "TestSwitch", subFolder( MUSTPASS_MISC, "TestSwitch.ch" ) );
		builder.addSources( "LoggerExample", subFolder( MUSTPASS_MISC, "LoggerExample.ch" ) );
		builder.addSources( "IfDesugarTest", subFolder( MUSTPASS_MISC, "IfDesugar" ) );
		builder.addSources( "ChainingExample", subFolder( MUSTPASS_MISC, "ChainingOperator.ch" ) );
		builder.addSources( "BuyBook2", subFolder( MUSTPASS_MISC, "BookSellingSoloist.ch" ) );
		builder.addSources( "CourtesyDefaultMethods", subFolder( MUSTPASS_MISC, "CourtesyDefaultMethods.ch" ) );

		return builder.build().map(request ->
				dynamicTest(request.symbol, new MustPassTest( request )));
	}

	@TestFactory
	public Stream< DynamicTest > mustFail() {
		CompilationRequestBuilder builder = new CompilationRequestBuilder();
		builder.addSources( "WrongType", subFolder( MUSTFAIL_MISC, "WrongType.ch" ) );
		builder.addSources( "MultiFoo", subFolder( MUSTFAIL_MISC, "MultiFoo.ch" ) );
		builder.addSources( "CyclicInheritance_A", subFolder( MUSTFAIL_MISC, "CyclicInheritance.ch" ) );
		builder.addSources( "LotsOfErrors", subFolder( MUSTFAIL_MISC, "LotsOfErrors.ch" ) );
		builder.addSources( "VariableDeclarations", subFolder( MUSTFAIL_MISC, "VariableDeclarations.ch" ) );
		builder.addSources( "TwoWorldList", subFolder( MUSTFAIL_MISC, "Interfaces.ch" ) );
		builder.addSources( "NonMatchingReturnType", subFolder( MUSTFAIL_MISC, "Foo.ch" ) );
		builder.addSources( "MultiFileError", subFolder( MUSTFAIL_MISC, "MultiFileError.ch" ) );
		builder.addSources( "MultiFileError", subFolder( MUSTFAIL_MISC, "ErrorHelper.ch" ) );

		return builder.build().map(request ->
				dynamicTest(request.symbol, new MustFailTest( request )));
	}

	@TestFactory
	public Stream< DynamicTest > typer() {
		CompilationRequestBuilder mustPass = new CompilationRequestBuilder();
		mustPass.addSources("OnDemandImports", subFolder(TYPER_PASS, "OnDemandImports.ch"));
		mustPass.addSources("InterfaceDefaultMethod", subFolder(TYPER_PASS, "InterfaceDefaultMethod.ch"));
		mustPass.addSources("ClassLifterIntegration", subFolder(TYPER_PASS,"ClassLifterIntegration.ch"));
		mustPass.addSources("DualJavaImport", subFolder(TYPER_PASS, "DualJavaImport.ch"));
		mustPass.addSources("StandardLibraryReduction", subFolder(TYPER_PASS, "StandardLibraryReduction.ch"));
		mustPass.addSources("AbstractInheritsAbstract", subFolder(TYPER_PASS, "AbstractInheritsAbstract.ch"));
		mustPass.addSources("ConcreteImplementsAbstract", subFolder(TYPER_PASS, "ConcreteImplementsAbstract.ch"));
		mustPass.addSources("ClassBeatsDefault", subFolder(TYPER_PASS, "ClassBeatsDefault.ch"));
		mustPass.addSources("DiamondDefaultSameOrigin", subFolder(TYPER_PASS, "DiamondDefaultSameOrigin.ch"));
		mustPass.addSources("CovariantReturn", subFolder(TYPER_PASS, "CovariantReturn.ch"));
		mustPass.addSources("FieldInheritance", subFolder(TYPER_PASS, "FieldInheritance.ch"));
		mustPass.addSources("MultiInterfaceInheritance", subFolder(TYPER_PASS, "MultiInterfaceInheritance.ch"));
		mustPass.addSources("FieldHidingDifferentType", subFolder(TYPER_PASS, "FieldHidingDifferentType.ch"));
		mustPass.addSources("ConcreteInheritsConcreteFromSuperclass", subFolder(TYPER_PASS, "ConcreteInheritsConcreteFromSuperclass.ch"));
		mustPass.addSources("StaticHidesStatic", subFolder(TYPER_PASS, "StaticHidesStatic.ch"));
		mustPass.addSources("OverrideProtectedWithPublic", subFolder(TYPER_PASS, "OverrideProtectedWithPublic.ch"));
		mustPass.addSources("AbstractSuperclassTrumpsDefault", subFolder(TYPER_PASS, "AbstractSuperclassTrumpsDefault.ch"));
		mustPass.addSources("DefaultOverriddenByMoreSpecific", subFolder(TYPER_PASS, "DefaultOverriddenByMoreSpecific.ch"));
		mustPass.addSources("InterfaceInheritsMultipleAbstracts", subFolder(TYPER_PASS, "InterfaceInheritsMultipleAbstracts.ch"));
		mustPass.addSources("ConcreteInheritsImplementation", subFolder(TYPER_PASS, "ConcreteInheritsImplementation.ch"));

		CompilationRequestBuilder mustFail = new CompilationRequestBuilder();
		mustFail.addSources("WeakerAccess1", subFolder( TYPER_FAIL, "WeakerAccess1.ch" ));
		mustFail.addSources("WeakerAccess2", subFolder( TYPER_FAIL, "WeakerAccess2.ch" ));
		mustFail.addSources("WeakerAccess3", subFolder( TYPER_FAIL, "WeakerAccess3.ch" ));
		mustFail.addSources("OverrideFinal", subFolder( TYPER_FAIL, "OverrideFinal.ch" ));
		mustFail.addSources("IncompatibleReturnType", subFolder( TYPER_FAIL, "IncompatibleReturnType.ch" ));
		mustFail.addSources("InstanceOverridesStatic", subFolder(TYPER_FAIL, "InstanceOverridesStatic.ch"));
		mustFail.addSources("StaticOverridesInstance", subFolder(TYPER_FAIL, "StaticOverridesInstance.ch"));
		mustFail.addSources("UnimplementedAbstract", subFolder(TYPER_FAIL, "UnimplementedAbstract.ch"));
		mustFail.addSources("ConflictingDefaults", subFolder(TYPER_FAIL, "ConflictingDefaults.ch"));
		mustFail.addSources("AbstractNotImplementedChain", subFolder(TYPER_FAIL, "AbstractNotImplementedChain.ch"));
		mustFail.addSources("OverrideWithWeakerAccessInherited", subFolder(TYPER_FAIL, "OverrideWithWeakerAccessInherited.ch"));
		mustFail.addSources("InterfaceInheritsConflictingDefaults", subFolder(TYPER_FAIL, "InterfaceInheritsConflictingDefaults.ch"));
		// Gap tests — moved to xKnownBugs/ (compiler does not yet check these):
		// mustFail.addSources("DefaultAbstractConflict", subFolder(TYPER_FAIL, "xKnownBugs/DefaultAbstractConflict.ch"));
		// mustFail.addSources("InheritedAbstractsIncompatibleReturn", subFolder(TYPER_FAIL, "xKnownBugs/InheritedAbstractsIncompatibleReturn.ch"));
		// mustFail.addSources("InterfaceInheritsAbstractsIncompatibleReturn", subFolder(TYPER_FAIL, "xKnownBugs/InterfaceInheritsAbstractsIncompatibleReturn.ch"));
		// mustFail.addSources("IncompatibleAbstractMethods", subFolder(TYPER_FAIL, "xKnownBugs/IncompatibleAbstractMethods.ch"));

		return Stream.concat(
				mustPass.build().map(request ->
						dynamicTest(request.symbol, new MustPassTest( request ))),
				mustFail.build().map(request ->
						dynamicTest(request.symbol, new MustFailTest( request )))
		);
	}

	@TestFactory
	public Stream< DynamicTest > moveMeant() {
		CompilationRequestBuilder mustPass = new CompilationRequestBuilder("--infer-comms");
		CompilationRequestBuilder mustFail = new CompilationRequestBuilder("--infer-comms");
		mustPass.addSources( "BiPair", subFolder( MOVEMEANT_PASS, "BiPair.ch" ) );
		mustPass.addSources( "BuyerSellerShipper", subFolder( MOVEMEANT_PASS, "BuyerSellerShipper" ) );
		mustPass.addSources( "ChannelsAsArgs", subFolder( MOVEMEANT_PASS, "ChannelsAsArgs.ch" ) );
		mustPass.addSources( "ChannelsAsArgs", subFolder( MOVEMEANT_PASS, "utils" ) );
		mustPass.addSources( "ChannelsAsFields", subFolder( MOVEMEANT_PASS, "ChannelsAsFields.ch" ) );
		mustPass.addSources( "ChannelsAsFields", subFolder( MOVEMEANT_PASS, "utils" ) );
		mustPass.addSources( "ChannelTypesExample", subFolder( MOVEMEANT_PASS, "ChannelTypesExample.ch" ) );
		mustPass.addSources( "ConsumeItems", subFolder( MOVEMEANT_PASS, "ConsumeItems" ) );
		mustPass.addSources( "DiffieHellman", subFolder( MOVEMEANT_PASS, "DiffieHellman.ch" ) );
		mustPass.addSources( "DiffieHellman", subFolder( MUSTPASS_MISC, "BiPair.ch" ) );
		mustPass.addSources( "DownloadFile", subFolder( MOVEMEANT_PASS, "DownloadFile.ch" ) );
		mustPass.addSources( "DownloadFile", subFolder( MOVEMEANT_PASS, "SendPackets" ) );
		mustPass.addSources( "HelloRoles", subFolder( MOVEMEANT_PASS, "HelloRoles.ch" ) );
		mustPass.addSources( "Increments", subFolder( MOVEMEANT_PASS, "Increments.ch" ) );
		mustPass.addSources( "Karatsuba", subFolder( MOVEMEANT_PASS, "Karatsuba.ch" ) );
		mustPass.addSources( "Mergesort", subFolder( MOVEMEANT_PASS, "Mergesort.ch" ) );
		mustPass.addSources( "OverloadOnRoles", subFolder( MOVEMEANT_PASS, "OverloadOnRoles.ch" ) );
		mustPass.addSources( "PingPong", subFolder( MOVEMEANT_PASS, "PingPong" ) );
		mustPass.addSources( "Quicksort", subFolder( MOVEMEANT_PASS, "Quicksort.ch" ) );
		mustPass.addSources( "RemoteFunction", subFolder( MOVEMEANT_PASS, "RemoteFunction.ch" ) );
		mustPass.addSources( "SendPackets", subFolder( MOVEMEANT_PASS, "SendPackets" ) );
		mustPass.addSources( "SimpleArithmetic", subFolder( MOVEMEANT_PASS, "SimpleArithmetic.ch" ) );
		mustPass.addSources( "SimpleIf3", subFolder( MOVEMEANT_PASS, "SimpleIf3" ) );
		mustPass.addSources( "SimpleKOC", subFolder( MOVEMEANT_PASS, "SimpleKOC.ch" ) );
		mustPass.addSources( "SimpleMethodCalls", subFolder( MOVEMEANT_PASS, "SimpleMethodCalls.ch" ) );
		mustPass.addSources( "SimpleMethodCalls", subFolder( MOVEMEANT_PASS, "utils" ) );
		mustPass.addSources( "SimpleReturns", subFolder( MOVEMEANT_PASS, "SimpleReturns.ch" ) );
		mustPass.addSources( "SimpleVariableReplacement", subFolder( MOVEMEANT_PASS, "SimpleVariableReplacement.ch" ) );
		mustPass.addSources( "SplitAndCombine", subFolder( MOVEMEANT_PASS, "SplitAndCombine" ) );
		mustPass.addSources( "SSOWithRetry", subFolder( MOVEMEANT_PASS, "SSOWithRetry" ) );
		mustPass.addSources( "VitalsStreaming", subFolder( MOVEMEANT_PASS, "VitalsStreaming" ) );

		mustFail.addSources( "AmbiguousRecipient1", subFolder( MOVEMEANT_FAIL, "AmbiguousRecipient1" ) );
		mustFail.addSources( "AmbiguousRecipient2", subFolder( MOVEMEANT_FAIL, "AmbiguousRecipient2" ) );

		return Stream.concat(
				mustPass.build().map(request ->
					dynamicTest(request.symbol, new MustPassTest( request ))),
				mustFail.build().map(request ->
					dynamicTest(request.symbol, new MustFailTest( request )))
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

	/**
	 * Helper class for building CompilationRequest objects.
	 */
	private static class CompilationRequestBuilder {
		private final Map<String, CompilationRequestData> requests = new LinkedHashMap<>();
		private final List<String> flags;

		public CompilationRequestBuilder(String... flags) {
			this.flags = List.of(flags);
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
        return targets.contains(req.symbol());
    }

	/**
	 * Replaces {@code expectedDir} with a fresh recursive copy of {@code projectedDir},
	 * deleting any stale files that no longer exist in the projection.
	 */
	private static void updateSnapshot( Path projectedDir, Path expectedDir ) throws IOException {
		if( Files.exists( expectedDir ) ) {
			try( var walk = Files.walk( expectedDir ) ) {
				walk.sorted( Comparator.reverseOrder() )
						.forEach( p -> p.toFile().delete() );
			}
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
			for( String packageName : packages ) {
				String[] packageList = packageName.split( "\\." );
				List< Path > projectedJavaFiles;
				List< Path > expectedFiles;

				// Get all the projected Java files
				Path projectFolder = Path.of( PROJECTED, packageList );
				try {
					projectedJavaFiles = Files.walk( projectFolder ).filter(
							javaFile -> javaFile.toString().endsWith( ".java" )
					).sorted().toList();
				} catch ( NoSuchFileException e ) {
					errors.add("Failed to compile Choral files");
					continue;
				}

				// If updating, overwrite expectedOutput with the fresh projection now,
				// so that the diff below is a no-op and javac compiles the new files.
				Path expectedFolderPath = Path.of( EXPECTED, packageList );
				if( update ) {
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

				if( projectedJavaFiles.size() != expectedFiles.size() ) {
					errors.add( "The number of projected files (" + projectedJavaFiles.size()
							+ ") does not equal the number of expected files (" + expectedFiles.size() + ").\n"
							+ "  Accept with: mvn test -Dchoral.updateExpected="
							+ compilationRequest.symbol() );
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
						errors.add( "There was a difference between the expected output and "
								+ "the generated output, now printing diff:\n" + diff
								+ "\n  Accept with: mvn test -Dchoral.updateExpected="
								+ compilationRequest.symbol() );
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
