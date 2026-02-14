package choral.options;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.Optional;

@Command()
public class HeaderCompilerOptions {

	@Option( names = { "--no-overwrite" },
			description = "Never overwrites existing files." )
	private boolean overwrite = true;

	@Option( names = { "--dry-run" },
			description = "Disable any write on disk." )
	private boolean dryRun = false;

	@Option( names = { "-p", "--canonical-paths" },
			description = "Use folders for packages." )
	boolean useCanonicalPaths;

	@Option( names = { "-t", "--target" },
			paramLabel = "<PATHS>",
			description = "Specify where to save compiled files." )
	private Path targetpath;

	public boolean isDryRun() {
		return dryRun;
	}

	public boolean isOverwritingAllowed() {
		return overwrite;
	}

	public boolean useCanonicalPaths() {
		return useCanonicalPaths;
	}

	public Optional< Path > targetpath() {
		return Optional.ofNullable( targetpath );
	}
}
