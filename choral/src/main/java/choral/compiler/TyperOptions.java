package choral.compiler;

import choral.utils.VerbosityLevel;

import java.util.function.Consumer;

/**
 * Options for configuring {@link choral.compiler.Typer}.
 */
public class TyperOptions {
	private final VerbosityLevel verbosity;
	private final boolean relaxed;
	private final Consumer< String > warningChannel;

	public TyperOptions( VerbosityLevel verbosity, Consumer< String > warningChannel ) {
		// Default to "normal" mode, where communications are written manually
		this( verbosity, false, warningChannel );
	}

	private TyperOptions( VerbosityLevel verbosity, boolean relaxed, Consumer< String > warningChannel ) {
		this.verbosity = verbosity;
		this.relaxed = relaxed;
		this.warningChannel = warningChannel;
	}

	/** The verbosity level to use when debugging. */
	public VerbosityLevel verbosity() { return verbosity; }

	/** Whether the typer should run in "relaxed mode", allowing programs where the roles
	 * don't match like {@code int@A x = 1@A; int@B y = x + 1@B}. Used for communication
	 * inference. */
	public boolean relaxed() { return relaxed; }

	/** The channel where warning messages should be published. */
	public Consumer< String > warningChannel() { return warningChannel; }

	/**
	 * @return A copy of TyperOptions with {@link #relaxed} mode enabled.
	 */
	public TyperOptions relaxedMode() {
		return new TyperOptions( verbosity, true, warningChannel );
	}

	/**
	 * @return A copy of TyperOptions with {@link #relaxed} mode disabled.
	 */
	public TyperOptions normalMode() {
		return new TyperOptions( verbosity, false, warningChannel );
	}

}
