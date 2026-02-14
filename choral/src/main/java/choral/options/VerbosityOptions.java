package choral.options;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command()
public class VerbosityOptions {

	public enum VerbosityLevel {
		ERRORS( -1 ),
		WARNINGS( 0 ),
		INFO( 1 ),
		DEBUG( 2 );

		final int value;

		VerbosityLevel( int value ) {
			this.value = value;
		}
	}

	private VerbosityOptions.VerbosityLevel verbosity;

	public VerbosityOptions.VerbosityLevel verbosity() {
		return this.verbosity;
	}

	@Option( names = { "--verbosity" },
			description = "Verbosity level: ${COMPLETION-CANDIDATES}.",
			defaultValue = "WARNINGS",
			paramLabel = "<LEVEL>"
	)
	private void setVerbosity( VerbosityOptions.VerbosityLevel value ) {
		this.verbosity = value;
	}

	@Option( names = { "-v", "--verbose" },
			description = "Enable information messages." )
	private void setVerboseLevel( boolean value ) {
		if( value ) {
			this.setVerbosity( VerbosityOptions.VerbosityLevel.INFO );
		}
	}

	@Option( names = { "-q", "--quiet" },
			description = "Disable all messages except errors." )
	private void setQuietLevel( boolean value ) {
		if( value ) {
			this.setVerbosity( VerbosityOptions.VerbosityLevel.ERRORS );
		}
	}

	@Option( names = { "--debug" },
			description = "Enable debug messages." )
	private void setDebugLevel( boolean value ) {
		if( value ) {
			this.setVerbosity( VerbosityOptions.VerbosityLevel.DEBUG );
		}
	}
}
