package choral.compiler;

import choral.utils.VerbosityLevel;

/**
 * Options for configuring {@link choral.compiler.Typer}.
 *
 * @param verbosity The verbosity level to use when debugging.
 * @param suppressLiftWarnings Whether to suppress warnings printed when a Java type cannot be
 *                             lifted by {@link choral.compiler.typer.ClassLifter}.
 * @param relaxed Whether the typer should run in "relaxed mode", allowing programs where the roles
 *                don't match like {@code int@A x = 1@A; int@B y = x + 1@B}. Used for communication
 *                inference.
 */
public record TyperOptions (
                VerbosityLevel verbosity,
                boolean suppressLiftWarnings,
                boolean relaxed
) {

	/**
	 * @return A copy of TyperOptions with {@link #relaxed} mode enabled.
	 */
	public TyperOptions relaxedMode() {
		return new TyperOptions( verbosity, suppressLiftWarnings, true );
	}

	/**
	 * @return A copy of TyperOptions with {@link #relaxed} mode disabled.
	 */
	public TyperOptions normalMode() {
		return new TyperOptions( verbosity, suppressLiftWarnings, true );
	}

}
