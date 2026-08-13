package lsp.features;

import choral.ast.CompilationUnit;
import choral.compiler.HeaderLoader;
import choral.compiler.Parser;
import choral.compiler.Typer;
import choral.compiler.TyperOptions;
import choral.utils.VerbosityLevel;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Parses and type-checks a Choral source file together with its source imports.
 * This class deliberately has no dependency on LSP4J so its result can be used
 * by diagnostics, diagrams, and other language-server features.
 */
public class TypedSourceAnalyzer {
	public AnalysisResult analyze( String uri, String content ) {
		return analyze( uri, content, Map.of() );
	}

	public AnalysisResult analyze(
			String uri, String content, Map< String, String > openDocuments
	) {
		List< AnalysisWarning > warnings = new ArrayList<>();
		CompilationUnit compUnit;

		try {
			compUnit = Parser.parseString( content, sourceFile( uri ) );
		} catch( Exception exception ) {
			return AnalysisResult.failure( AnalysisFailure.PARSE_ERROR, exception );
		}

		try {
			typeCheck( uri, sourceUnits( uri, compUnit, sourceOverlays( openDocuments ) ),
					( position, message ) -> warnings.add(
							new AnalysisWarning( position, message ) ) );
		} catch( Exception exception ) {
			return AnalysisResult.failure(
					AnalysisFailure.TYPE_ERROR, exception, warnings );
		}

		return AnalysisResult.success( compUnit, warnings );
	}

	public enum AnalysisFailure {
		PARSE_ERROR,
		TYPE_ERROR
	}

	public record AnalysisWarning( choral.ast.Position position, String message ) {
	}

	public record AnalysisResult(
			CompilationUnit compilationUnit,
			List< AnalysisWarning > warnings,
			AnalysisFailure failure,
			Exception exception
	) {
		private static AnalysisResult success(
				CompilationUnit compilationUnit, List< AnalysisWarning > warnings
		) {
			return new AnalysisResult(
					compilationUnit, List.copyOf( warnings ), null, null );
		}

		private static AnalysisResult failure(
				AnalysisFailure failure, Exception exception
		) {
			return failure( failure, exception, List.of() );
		}

		private static AnalysisResult failure(
				AnalysisFailure failure, Exception exception,
				List< AnalysisWarning > warnings
		) {
			return new AnalysisResult(
					null, List.copyOf( warnings ), failure, exception );
		}

		public boolean successful() {
			return failure == null;
		}

		public String failureMessage() {
			return exception == null ? null : exception.getMessage();
		}
	}

	private static void typeCheck(
			String uri, List< CompilationUnit > sourceUnits,
			java.util.function.BiConsumer< choral.ast.Position, String > infoChannel
	) throws Exception {
		List< CompilationUnit > headerUnits = loadHeaders( uri, sourceUnits );
		TyperOptions typerOptions = new TyperOptions( VerbosityLevel.WARNINGS )
				.withInfoChannel( infoChannel );
		Typer.annotate( sourceUnits, headerUnits, typerOptions );
	}

	private static List< CompilationUnit > sourceUnits(
			String uri, CompilationUnit activeUnit, Map< Path, String > sourceOverlays
	) throws Exception {
		Path activePath = sourcePath( uri );
		if( activePath == null ) return List.of( activeUnit );
		Path sourceRoot = sourceRoot( activePath, activeUnit );
		Map< Path, CompilationUnit > units = new LinkedHashMap<>();
		ArrayDeque< SourceUnit > pending = new ArrayDeque<>();
		Path normalisedActivePath = normalise( activePath );
		units.put( normalisedActivePath, activeUnit );
		pending.add( new SourceUnit( normalisedActivePath, activeUnit ) );
		while( !pending.isEmpty() ) {
			SourceUnit source = pending.remove();
			for( var imported : source.unit().imports() ) {
				String name = imported.name();
				if( imported.isOnDemand() ) {
					String packageName = name.substring( 0, name.length() - 2 );
					Path packagePath = sourceRoot.resolve(
							packageName.replace( '.', java.io.File.separatorChar ) );
					for( Path path : packageSources( packagePath, sourceOverlays ) )
						addSource( path, packageName, true,
								sourceOverlays, units, pending );
				} else {
					String simpleName = name.substring( name.lastIndexOf( '.' ) + 1 );
					addSource( source.path().getParent().resolve( simpleName + ".ch" ),
							name, false, sourceOverlays, units, pending );
					addSource( sourceRoot.resolve(
							name.replace( '.', java.io.File.separatorChar ) + ".ch" ),
							name, false, sourceOverlays, units, pending );
				}
			}
		}
		return List.copyOf( units.values() );
	}

	private static Set< Path > packageSources(
			Path packagePath, Map< Path, String > sourceOverlays
	) throws Exception {
		Path normalisedPackagePath = normalise( packagePath );
		Set< Path > sources = new LinkedHashSet<>();
		if( Files.isDirectory( normalisedPackagePath ) ) {
			try( Stream< Path > files = Files.list( normalisedPackagePath ) ) {
				files.filter( TypedSourceAnalyzer::isChoralSource )
						.sorted()
						.map( TypedSourceAnalyzer::normalise )
						.forEach( sources::add );
			}
		}
		sourceOverlays.keySet().stream()
				.filter( TypedSourceAnalyzer::hasChoralExtension )
				.filter( path -> normalisedPackagePath.equals( path.getParent() ) )
				.sorted()
				.forEach( sources::add );
		return sources;
	}

	private static void addSource(
			Path path, String importedName, boolean onDemand,
			Map< Path, String > sourceOverlays,
			Map< Path, CompilationUnit > units, ArrayDeque< SourceUnit > pending
	) throws Exception {
		Path normalised = normalise( path );
		if( units.containsKey( normalised ) ) return;
		String sourceCode = sourceOverlays.get( normalised );
		if( sourceCode == null && !Files.isRegularFile( normalised ) ) return;
		CompilationUnit unit = sourceCode == null
				? Parser.parseSourceFile( normalised.toFile() )
				: Parser.parseString( sourceCode, normalised.toString() );
		if( !declaresImport( unit, importedName, onDemand ) ) return;
		units.put( normalised, unit );
		pending.add( new SourceUnit( normalised, unit ) );
	}

	private static boolean declaresImport(
			CompilationUnit unit, String importedName, boolean onDemand
	) {
		String packageName = unit.packageDeclaration().orElse( "" );
		if( onDemand ) return packageName.equals( importedName );
		String prefix = packageName.isEmpty() ? "" : packageName + ".";
		return Stream.of( unit.classes(), unit.interfaces(), unit.enums() )
				.flatMap( List::stream )
				.anyMatch( declaration -> ( prefix + declaration.name().identifier() )
						.equals( importedName ) );
	}

	private static Path sourceRoot( Path activePath, CompilationUnit activeUnit ) {
		Path parent = normalise( activePath ).getParent();
		if( parent == null ) return normalise( activePath );
		String packageName = activeUnit.packageDeclaration().orElse( "" );
		if( packageName.isEmpty() ) return parent;
		Path root = parent;
		String[] parts = packageName.split( "\\." );
		for( int index = parts.length - 1; index >= 0; index-- ) {
			if( root.getFileName() == null ||
					!root.getFileName().toString().equals( parts[ index ] ) )
				return parent;
			root = root.getParent();
			if( root == null ) return parent;
		}
		return root;
	}

	private static Map< Path, String > sourceOverlays(
			Map< String, String > openDocuments
	) {
		Map< Path, String > overlays = new LinkedHashMap<>();
		openDocuments.forEach( ( uri, content ) -> {
			Path path = sourcePath( uri );
			if( path != null ) overlays.put( normalise( path ), content );
		} );
		return overlays;
	}

	private static boolean isChoralSource( Path path ) {
		return Files.isRegularFile( path ) && hasChoralExtension( path );
	}

	private static boolean hasChoralExtension( Path path ) {
		return path.toString().toLowerCase().endsWith( ".ch" );
	}

	private static Path sourcePath( String uri ) {
		if( uri == null || !uri.startsWith( "file:" ) ) return null;
		return Paths.get( URI.create( uri ) );
	}

	private static Path normalise( Path path ) {
		return path.toAbsolutePath().normalize();
	}

	private record SourceUnit( Path path, CompilationUnit unit ) {
	}

	private static String sourceFile( String uri ) {
		Path path = sourcePath( uri );
		return path == null ? null : path.toString();
	}

	private static List< CompilationUnit > loadHeaders(
			String uri, List< CompilationUnit > sourceUnits
	) throws Exception {
		Stream< CompilationUnit > standardHeaders = HeaderLoader.loadStandardProfile();
		Path documentPath = sourcePath( uri );
		if( documentPath == null || documentPath.getParent() == null )
			return standardHeaders.toList();
		List< File > sourceFiles = sourceUnits.stream()
				.map( unit -> unit.position().sourceFile() )
				.filter( java.util.Objects::nonNull )
				.map( File::new )
				.toList();
		return Stream.concat( standardHeaders, HeaderLoader.loadFromPath(
				List.of( documentPath.getParent() ), sourceFiles, true, true ) )
				.toList();
	}
}
