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

	/** Add the error to diagnostics and throw it. */
	public < T > T abort( AstPositionedException error ) {
    	diagnostics.add( error );
		throw new ChoralCompoundException( List.copyOf( diagnostics ) );
	}

	/** Throws the recorded errors, preserving the single-error behaviour. */
	public void abortIfErrors() {
		if( !diagnostics.isEmpty() ) {
			throw new ChoralCompoundException( List.copyOf( diagnostics ) );
		}
	}
}
