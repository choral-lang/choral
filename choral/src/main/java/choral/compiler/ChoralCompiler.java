package choral.compiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import choral.ast.CompilationUnit;
import choral.ast.visitors.PrettyPrinterVisitor;
import choral.compiler.SourceObject.ChoralSourceObject;
import choral.compiler.SourceObject.HeaderSourceObject;

/**
 * A class to translate CompilationUnits into choral programs, and 
 * write them to the provided destinationfolder
 */
public class ChoralCompiler extends PrettyPrinterVisitor {

    private static final String NEWLINE = "\n";
	private static final String _2NEWLINE = NEWLINE + NEWLINE;
	private static final String SEMICOLON = ";";
	private static final String PACKAGE = "package";
    
	public static void generateChoralFiles( 
		Collection< CompilationUnit > choralSources, 
		Collection< CompilationUnit > choralHeaders, 
		Optional< Path > targetpath 
	) throws IOException {

		String destinationFolder = targetpath
			.map( tp -> tp.toAbsolutePath().toString() )
			.orElse( System.getProperty( "user.dir" ) + File.separator + "dist" );

		Collection< ? extends SourceObject > sources = choralSources.stream().map(
				ChoralCompiler::compileSources ).flatMap( Collection::stream ).collect(
				Collectors.toList() );
		Collection< ? extends SourceObject > headers = choralHeaders.stream().map(
				ChoralCompiler::compileHeaders ).flatMap( Collection::stream ).collect(
				Collectors.toList() );
		for( SourceObject source : sources ) {
			SourceWriter.writeSource( source, Paths.get( destinationFolder ) );
		}
		for( SourceObject header : headers ) {
			SourceWriter.writeSource( header, Paths.get( destinationFolder ) );
		}

	}

    private static Collection< ChoralSourceObject > compileSources( CompilationUnit n ) {
		List< ChoralSourceObject > choralSources = new LinkedList<>();
		ChoralCompiler choralCompiler = new ChoralCompiler();
		Path sourcePath = Paths.get( n.position().sourceFile() );
		String imports = choralCompiler.visitAndCollect( n.imports(), SEMICOLON + NEWLINE,
				SEMICOLON + _2NEWLINE );
		String packageDeclaration = n.packageDeclaration().isPresent() ? PACKAGE + " " + n.packageDeclaration().get() + SEMICOLON + _2NEWLINE : "";
		n.interfaces().forEach( x -> choralSources.add(
				new ChoralSourceObject( packageDeclaration + imports + choralCompiler.visit( x ),
                ChoralSourceObject.combineName( n.packageDeclaration(),
								x.name().identifier() ),
						sourcePath.resolveSibling(
								x.name().identifier() + ChoralSourceObject.FILE_EXTENSION ).toString() ) ) );
		n.enums().forEach( x -> choralSources.add(
				new ChoralSourceObject( packageDeclaration + imports + choralCompiler.visit( x ),
                ChoralSourceObject.combineName( n.packageDeclaration(),
								x.name().identifier() ),
						sourcePath.resolveSibling(
								x.name().identifier() + ChoralSourceObject.FILE_EXTENSION ).toString() ) ) );
		n.classes().forEach( x -> choralSources.add(
				new ChoralSourceObject( packageDeclaration + imports + choralCompiler.visit( x ),
                ChoralSourceObject.combineName( n.packageDeclaration(),
								x.name().identifier() ),
						sourcePath.resolveSibling(
								x.name().identifier() + ChoralSourceObject.FILE_EXTENSION ).toString() ) ) );
		return choralSources;
	}

	private static Collection< HeaderSourceObject > compileHeaders( CompilationUnit n ) {
		List< HeaderSourceObject > choralHeaders = new LinkedList<>();
		ChoralCompiler choralCompiler = new ChoralCompiler();
		Path sourcePath = Paths.get( n.position().sourceFile() );
		String imports = choralCompiler.visitAndCollect( n.imports(), SEMICOLON + NEWLINE,
				SEMICOLON + _2NEWLINE );
		String packageDeclaration = n.packageDeclaration().isPresent() ? PACKAGE + " " + n.packageDeclaration().get() + SEMICOLON + _2NEWLINE : "";
		n.interfaces().forEach( x -> choralHeaders.add(
				new HeaderSourceObject( packageDeclaration + imports + choralCompiler.visit( x ),
                HeaderSourceObject.combineName( n.packageDeclaration(),
								x.name().identifier() ),
						sourcePath.resolveSibling(
								x.name().identifier() + HeaderSourceObject.FILE_EXTENSION ).toString() ) ) );
		n.enums().forEach( x -> choralHeaders.add(
				new HeaderSourceObject( packageDeclaration + imports + choralCompiler.visit( x ),
                HeaderSourceObject.combineName( n.packageDeclaration(),
								x.name().identifier() ),
						sourcePath.resolveSibling(
								x.name().identifier() + HeaderSourceObject.FILE_EXTENSION ).toString() ) ) );
		n.classes().forEach( x -> choralHeaders.add(
				new HeaderSourceObject( packageDeclaration + imports + choralCompiler.visit( x ),
                HeaderSourceObject.combineName( n.packageDeclaration(),
								x.name().identifier() ),
						sourcePath.resolveSibling(
								x.name().identifier() + HeaderSourceObject.FILE_EXTENSION ).toString() ) ) );
		return choralHeaders;
	}
}
