/*
 * Copyright (C) 2019 by Saverio Giallorenzo <saverio.giallorenzo@gmail.com>
 * Copyright (C) 2019 by Fabrizio Montesi <famontesi@gmail.com>
 * Copyright (C) 2019 by Marco Peressotti <marco.peressotti@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package choral;

import choral.ast.CompilationUnit;
import choral.ast.Position;
import choral.compiler.Compiler;
import choral.compiler.*;
import choral.exceptions.AstPositionedException;
import choral.exceptions.ChoralCompoundException;
import choral.exceptions.ChoralException;
import picocli.AutoComplete;
import picocli.CommandLine;
import picocli.CommandLine.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static choral.utils.Streams.*;

@Command(
		name = "choral",
		description = "A compiler for the Choral programming language.%nhttps://choral-lang.org/",
		subcommands = {
				Choral.Checker.class,
				Choral.Projector.class,
				Choral.HeaderGenerator.class,
				Choral.Amend.class,
				AutoComplete.GenerateCompletion.class
		}
)
public class Choral extends ChoralCommand implements Callable< Integer > {

	public static void main( String[] args ) {
		CommandLine cl = new CommandLine( new Choral() );
		cl.setToggleBooleanFlags( true );
		cl.setCaseInsensitiveEnumValuesAllowed( true );
		System.exit( cl.execute( args ) );
	}

	@Override
	public Integer call() throws Exception {
		new CommandLine( this ).usage( System.err );
		return 1;
	}

	@Command( name = "check", aliases = { "c" },
			description = "Check correctness and projectability."
	)
	static class Checker extends ChoralCommand implements Callable< Integer > {

		@Mixin
		PathOption.HeadersPathOption headersPathOption;

		@Option( names = { "--strict-header-search" },
				description = "ignore headers in the same folder of the source files, unless specified by -l/--headers." )
		boolean strictHeaderSearch = false;

		@Option( names = { "--no-projectability" },
				description = "skip projectability checks." )
		boolean skipProjectability = false;

		@Parameters( arity = "1..*" )
		Collection< File > sourceFiles;

		@Override
		public Integer call() {
			try {
				Collection< CompilationUnit > sourceUnits = sourceFiles.stream().map(
						wrapFunction( Parser::parseSourceFile ) ).collect( Collectors.toList() );
				Collection< CompilationUnit > headerUnits = Stream.concat(
								HeaderLoader.loadStandardProfile(),
								HeaderLoader.loadFromPath(
										headersPathOption.getPaths(),
										sourceFiles,
										true, strictHeaderSearch )
						)
						.collect( Collectors.toList() );
				Collection< CompilationUnit > annotatedUnits = Typer.annotate( sourceUnits,
						headerUnits );
				if( !skipProjectability ) {
					Compiler.checkProjectiability( annotatedUnits );
				}
			} catch( Exception e ) {
				printNiceErrorMessage( e, verbosityOptions.verbosity() );
				System.out.println( "compilation failed." );
				return 1;
			}
			return 0;
		}
	}

	@Command( name = "endpoint-projection", aliases = { "epp" },
			description = "Generate local code by projecting a choreography at a set of roles."
	)
	static class Projector extends ChoralCommand implements Callable< Integer > {

		@Mixin
		EmissionOptions emissionOptions;

		@Mixin
		PathOption.HeadersPathOption headersPathOption;

		@Mixin
		PathOption.SourcePathOption sourcesPathOption;

		@Parameters( index = "0", arity = "1" )
		String symbol;

		@Parameters( index = "1..*", arity = "0..*" )
		List< String > worlds;

		@Override
		public Integer call() {
			try {
				Collection< File > sourceFiles = sourcesPathOption.getPaths( true ).stream()
						.flatMap( wrapFunction( p -> Files.find( p, 999, ( q, a ) -> {
							if( Files.isDirectory( q ) ) return false;
							String x = q.toString();
							x = x.substring(
									x.length() - SourceObject.ChoralSourceObject.FILE_EXTENSION.length() ).toLowerCase();
							return x.equals( SourceObject.ChoralSourceObject.FILE_EXTENSION );
						}, FileVisitOption.FOLLOW_LINKS ) ) )
						.map( Path::toFile )
						.collect( Collectors.toList() );
				Collection< CompilationUnit > sourceUnits = sourceFiles.stream().map(
						wrapFunction( Parser::parseSourceFile ) ).collect( Collectors.toList() );
				Collection< CompilationUnit > headerUnits = Stream.concat(
								HeaderLoader.loadStandardProfile(),
								HeaderLoader.loadFromPath(
										headersPathOption.getPaths(),
										sourceFiles,
										true, true ) // TODO: keep this or introduce parameter also in EPP?
						)
						.collect( Collectors.toList() );
				AtomicReference< Collection< CompilationUnit > > annotatedUnits = new AtomicReference<>();
				profilerLog( "typechecking", () -> annotatedUnits.set( Typer.annotate( sourceUnits,
						headerUnits ) ) );

				profilerLog( "projectability check",
						() -> Compiler.checkProjectiability( annotatedUnits.get() ) );

				// TODO: ... UNTIL HERE (annotatedUnits)
				if( worlds == null ) {
					worlds = Collections.emptyList();
				}
				profilerLog( "compilation", () ->
						{
							try {
								Compiler.project(
										emissionOptions.isDryRun(),
										emissionOptions.isAnnotated(),
										//						emissionOptions.useCanonicalPaths() TODO: implement this
										//						emissionOptions.isOverwritingAllowed() TODO: implement this
										annotatedUnits.get(),
										symbol,
										worlds.stream().map( String::trim ).filter(
												w -> !( w.isBlank() || w.isEmpty() ) ).collect(
												Collectors.toList() ),
										emissionOptions.targetpath()
								);
							} catch( IOException e ) {
								throw new RuntimeException( e );
							}
						}
				);
			} catch( Exception e ) {
				printNiceErrorMessage( e, verbosityOptions.verbosity() );
				System.out.println( "compilation failed." );
				return 1;
			}
			return 0;
		}
	}

	@Command( name = "headers", aliases = { "chh" },
			description = "Generate choral header files (" + Compiler.HEADER_FILE_EXTENSION + ")."
	)
	static class HeaderGenerator extends ChoralCommand implements Callable< Integer > {

		@Mixin
		EmissionOptions emissionOptions;

		@Mixin
		PathOption.HeadersPathOption headersPathOption;

		@Option( names = { "--strict-header-search" },
				description = "ignore headers in the same folder of the source files, unless specified by -l/--headers." )
		boolean strictHeaderSearch = false;

		@Parameters( arity = "1..*" )
		List< File > sourceFiles;

		@Override
		public Integer call() {
			try {
				Collection< CompilationUnit > sourceUnits = sourceFiles.stream().map(
						wrapFunction( Parser::parseSourceFile ) ).collect( Collectors.toList() );
				Collection< CompilationUnit > headerUnits = Stream.concat(
								HeaderLoader.loadStandardProfile(),
								HeaderLoader.loadFromPath(
										headersPathOption.getPaths(),
										sourceFiles,
										true, strictHeaderSearch )
						)
						.collect( Collectors.toList() );
				Collection< CompilationUnit > annotatedUnits = Typer.annotate( sourceUnits,
						headerUnits );
				annotatedUnits.parallelStream().map( HeaderCompiler::compile )
						.forEach( emissionOptions.isDryRun()
								? skip()
								: wrapConsumer( s -> SourceWriter.writeSource( s, emissionOptions.targetpath(),
										emissionOptions.useCanonicalPaths(),
										emissionOptions.isOverwritingAllowed() ) ) );
			} catch( Exception e ) {
				printNiceErrorMessage( e, verbosityOptions.verbosity() );
				System.out.println( "compilation failed." );
				return 1;
			}
			return 0;
		}
	}

	@Command( name = "amend", 
			description = "dummy command." 
	)
	static class Amend extends ChoralCommand implements Callable< Integer >{
		public Integer call(){
			System.out.println( "dummy command" );
			return 0;
		}
	}

	private static void printNiceErrorMessage(
			Throwable e, VerbosityOptions.VerbosityLevel verbosity
	) {
		if( e instanceof AstPositionedException ) {
			AstPositionedException pe = (AstPositionedException) e;
			if (pe.position() == null) {
				// TODO: position should be defined!
				System.out.println( "error: " + capitalizeFirst( e.getMessage() ) + "." );
				if( verbosity == VerbosityOptions.VerbosityLevel.DEBUG ) {
					e.printStackTrace();
				}
			} else {
				printNiceErrorMessage( (AstPositionedException) e, verbosity );
			}
		} else if( e instanceof ChoralCompoundException ) {
			for( ChoralException c : ( (ChoralCompoundException) e ).getCauses() ) {
				printNiceErrorMessage( c, verbosity );
			}
		} else if( e instanceof WrappedException ) {
			printNiceErrorMessage( e.getCause(), verbosity );
		} else if( e instanceof IOException ) {
			System.out.println( "error: " + capitalizeFirst( e.getMessage() ) + "." );
			if( verbosity == VerbosityOptions.VerbosityLevel.DEBUG ) {
				e.printStackTrace();
			}
		} else {
			System.out.println( "error: " + capitalizeFirst( e.getMessage() ) + "." );
			if( verbosity == VerbosityOptions.VerbosityLevel.DEBUG ) {
				e.printStackTrace();
			}
		}
	}

	private static void printNiceErrorMessage(
			AstPositionedException e, VerbosityOptions.VerbosityLevel verbosity
	) {
		// -- parameters ---------------
		int tabSize = 2;       // size of soft tabs
		int contextLines = 1;  // number lines to display before and after the error line
		// -----------------------------
		StringBuilder formattedSnippet = new StringBuilder();
		Position p = e.position();
		try( Stream< String > allLines = Files.lines( Paths.get( p.sourceFile() ) ) ) {
			int lineDigits = (int) Math.ceil( Math.log10( p.line() ) ) + 1;
			String format = "  %" + lineDigits + "d | %s\n";
			int errorLine = Math.min( p.line() - 1, contextLines ) + 1;
			int baseLine = p.line() - errorLine;
			List< String > snippetLines = allLines
					.skip( p.line() - errorLine )
					.limit( errorLine + contextLines )
					.collect( Collectors.toList() );
			for( int i = 1; i <= snippetLines.size(); i++ ) {
				String line = snippetLines.get( i - 1 );
				formattedSnippet.append( String.format( format, baseLine + i,
						line.replace( "\t", " ".repeat( tabSize ) ) ) );
				if( i == errorLine ) {
					// get the column with tabs (2 spaces)
					int length = p.column();
					for( char c : line.substring( 0, p.column() ).toCharArray() ) {
						if( "\t".equals( "" + c ) ) {
							length += tabSize - 1;
						}
					}
					formattedSnippet.append( " ".repeat( lineDigits + 2 ) ).append( " | " ).append(
							"-".repeat(
									length ) ).append( "^\n" );
				}
			}
		} catch( IOException ex ) {
			// give up printing the code snippet
		}
		System.out.printf(
				"%1$s:%2$d:%3$d: error: %4$s.\n\n%5$s\n",
				relativizePath( p.sourceFile() ),
				p.line(),
				p.column(),
				capitalizeFirst( e.getInnerMessage() ),
				formattedSnippet
		);
		if( verbosity == VerbosityOptions.VerbosityLevel.DEBUG ) {
			e.printStackTrace();
		}
	}

	public static String relativizePath( String path ) {
		return Paths.get( "." ).toAbsolutePath().relativize(
				Paths.get( path ).toAbsolutePath() ).toString();
	}

	static String capitalizeFirst( String str ) {
		if( str == null || str.isEmpty() ) {
			return str;
		}

		return str.substring( 0, 1 ).toUpperCase() + str.substring( 1 );
	}

	// - - - - - - PROFILING VARIABLES, METHODS, AND UTILITIES

	private static Map< String, ArrayList< Long > > profilingLog;

	public static void mainProfiler(
			String[] args, Map< String, ArrayList< Long > > profilingLog
	) {
		Choral c = new Choral();
		c.profilingLog = profilingLog;
		CommandLine cl = new CommandLine( c );
		cl.setToggleBooleanFlags( true );
		cl.setCaseInsensitiveEnumValuesAllowed( true );
		cl.execute( args );
	}

	protected static void profilerLog( String action, Runnable r ) throws Exception {
		if( profilingLog != null ) {
			Long start = System.nanoTime();
			try {
				r.run();
			} catch( RuntimeException e ) {
				throw new Exception( e );
			}
			Long finish = System.nanoTime();
			profilingLog.putIfAbsent( action, new ArrayList<>() );
			profilingLog.get( action ).add( finish - start );
		} else {
			r.run();
		}
	}

}

@Command(
		versionProvider = ChoralVersionProvider.class,
		sortOptions = false,
		descriptionHeading = "%nDescription:%n",
		parameterListHeading = "%nParameters:%n",
		optionListHeading = "%nOptions:%n",
		commandListHeading = "%nCommands:%n",
		mixinStandardHelpOptions = true
)
abstract class ChoralCommand {
	@Mixin
	VerbosityOptions verbosityOptions;
}

@Command()
class VerbosityOptions {

	enum VerbosityLevel {
		ERRORS( -1 ),
		WARNINGS( 0 ),
		INFO( 1 ),
		DEBUG( 2 );

		final int value;

		VerbosityLevel( int value ) {
			this.value = value;
		}
	}

	private VerbosityLevel verbosity;

	protected VerbosityLevel verbosity() {
		return this.verbosity;
	}

	@Option( names = { "--verbosity" },
			description = "Verbosity level: ${COMPLETION-CANDIDATES}.",
			defaultValue = "WARNINGS",
			paramLabel = "<LEVEL>"
	)
	private void setVerbosity( VerbosityLevel value ) {
		this.verbosity = value;
	}

	@Option( names = { "-v", "--verbose" },
			description = "Enable information messages." )
	private void setVerboseLevel( boolean value ) {
		if( value ) {
			this.setVerbosity( VerbosityLevel.INFO );
		}
	}

	@Option( names = { "-q", "--quiet" },
			description = "Disable all messages except errors." )
	private void setQuietLevel( boolean value ) {
		if( value ) {
			this.setVerbosity( VerbosityLevel.ERRORS );
		}
	}

	@Option( names = { "--debug" },
			description = "Enable debug messages." )
	private void setDebugLevel( boolean value ) {
		if( value ) {
			this.setVerbosity( VerbosityLevel.DEBUG );
		}
	}
}

@Command()
abstract class PathOption {
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
				description = "Specify where to find choral source files (" + Compiler.SOURCE_FILE_EXTENSION + ")." )
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

@Command()
class EmissionOptions {

	@Option( names = { "--no-overwrite" },
			description = "Never overwrites existing files." )
	private boolean overwrite = true;

	@Option( names = { "--dry-run" },
			description = "Disable any write on disk." )
	private boolean dryRun = false;

	@Option( names = { "--annotate" },
			description = "Annotate the projected artefacts with the @Choreography annotation." )
	private boolean isAnnotated = false;

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

	public boolean isAnnotated() {
		return isAnnotated;
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

class ChoralVersionProvider implements IVersionProvider {
	public String[] getVersion() throws Exception {
		Properties properties = new Properties();
		InputStream is = getClass().getClassLoader().getResourceAsStream( "version.properties" );
		properties.load( is );
		return new String[] {
				"${COMMAND-FULL-NAME} " + properties.getProperty( "choral.version" )
		};
	}
}
