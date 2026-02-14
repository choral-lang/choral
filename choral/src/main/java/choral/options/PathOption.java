package choral.options;

import choral.compiler.Compiler;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

@Command()
public abstract class PathOption {
	private String value;

	private List< Path > paths;

	protected void setValue( String value ) {
		this.value = value;
	}

	public final String value() {
		return value;
	}

	public final List< Path > getPaths() {
		return getPaths( false );
	}

	public final List< Path > getPaths( boolean cwdIfEmpty ) {
		if( paths == null ) {
			paths = new LinkedList<>();
			if( value != null ) {
				for( String p : value().split( File.pathSeparator ) ) {
					paths.add( Paths.get( p ) );
				}
			}
		}
		if( cwdIfEmpty && paths.isEmpty() ) {
			paths.add( Paths.get( "" ) );
		}
		return paths;
	}

	public final static class SourcePathOption extends PathOption {
		@Option( names = { "-s", "--sources" },
				paramLabel = "<PATH>",
				description = "Specify where to find choral source files (" + choral.compiler.Compiler.SOURCE_FILE_EXTENSION + ")." )
		@Override
		protected void setValue( String value ) {
			super.setValue( value );
		}
	}

	public final static class HeadersPathOption extends PathOption {
		@Option( names = { "-l", "--headers" },
				paramLabel = "<PATH>",
				description = "Specify where to find choral header files (" + Compiler.HEADER_FILE_EXTENSION + ")." )
		@Override
		protected void setValue( String value ) {
			super.setValue( value );
		}
	}
}
