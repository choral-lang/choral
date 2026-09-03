package choral.compiler;

import java.util.ArrayList;
import java.util.List;

import choral.exceptions.AstPositionedException;
import choral.exceptions.ChoralCompoundException;

/** Collects diagnostics until you ask it to abort. */
public final class ErrorReporter {
	private final List< AstPositionedException > diagnostics = new ArrayList<>();

	/** Records a diagnostic and allows the current check to continue. */
	public void report( AstPositionedException error ) {
		diagnostics.add( error );
	}

	/** Add the error to diagnostics and abort the current check. */
	public < T > T abort( AstPositionedException error ) {
		diagnostics.add( error );
		throw diagnosticsException();
	}

	/** Throws any recorded diagnostics. */
	public void abortIfErrors() {
		if( !diagnostics.isEmpty() ) {
			throw diagnosticsException();
		}
	}

	private ChoralCompoundException diagnosticsException() {
		return new ChoralCompoundException( List.copyOf( diagnostics ) );
	}
}
