package choral.options;

/**
 * Options for configuring {@link choral.compiler.Typer}.
 *
 * @param verbosity The verbosity level to use when debugging.
 * @param relaxed Whether the typer should run in "relaxed mode", allowing programs where the roles
 *                don't match like {@code int@A x = 1@A; int@B y = x + 1@B}. Used for communication
 *                inference.
 */
public record TyperOptions (
		VerbosityOptions.VerbosityLevel verbosity,
		boolean relaxed
) {

	/**
	 * Construct a TyperOptions object in the default ("non-relaxed") mode.
	 */
	public TyperOptions(
		VerbosityOptions.VerbosityLevel verbosity
	) {
		this( verbosity, false );
	}

	/**
	 * @return A copy of TyperOptions with {@link #relaxed} mode enabled.
	 */
	public TyperOptions relaxedMode() {
		return new TyperOptions( verbosity, true );
	}

	/**
	 * @return A copy of TyperOptions with {@link #relaxed} mode disabled.
	 */
	public TyperOptions normalMode() {
		return new TyperOptions( verbosity, true );
	}

}
