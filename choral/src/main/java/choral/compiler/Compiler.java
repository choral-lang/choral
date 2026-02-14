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

package choral.compiler;

import choral.ast.CompilationUnit;
import choral.ast.ImportDeclaration;
import choral.ast.Name;
import choral.ast.Node;
import choral.ast.body.Annotation;
import choral.ast.body.Class;
import choral.ast.body.Enum;
import choral.ast.body.Interface;
import choral.ast.expression.LiteralExpression;
import choral.ast.type.WorldArgument;
import choral.compiler.Logger.Level;
import choral.compiler.courtesyMethodSynthesiser.CourtesyMethodsSynthesiser;
import choral.compiler.soloist.DependencyVisitor;
import choral.compiler.soloist.ImportProjector;
import choral.compiler.soloist.ProjectableTemplate;
import choral.compiler.soloist.SoloistProjector;
import choral.compiler.unitNormaliser.UnitRepresentation;
import choral.exceptions.ChoralException;
import choral.grammar.ChoralLexer;
import choral.grammar.ChoralParser;
import choral.utils.VerbosityLevel;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.*;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Compiler {

	public final String NAME = "Choral Compiler";
	public final String USAGE = "choral [<options>] <templateName> [<world>[,<world>]*]";
	public final String VERSION = "0.0";
	public static final String SOURCE_FILE_EXTENSION = ".ch";
	public static final String HEADER_FILE_EXTENSION = ".chh";
	private final boolean USE_TEST_FILES = false;
	//	private static final boolean USE_TEST_FILES = true;
	private final Logger LOGGER = new Logger( Level.ERROR, Level.WARNING );

	// flags
	private boolean isDryRun = false;
	private boolean debugging = false;
	private boolean projection = true;
	private boolean annotate = false;

	private Compiler() {
		INSTANCE = this;
	}

	// working variables
	private String targetSymbol;
	private final List< String > targetWorlds = new ArrayList<>();
	private Path targetFolder = Path.of( System.getProperty( "user.dir" ) );
	private String destinationFolder = System.getProperty( "user.dir" ) + File.separator + "dist";
	private final List< String > classpath = new ArrayList<>();
	private final List< CompilationUnit > compilationUnits = new ArrayList<>();
	private final List< String > sourceFiles = new ArrayList<>();

	private final List< String > targetSourceFiles = new ArrayList<>();
	private final List< CompilationUnit > targetCompilationUnits = new ArrayList<>();

	private List< CompilationUnit > compilableCUs = new ArrayList<>();
	private static Compiler INSTANCE;

	// = = = = = = = = = = = MAIN SEQUENCE = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = //

	public static void main( String[] args ) throws Exception {
		new Compiler().run( args );
	}

	public static Compiler instance() {
		return INSTANCE;
	}

	private void run( String[] args ) throws Exception {
		readCommands( args );
		loadSourceFiles();
		parseSourceFiles();
//		loadHeadersAndTypeChecking();
		type();
		project();
//		generateHeaderFiles();
		decoupleUnits();
//		generateHeaderFiles();
		generateJavaFiles();
	}

	public static void checkProjectiability(
			Collection< CompilationUnit > annotatedCompilationUnits
	) {
		Compiler instance = new Compiler();
		instance.targetCompilationUnits.addAll( annotatedCompilationUnits );
		instance.projection = true;

		for( CompilationUnit targetCompilationUnit : annotatedCompilationUnits ) {
			List< String > targets = targetCompilationUnit.classes().stream().map(
					e -> e.name().identifier() ).collect( Collectors.toList() );
			targets.addAll( targetCompilationUnit.interfaces().stream().map(
					e -> e.name().identifier() ).collect( Collectors.toList() ) );
			targets.addAll( targetCompilationUnit.enums().stream().map(
					e -> e.name().identifier() ).collect( Collectors.toList() ) );
			for( String target : targets ) {
				instance.targetSymbol = target;
				instance.project();
				instance.decoupleUnits();
			}
		}
	}

	public static void project(
			Boolean isDry,
			Boolean isAnnotated,
			Collection< CompilationUnit > annotatedCompilationUnits,
			String targetSymbol,
			List< String > targetWorlds,
			Optional< Path > destinationFolder
	) throws IOException {
		Compiler instance = new Compiler();
		instance.targetCompilationUnits.addAll( annotatedCompilationUnits );
		instance.projection = true;
		instance.isDryRun = isDry;
		instance.annotate = isAnnotated;
		instance.targetSymbol = targetSymbol;
		instance.targetWorlds.addAll( targetWorlds );
		destinationFolder.ifPresent(
				dF -> instance.destinationFolder = dF.toAbsolutePath().toString() );
		instance.project();
		instance.decoupleUnits();
		instance.generateJavaFiles();
	}

	// = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = //

	public void readCommands( String[] args ) {
		/*- cli config -----------------------------------------------------------------------------------------------*/
		Options options = new Options();
		Option help = new Option( "h", "help", false, "Print this message and exit" );
		options.addOption( help );
		Option version = new Option( "version", "Print the version information and exit" );
		options.addOption( version );
		Option quiet = new Option( "quiet", "Print nothing" );
		options.addOption( quiet );
		Option verbose = new Option( "verbose",
				"Output messages about what the compiler is doing" );
		options.addOption( verbose );
		Option debug = new Option( "debug", "Print debugging information" );
		options.addOption( debug );
		Option dryRun = new Option( "dry", "Run without writing on disk" );
		options.addOption( dryRun );
		Option noepp = new Option( "noepp", "Skip EndPoint Projection" );
		options.addOption( noepp );
		Option annotateOption = new Option( "annotate",
				"Annotates the compiled classes with metadata" );
		options.addOption( annotateOption );
		Option destinationOption = new Option( "d", "destination", true,
				"The destination folder of the compilation" );
		options.addOption( destinationOption );
		Option classpathOption = new Option( "cp", "classpath", true,
				"A semicolon-separated list of paths containing files defining Choral templates" );
		options.addOption( classpathOption );
		Option targetFolderOption = new Option( "t", "targetPath", true,
				"The path to the source file containing the compilation target" );
		options.addOption( targetFolderOption );
		// Option optimistic = new Option("optimistic", "Try to recover from errors and give up only on files with unrecoverable errors");
		// options.addOption(noepp);
		// Option soloist = new Option("soloist", "Generate EndPoint intermediate code");
		// options.addOption(soloist);
		//- parsing cli args -----------------------------------------------------------------------------------------*/
		CommandLineParser cmdParser = new DefaultParser();
		HelpFormatter helpFormatter = new HelpFormatter();
		CommandLine cmd;
		try {
			cmd = cmdParser.parse( options, args );
			if( cmd.hasOption( help.getOpt() ) ) {
				helpFormatter.printHelp( USAGE, options );
				System.exit( 0 );
			}
			if( cmd.hasOption( version.getOpt() ) ) {
				String v = NAME + " version ";
				String format = String.format( "%%%ds(%%s)\n", v.length() );
				System.out.printf( format, "", "\\ /" );
				System.out.printf( format, v, VERSION );
				System.out.printf( format, "", "> <" );
				System.exit( 0 );
			}
			if( cmd.hasOption( debug.getOpt() ) ) {
				LOGGER.filterLevels.add( Level.DEBUG );
				debugging = true;
			}
			if( cmd.hasOption( verbose.getOpt() ) ) {
				LOGGER.filterLevels.add( Level.INFO );
			}
			if( cmd.hasOption( quiet.getOpt() ) ) {
				// preempts debug and verbose
				LOGGER.filterLevels.clear();
			}
			if( cmd.hasOption( dryRun.getOpt() ) ) {
				log( Level.INFO, "this is a dry run." );
				isDryRun = true;
			}
			if( cmd.hasOption( noepp.getOpt() ) ) {
				projection = false;
			}
			if( cmd.hasOption( annotateOption.getOpt() ) ) {
				annotate = true;
			}
			if( cmd.hasOption( destinationOption.getOpt() ) ) {
				destinationFolder = cmd.getOptionValue( destinationOption.getOpt() );
			}
			if( cmd.hasOption( classpathOption.getOpt() ) ) {
				classpath.addAll( Arrays.asList(
						cmd.getOptionValue( classpathOption.getOpt() ).split( ";" ) ) );
			}
			if( cmd.hasOption( targetFolderOption.getOpt() ) ) {
				targetFolder = Path.of( cmd.getOptionValue( targetFolderOption.getOpt() ) );
				if( Files.exists( targetFolder ) ) {
					if( Files.isRegularFile(
							targetFolder ) ) { // if the user passed a file, we just get the enclosing folder
						targetFolder = targetFolder.getParent();
					}
				} else {
					System.err.println( "Error: target path " + targetFolder + " does not exist." );
					System.exit( 1 );
				}
			}
			if( cmd.getArgs().length < 2 && !USE_TEST_FILES ) {
				helpFormatter.printHelp( USAGE, options );
				System.exit( 0 );
			}
			targetSymbol = cmd.getArgs()[ 0 ];
			for( int i = 1; i < cmd.getArgs().length; i++ ) {
				if( cmd.getArgs()[ i ].trim().length() > 0 ) {
					targetWorlds.add( cmd.getArgs()[ i ] );
				}
			}

		} catch( ParseException e ) {
			System.err.println( e.getMessage() );
			helpFormatter.printHelp( USAGE, options );
			System.exit( 1 );
		}
	}

	private void loadSourceFiles() throws IOException {
		// we load the sources within the classpath	...
		sourceFiles.addAll( getChoralSourceFiles( classpath ) );
		// we load the sources within the targetFolder ..
		targetSourceFiles.addAll(
				getChoralSourceFiles( targetFolder.toAbsolutePath().toString() ) );
		// and we add them to the classpath sources (omitting duplicates)
		targetSourceFiles.stream().filter( f -> !sourceFiles.contains( f ) ).forEach(
				sourceFiles::add );
		// remove the corresponding headers
		List< String > headerFiles = sourceFiles.stream()
				.map( ( f ) -> f.substring( 0,
						f.length() - SOURCE_FILE_EXTENSION.length() ) + HEADER_FILE_EXTENSION )
				.collect( Collectors.toList() );
		for( String headerFile : headerFiles ) {
			if( isDryRun ) {
				if( Files.exists( Paths.get( headerFile ) ) ) {
					logf( Level.DEBUG, "deletion of header file '%s' skipped (dry-run).",
							headerFile );
				}
			} else {
				if( Files.deleteIfExists( Paths.get( headerFile ) ) ) {
					logf( Level.DEBUG, "deleted '%s'.", headerFile );
				}
			}
		}
	}

	private void parseSourceFiles() {
		// parsing -----------------------------------------------------------------------------------------------------
		compilationUnits.addAll(
				sourceFiles.stream().map( t -> {
					logf( Level.DEBUG, "parsing source file '%s'", t );
					CompilationUnit cu = null;
					Logger logger = new Logger( LOGGER, t );
					try {
						ANTLRInputStream input = new ANTLRFileStream( t );
						ChoralLexer lexer = new ChoralLexer( input );
						CommonTokenStream tokens = new CommonTokenStream( lexer );
						ChoralParser cp = new ChoralParser( tokens );
						cp.removeErrorListeners();
						cp.addErrorListener( new ParsingErrorListener( logger ) );
						ChoralParser.CompilationUnitContext ctx = cp.compilationUnit();
						if( !logger.hasErrors() ) {
							cu = AstOptimizer.loadParameters().optimise( ctx, t );
						}
					} catch( ChoralException | IOException e ) {
						logger.logf( Level.ERROR, "%s: %s", t, e.getMessage() );
						cu = null;
					}
					if( cu == null )
						logf( Level.ERROR, "compilation of '%s' failed (see details above).", t );
					if( targetSourceFiles.contains( t ) ) {
						targetCompilationUnits.add(
								cu ); // we add this CU to the list of the target compilation units
					}
					return cu;
				} ).collect( Collectors.toList() )
		);
	}

	private void type() {
		TyperOptions typerOptions = new TyperOptions( VerbosityLevel.WARNINGS );
		Typer.annotate( targetCompilationUnits, Collections.emptyList(), typerOptions );
	}

	private void project() {
		// projection --------------------------------------------------------------------------------------------------
		if( projection ) {
			// TODO: update to have the main file/CU and the classpath, e.g., we launched choral with `choral MyFile SomeWorld`
			//		 but we have the user-classes with MyFile1, MyFile2, etc. as a list of CUs
			// 		 then we pass both elements to SoloistProjector

			// we collect all projectable templates from the target compilation units
//			Set< Pair< Pair< String, Node >, WorldArgument > > projectableTemplates = new HashSet<>();
			Set< ProjectableTemplate > projectableTemplates = new HashSet<>();
			if( targetWorlds.size() > 0 ) {
				for( String targetWorld : targetWorlds ) {
					projectableTemplates.addAll(
							new DependencyVisitor( targetCompilationUnits,
									new WorldArgument( new Name( targetWorld ) ) )
									.collectTemplates( targetSymbol )
					);
				}

			} else {
				// we project for all worlds in the target template
				logf( Level.DEBUG, "Projecting for all worlds" );
				for( WorldArgument targetWorld : DependencyVisitor.getWorldArguments(
						targetCompilationUnits,
						targetSymbol ) ) {
					logf( Level.DEBUG, "Projecting for world: %s",
							targetWorld.name().identifier() );
					projectableTemplates.addAll(
							new DependencyVisitor( targetCompilationUnits,
									targetWorld ).collectTemplates(
									targetSymbol )
					);
				}
			}

			// we project them into different lists
			projectableTemplates.forEach( pt ->
					compilableCUs.add( projectAndEncloseInCompilationUnit(
									pt.packageDeclaration(),
									pt.imports(),
									pt.worldArgument(),
									pt.node()
							)
					)
			);
		} else {
			log( Level.INFO, "EndPoint Projection skipped." );
		}
		if( LOGGER.hasErrors() ) {
			die();
		}
	}

	private void decoupleUnits() {
		compilableCUs = compilableCUs.stream().map(
				CourtesyMethodsSynthesiser::visitCompilationUnit ).collect( Collectors.toList() );
	}

	private void generateJavaFiles() throws IOException {
		if( !isDryRun ) {
			Collection< ? extends SourceObject > sources = compilableCUs.stream().map(
					JavaCompiler::compile ).flatMap( Collection::stream ).collect(
					Collectors.toList() );
			for( SourceObject source : sources ) {
				SourceWriter.writeSource( source, Paths.get( destinationFolder ) );
			}
		} else {
			logf( Level.INFO,
					"The compiler has been run in 'dry' mode, skipping production of Java classes" );
		}

	}

	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	// - - - - - - - - - - - - - - - - - - - - - - AUXILIARY METHODS - - - - - - - - - - - - - - - - - - - - - - - - -
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private List< String > getChoralSourceFiles( List< String > foldersList ) {
		return foldersList
				.stream()
				.filter( cpString -> Files.isDirectory( Path.of( cpString ) ) )
				.flatMap( cpString -> {
					try {
						return Files.walk( Path.of( cpString ) );
					} catch( IOException e ) {
						logf( Level.INFO, "Path '%s' in classpath does not exist", cpString );
					}
					return null;
				} )
				.filter( ( cpPath ) -> {
					// check file extension
					if( !cpPath.toAbsolutePath().toString().endsWith( SOURCE_FILE_EXTENSION ) ) {
						logf( Level.INFO,
								"ignoring file '%s': Choral source files must have extension '%s'.",
								cpPath.toAbsolutePath(), SOURCE_FILE_EXTENSION );
						return false;
					}
					if( Files.notExists( cpPath ) ) {
						logf( Level.ERROR, "file '%s' does not exists.", cpPath.toAbsolutePath() );
						return false;
					}
					return true;
				} )
				.distinct()
				.map( p -> p.toAbsolutePath().toString() )
				.collect( Collectors.toList() );
	}

	private List< String > getChoralSourceFiles( String folder ) {
		return getChoralSourceFiles( Collections.singletonList( folder ) );
	}

	private static String escapeString( String s ) {
		return "\"" + s + "\"";
	}

	private static void addChoreographyAnnotation(
			Node node, String name, String role, List< ImportDeclaration > imports
	) {
		imports.add( new ImportDeclaration( "choral.annotations.Choreography", null ) );
		Map< Name, LiteralExpression > values = new HashMap<>();
		values.put( new Name( "name" ),
				new LiteralExpression.StringLiteralExpression( escapeString( name ), null ) );
		values.put( new Name( "role" ),
				new LiteralExpression.StringLiteralExpression( escapeString( role ), null ) );
		Annotation a = new Annotation( new Name( "Choreography" ), values );
		if( node instanceof Interface ) {
			( (Interface) node ).annotations().add( a );
		} else if( node instanceof Class ) {
			( (Class) node ).annotations().add( a );
		} else if( node instanceof Enum ) {
			( (Enum) node ).annotations().add( a );
		}
	}

	private CompilationUnit projectAndEncloseInCompilationUnit(
			Optional< String > pkgDec, List< ImportDeclaration > imports, WorldArgument w, Node node
	) {
		ArrayList< ImportDeclaration > _imports = new ArrayList<>( imports );
		_imports.add( UnitRepresentation.UNIT_IMPORT_DECLARATION );
		if( node instanceof Interface ) {
			Interface projectedInterface = new SoloistProjector( w ).visit( ( (Interface) node ) );
			if( annotate ) {
				addChoreographyAnnotation( projectedInterface,
						( (Interface) node ).name().identifier(), w.name().identifier(), _imports );
			}
			return new CompilationUnit(
					pkgDec,
					new ImportProjector( _imports ).projectImports( projectedInterface ),
					Collections.singletonList( projectedInterface ),
					Collections.emptyList(),
					Collections.emptyList(),
					node.position().sourceFile()
			);
		}
		if( node instanceof Class ) {
			Class projectedClass = new SoloistProjector( w ).visit( ( (Class) node ) );
			if( annotate ) {
				addChoreographyAnnotation( projectedClass, ( (Class) node ).name().identifier(),
						w.name().identifier(), _imports );
			}
			return new CompilationUnit(
					pkgDec,
					new ImportProjector( _imports ).projectImports( projectedClass ),
					Collections.emptyList(),
					Collections.singletonList( projectedClass ),
					Collections.emptyList(),
					node.position().sourceFile()
			);
		}
		if( node instanceof Enum ) {
			Enum projectedEnum = new SoloistProjector( w ).visit( ( (Enum) node ) );
			if( annotate ) {
				addChoreographyAnnotation( projectedEnum, ( (Enum) node ).name().identifier(),
						w.name().identifier(), _imports );
			}
			return new CompilationUnit(
					pkgDec,
					new ImportProjector( _imports ).projectImports( projectedEnum ),
					Collections.emptyList(),
					Collections.emptyList(),
					Collections.singletonList( projectedEnum ),
					node.position().sourceFile()
			);
		}
		throw new ChoralException(
				"projectAndEncloseInCompilationUnit launched on a node different from an Interface, Class, or Enum" );
	}

	private void die() {
		System.err.println( "Compilation failed, see above for details." );
		System.err.println( NAME + " found errors it was not able to fix and had to give up." );
		System.err.println( "\t (\\  /)\n\t ( .  .)\n\tC(\") (\")" );
		System.exit( 1 );
	}

	public void logf( Level level, String format, Object... args ) {
		LOGGER.logf( level, format, args );
	}

	private void log( Level level, String message ) {
		LOGGER.log( level, message );
	}

    /*public static void printInLispFormat(String src) {
        // toStringTree modifies the shape of the tree
        // and it cannot be used by other visitors!
        System.out.println("\n\n* * * * * * LISP Format * * * * * *");
        ChoralParser p = parseSource(src);
        System.out.println(p.compilationUnit().toStringTree(p));
        System.out.println("\n* * * * * * LISP Format * * * * * * \n");
        //		System.out.println( PrettyTree.toPrettyTree( tree, Arrays.asList( parser.getRuleNames() ) ) );
    }*/

	// = = = = = = = = = = = = = PROFILING = = = = = = = = = = = = = = = = = = = =

	public static Map< String, Long > mainProfile( String[] args ) throws Exception {
		return new Compiler().runProfile( args );
	}

	private Map< String, Long > runProfile( String[] args ) {
		Map< String, Long > performance = new HashMap<>();
		performance.put( "read commands", trackPerformance( () -> readCommands( args ) ) );
		performance.put( "load sources", trackPerformance( () -> {
			try {
				loadSourceFiles();
			} catch( Exception e ) {
				e.printStackTrace();
			}
		} ) );
		performance.put( "parse sources", trackPerformance( this::parseSourceFiles ) );
		performance.put( "project", trackPerformance( this::project ) );
		performance.put( "decouple units", trackPerformance( this::decoupleUnits ) );
		performance.put( "write files", trackPerformance( () -> {
			try {
				generateJavaFiles();
			} catch( Exception e ) {
				e.printStackTrace();
			}
		} ) );
		return performance;
	}

	private Long trackPerformance( Runnable r ) {
		Long start = System.nanoTime();
		r.run();
		Long finish = System.nanoTime();
		return finish - start;
	}


	public static class ParsingErrorListener extends BaseErrorListener {

		private Logger logger;

		public ParsingErrorListener( Logger logger ) {
			this.logger = logger;
		}

		@Override
		public void syntaxError(
				Recognizer< ?, ? > recognizer,
				Object offendingSymbol,
				int line,
				int charPositionInLine,
				String msg,
				RecognitionException e
		) {
			List< String > stack = ( (Parser) recognizer ).getRuleInvocationStack();
			Collections.reverse( stack );
			logger.logfWithPosition( Logger.Level.ERROR, line, charPositionInLine, "%s", msg );
			// System.err.println( "ERROR: " + recognizer.getInputStream().getSourceName()	+ "[" + line + ", " + charPositionInLine + "]: error:" + msg);
		}
	}
}
