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

import java.util.ArrayList;
import java.util.List;

public class DiagnosticsProvider {
	private LanguageClient client;

	public void setClient( LanguageClient client ) {
		this.client = client;
	}

	public List< Diagnostic > analyze( String uri, String content ) {
		List< Diagnostic > diagnostics = new ArrayList<>();

		try { // choral compiler errors are reported through exceptions
			// so try to parse and type program, then catch any exceptions
			// to pass along error messages to language client
			CompilationUnit compUnit = Parser.parseString( content );

			List< CompilationUnit > headerUnits = HeaderLoader.loadStandardProfile().toList();

			TyperOptions typerOptions = new TyperOptions( VerbosityLevel.WARNINGS )
					.withInfoChannel( ( pos, s ) -> {
						if( pos == null ) pos = new choral.ast.Position( null, 1, 1 );
						Diagnostic diagnostic = warningDiagnostic( s );
						setRange( diagnostic, pos );
						diagnostics.add( diagnostic );
					} );
			Typer.annotate( List.of( compUnit ), headerUnits, typerOptions );

		} catch( ChoralCompoundException e ) {
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
		} catch( AstPositionedException e ) {
			Diagnostic diagnostic = errorDiagnostic( e.getMessage() );
			setRange( diagnostic, e.position() );
			diagnostics.add( diagnostic );

		} catch( Exception e ) {
			Diagnostic diagnostic = errorDiagnostic( "Internal compiler error: " + e.getMessage() );
			diagnostics.add( diagnostic );
		}

		return diagnostics;
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
		diagnostic.setSeverity( DiagnosticSeverity.Error );
		diagnostic.setMessage( message );
		diagnostic.setSource( "choral-compiler" );
		return diagnostic;
	}

	private static void setRange( Diagnostic diagnostic, choral.ast.Position position ) {
		// position.line() -1 to account for diff between 0-indexing and 1-indexing
		Range range = new Range( new Position( position.line() - 1, position.column() ),
				new Position( position.line() - 1, position.column() ) );
		diagnostic.setRange( range );
	}
}
