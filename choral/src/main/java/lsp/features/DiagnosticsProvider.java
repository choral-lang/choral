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

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
			typeCheck( uri, compUnit, ( pos, s ) -> {
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

	public CompilationUnit typeCheck( String uri, CompilationUnit compUnit ) throws Exception {
		return typeCheck( uri, compUnit, ( pos, message ) -> {} );
	}

	private static CompilationUnit typeCheck(
			String uri, CompilationUnit compUnit,
			BiConsumer< choral.ast.Position, String > infoChannel
	) throws Exception {
		List< CompilationUnit > headerUnits = loadHeaders( uri );
		TyperOptions typerOptions = new TyperOptions( VerbosityLevel.WARNINGS )
				.withInfoChannel( infoChannel );
		Typer.annotate( List.of( compUnit ), headerUnits, typerOptions );
		return compUnit;
	}

	private static String sourceFile( String uri ) {
		if( uri == null || !uri.startsWith( "file:" ) ) return null;
		return Paths.get( URI.create( uri ) ).toString();
	}

	private static List< CompilationUnit > loadHeaders( String uri ) throws Exception {
		Stream< CompilationUnit > standardHeaders = HeaderLoader.loadStandardProfile();
		if( uri == null || !uri.startsWith( "file:" ) ) {
			return standardHeaders.toList();
		}
		Path documentPath = Paths.get( URI.create( uri ) );
		Path parent = documentPath.getParent();
		if( parent == null ) {
			return standardHeaders.toList();
		}
		return Stream.concat( standardHeaders, HeaderLoader.loadFromPath( List.of( parent ) ) )
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
