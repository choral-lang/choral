package choral.options;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.Optional;

@Command()
public class EmissionOptions {

	@Option( names = { "--dry-run" },
			description = "Disable any write on disk." )
	private boolean dryRun = false;

	@Option( names = { "--annotate" },
			description = "Annotate the projected artefacts with the @Choreography annotation." )
	private boolean isAnnotated = false;

	@Option( names = { "--infer-comms" },
			description = "Infer missing communications and selections." )
	private boolean inferComms = false;

	@Option( names = { "--overwrite-source" },
			description = "After static analysis, overwrite source files with the compiler's elaborated AST." )
	private boolean canOverwrite = false;

	@Option( names = { "-t", "--target" },
			paramLabel = "<PATHS>",
			description = "Specify where to save compiled files." )
	private Path targetpath;

	public boolean isDryRun() {
		return dryRun;
	}

	public boolean isAnnotated() {
		return isAnnotated;
	}

	public boolean inferComms() {
		return inferComms;
	}

	public boolean canOverwriteSourceCode() {
		return canOverwrite;
	}

	public Optional< Path > targetpath() {
		return Optional.ofNullable( targetpath );
	}
}
