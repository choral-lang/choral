package lsp.features;

import choral.ast.CompilationUnit;
import choral.compiler.HeaderLoader;
import choral.compiler.Parser;
import choral.compiler.Typer;
import choral.compiler.TyperOptions;
import choral.exceptions.AstPositionedException;
import choral.exceptions.ChoralCompoundException;
import choral.exceptions.ChoralException;
import choral.utils.VerbosityLevel;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.services.LanguageClient;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class DiagnosticsProvider {
	private LanguageClient client;

	public void setClient( LanguageClient client ) {
		this.client = client;
	}

	public List< Diagnostic > analyze( String uri, String content ) {
		return analyzeTyped( uri, content ).diagnostics();
	}

	public AnalysisResult analyzeTyped( String uri, String content ) {
		List< Diagnostic > diagnostics = new ArrayList<>();
		CompilationUnit compUnit;

		try {
			compUnit = Parser.parseString( content, sourceFile( uri ) );
		} catch( Exception e ) {
			addErrorDiagnostics( diagnostics, e );
			return AnalysisResult.failure(
					diagnostics, AnalysisFailure.PARSE_ERROR, e.getMessage() );
		}

		try { // choral compiler errors are reported through exceptions
			// so try to type the program, then catch any exceptions
			// to pass along error messages to language client
			typeCheck( uri, sourceUnits( uri, compUnit ), ( pos, s ) -> {
				if( pos == null ) pos = new choral.ast.Position( null, 1, 1 );
				Diagnostic diagnostic = warningDiagnostic( s );
				setRange( diagnostic, pos );
				diagnostics.add( diagnostic );
			} );
		} catch( Exception e ) {
			addErrorDiagnostics( diagnostics, e );
			return AnalysisResult.failure(
					diagnostics, AnalysisFailure.TYPE_ERROR, e.getMessage() );
		}

		return AnalysisResult.success( compUnit, diagnostics );
	}

	private static void addErrorDiagnostics( List< Diagnostic > diagnostics, Exception exception ) {
		if( exception instanceof ChoralCompoundException e ) {
			for( ChoralException cause : e.getCauses() ) {
				if( cause instanceof AstPositionedException ape ) {
					Diagnostic diagnostic = errorDiagnostic( ape.getMessage() );
					setRange( diagnostic, ape.position() );
					diagnostics.add( diagnostic );
				} else {
					Diagnostic diagnostic = errorDiagnostic( e.getMessage() );
					diagnostics.add( diagnostic );
				}
			}
		} else if( exception instanceof AstPositionedException e ) {
			Diagnostic diagnostic = errorDiagnostic( e.getMessage() );
			setRange( diagnostic, e.position() );
			diagnostics.add( diagnostic );
		} else {
			Diagnostic diagnostic = errorDiagnostic(
					"Internal compiler error: " + exception.getMessage() );
			diagnostics.add( diagnostic );
		}
	}

	public enum AnalysisFailure {
		PARSE_ERROR,
		TYPE_ERROR
	}

	public record AnalysisResult(
			CompilationUnit compilationUnit,
			List< Diagnostic > diagnostics,
			AnalysisFailure failure,
			String failureMessage
	) {
		private static AnalysisResult success(
				CompilationUnit compilationUnit, List< Diagnostic > diagnostics
		) {
			return new AnalysisResult(
					compilationUnit, List.copyOf( diagnostics ), null, null );
		}

		private static AnalysisResult failure(
				List< Diagnostic > diagnostics, AnalysisFailure failure, String failureMessage
		) {
			return new AnalysisResult(
					null, List.copyOf( diagnostics ), failure, failureMessage );
		}

		public boolean successful() {
			return failure == null;
		}
	}

	private static CompilationUnit typeCheck(
			String uri, List< CompilationUnit > sourceUnits,
			BiConsumer< choral.ast.Position, String > infoChannel
	) throws Exception {
		List< CompilationUnit > headerUnits = loadHeaders( uri, sourceUnits );
		TyperOptions typerOptions = new TyperOptions( VerbosityLevel.WARNINGS )
				.withInfoChannel( infoChannel );
		Typer.annotate( sourceUnits, headerUnits, typerOptions );
		return sourceUnits.get( 0 );
	}

	private static List< CompilationUnit > sourceUnits(
			String uri, CompilationUnit activeUnit
	) throws Exception {
		Path activePath = sourcePath( uri );
		if( activePath == null ) return List.of( activeUnit );
		Path sourceRoot = sourceRoot( activePath, activeUnit );
		Map< Path, CompilationUnit > units = new LinkedHashMap<>();
		ArrayDeque< SourceUnit > pending = new ArrayDeque<>();
		Path normalisedActivePath = activePath.toAbsolutePath().normalize();
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
					if( Files.isDirectory( packagePath ) ) {
						try( Stream< Path > files = Files.list( packagePath ) ) {
							for( Path file : files.filter( DiagnosticsProvider::isChoralSource )
									.sorted().toList() )
								addSource( file, packageName, true, units, pending );
						}
					}
				} else {
					String simpleName = name.substring( name.lastIndexOf( '.' ) + 1 );
					addSource( source.path().getParent().resolve( simpleName + ".ch" ),
							name, false, units, pending );
					addSource( sourceRoot.resolve(
							name.replace( '.', java.io.File.separatorChar ) + ".ch" ),
							name, false, units, pending );
				}
			}
		}
		return List.copyOf( units.values() );
	}

	private static void addSource(
			Path path, String importedName, boolean onDemand,
			Map< Path, CompilationUnit > units, ArrayDeque< SourceUnit > pending
	) throws Exception {
		Path normalised = path.toAbsolutePath().normalize();
		if( units.containsKey( normalised ) || !Files.isRegularFile( normalised ) ) return;
		CompilationUnit unit = Parser.parseSourceFile( normalised.toFile() );
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
		Path parent = activePath.toAbsolutePath().normalize().getParent();
		if( parent == null ) return activePath.toAbsolutePath().normalize();
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

	private static boolean isChoralSource( Path path ) {
		return Files.isRegularFile( path ) && path.toString().toLowerCase().endsWith( ".ch" );
	}

	private static Path sourcePath( String uri ) {
		if( uri == null || !uri.startsWith( "file:" ) ) return null;
		return Paths.get( URI.create( uri ) );
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
		if( uri == null || !uri.startsWith( "file:" ) ) {
			return standardHeaders.toList();
		}
		Path documentPath = Paths.get( URI.create( uri ) );
		Path parent = documentPath.getParent();
		if( parent == null ) {
			return standardHeaders.toList();
		}
		List< File > sourceFiles = sourceUnits.stream()
				.map( unit -> unit.position().sourceFile() )
				.filter( java.util.Objects::nonNull )
				.map( File::new )
				.toList();
		return Stream.concat( standardHeaders, HeaderLoader.loadFromPath(
				List.of( parent ), sourceFiles, true, true ) )
				.toList();
	}

	private static Diagnostic warningDiagnostic( String message ) {
		Diagnostic diagnostic = new Diagnostic();
		diagnostic.setSeverity( DiagnosticSeverity.Warning );
		diagnostic.setMessage( message );
		diagnostic.setSource( "choral-compiler" );
		return diagnostic;
	}

	private static Diagnostic errorDiagnostic( String message ) {
		Diagnostic diagnostic = new Diagnostic();
		diagnostic.setRange( new Range( new Position( 0, 0 ), new Position( 0, 0 ) ) );
		diagnostic.setSeverity( DiagnosticSeverity.Error );
		diagnostic.setMessage( message );
		diagnostic.setSource( "choral-compiler" );
		return diagnostic;
	}

	private static void setRange( Diagnostic diagnostic, choral.ast.Position position ) {
		if( position == null ) {
			diagnostic.setRange( new Range( new Position( 0, 0 ), new Position( 0, 0 ) ) );
			return;
		}
		// position.line() -1 to account for diff between 0-indexing and 1-indexing
		Range range = new Range( new Position( position.line() - 1, position.column() ),
				new Position( position.line() - 1, position.column() ) );
		diagnostic.setRange( range );
	}
}
