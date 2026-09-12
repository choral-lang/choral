package lsp.features;

import choral.exceptions.AstPositionedException;
import choral.exceptions.ChoralCompoundException;
import choral.exceptions.ChoralException;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.services.LanguageClient;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DiagnosticsProvider {
	private final TypedSourceAnalyzer analyzer = new TypedSourceAnalyzer();
	private LanguageClient client;

	public void setClient( LanguageClient client ) {
		this.client = client;
	}

	public List< Diagnostic > analyze( String uri, String content ) {
		return diagnostics( uri, analyzer.analyze( uri, content ) );
	}

	public List< Diagnostic > diagnostics(
			String uri, TypedSourceAnalyzer.AnalysisResult analysis
	) {
		List< Diagnostic > diagnostics = new ArrayList<>();
		for( TypedSourceAnalyzer.AnalysisWarning warning : analysis.warnings() ) {
			choral.ast.Position position = warning.position();
			if( !belongsTo( uri, position ) ) continue;
			if( position == null ) position = new choral.ast.Position( null, 1, 1 );
			Diagnostic diagnostic = warningDiagnostic( warning.message() );
			setRange( diagnostic, position );
			diagnostics.add( diagnostic );
		}
		if( analysis.exception() != null )
			addErrorDiagnostics( uri, diagnostics, analysis.exception() );
		return List.copyOf( diagnostics );
	}

	private static void addErrorDiagnostics(
			String uri, List< Diagnostic > diagnostics, Exception exception
	) {
		if( exception instanceof ChoralCompoundException e ) {
			for( ChoralException cause : e.getCauses() ) {
				if( cause instanceof AstPositionedException ape ) {
					if( !belongsTo( uri, ape.position() ) ) continue;
					Diagnostic diagnostic = errorDiagnostic( ape.getMessage() );
					setRange( diagnostic, ape.position() );
					diagnostics.add( diagnostic );
				} else {
					Diagnostic diagnostic = errorDiagnostic( e.getMessage() );
					diagnostics.add( diagnostic );
				}
			}
		} else if( exception instanceof AstPositionedException e ) {
			if( !belongsTo( uri, e.position() ) ) return;
			Diagnostic diagnostic = errorDiagnostic( e.getMessage() );
			setRange( diagnostic, e.position() );
			diagnostics.add( diagnostic );
		} else {
			Diagnostic diagnostic = errorDiagnostic(
					"Internal compiler error: " + exception.getMessage() );
			diagnostics.add( diagnostic );
		}
	}

	private static boolean belongsTo( String uri, choral.ast.Position position ) {
		Path documentPath = sourcePath( uri );
		if( documentPath == null || position == null || position.sourceFile() == null )
			return true;
		return documentPath.toAbsolutePath().normalize().equals(
				Path.of( position.sourceFile() ).toAbsolutePath().normalize() );
	}

	private static Path sourcePath( String uri ) {
		if( uri == null || !uri.startsWith( "file:" ) ) return null;
		return Path.of( URI.create( uri ) );
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
