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

package choral.BookSelling;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.lang.ArrayUtils;
import org.choral.ast.CompilationUnit;
import org.choral.ast.Node;
import org.choral.ast.body.Class;
import org.choral.ast.body.Enum;
import org.choral.ast.body.Interface;
import org.choral.ast.type.WorldArgument;
import org.choral.ast.visitors.PrettyPrinterVisitor;
import org.choral.compiler.*;
import org.choral.compiler.Compiler;
import org.choral.compiler.soloist.DependecyVisitor;
import org.choral.compiler.soloist.SoloistProjector;
import org.choral.compiler.soloist.SoloistProjectorException;
import org.choral.grammar.ChoralLexer;
import org.choral.grammar.ChoralParser;
import org.choral.utils.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class testCompiler {

	public static void main( String[] args ) {

		String classPath = "src/tests/choral/OtherUtils";
		String targetFolder = "src/tests/choral/BookSelling";
		String destinationFolder = "src/tests/java/choral/BookSelling/BookSelling";

		String targetTemplate = "BuyBook";
		String[] targetWorlds = new String[]{ "Buyer", "Bank" };

		try {
			Compiler.main( ( String[] ) ArrayUtils.addAll(
					new String[]{
					"-cp", classPath,
					"-d", destinationFolder,
					"-t", targetFolder,
					targetTemplate },
					targetWorlds )
			);
		} catch ( Exception e ) {
			e.printStackTrace();
		}


//		String classPath, outputLocation;
//		classPath = "src/tests/choral/BookSelling";
//		outputLocation = "src/tests/java/choral/BookSelling/BookSelling";
//
//		List< CompilationUnit > compilationUnits = new ArrayList<>();
//
//		// we gather and parse all classes
//		try( Stream< Path > classes = Files.walk( Paths.get( classPath ) ) ){
//			classes.filter( p -> p.toAbsolutePath().toString().endsWith( ".ch" ) )
//					.forEach( p -> {
//						try {
//							ANTLRInputStream input = new ANTLRFileStream( p.toAbsolutePath().toString() );
//							ChoralLexer lexer = new ChoralLexer( input );
//							CommonTokenStream tokens = new CommonTokenStream( lexer );
//							ChoralParser cp = new ChoralParser( tokens );
//							cp.removeErrorListeners();
//							cp.addErrorListener( new ParsingErrorListener() );
//							ChoralParser.CompilationUnitContext ctx = cp.compilationUnit();
//							CompilationUnit cu = AstOptimizer
//									.loadParameters()
//									.optimise( ctx );
//							compilationUnits.add( AstDesugarer.desugar( cu ) );
//						} catch ( IOException e ) {
//							e.printStackTrace();
//						}
//					});
//		} catch ( IOException e ){
//			e.printStackTrace();
//		}
//
////		compilationUnits.forEach( c -> System.out.println( new PrettyPrinterVisitor().visit( c ) ) );
//
//		final String mainClass = "BuyBook";
//		final WorldArgument world = new WorldArgument( "Buyer" );
//
//		// we collect all projectable templates
//		Set< Pair < Node, WorldArgument > > projectableTemplates =
//				new DependecyVisitor( compilationUnits, world ).collectTemplates( mainClass );
//
//		// we project them into different lists
//		List< Interface > interfaces  = projectableTemplates.stream()
//				.filter( p -> p.left() instanceof Interface )
//				.map( p -> new SoloistProjector( p.right() ).visit( ( Interface ) p.left() ) )
//				.collect( Collectors.toList() );
//
//		List< Class > classes = projectableTemplates.stream()
//				.filter( p -> p.left() instanceof Class )
//				.map( p -> new SoloistProjector( p.right() ).visit( ( Class ) p.left() ) )
//				.collect( Collectors.toList() );
//
//		List< Enum > enums = projectableTemplates.stream()
//				.filter( p -> p.left() instanceof Enum )
//				.map( p -> new SoloistProjector( p.right() ).visit( ( Enum ) p.left() ) )
//				.collect( Collectors.toList() );
//
////		List< Class > projectedClasses = new ArrayList<>();
////			final String mainClass = "BuyBook";
////			WorldArgument[] worlds = { new WorldArgument(  "Buyer" ), new WorldArgument( "Seller" ), new WorldArgument(  "Bank" ) };
////			for ( WorldArgument world : worlds ) {
////				Set< Pair< Node, WorldArgument > > projectableTemplates =
////						new DependecyVisitor( Collections.singletonList( cuo ), world ).collectTemplates( mainClass );
////				System.out.println( "PT for world " + world.name().identifier() + " are " + projectableTemplates.size() );
////				projectableTemplates.forEach( p -> projectedClasses.add( new SoloistProjector( p.right() ).visit( ( Class ) p.left() ) ) );
////			}
//
//		// we combine everything into a single compilation unit compilable to Java
//		// TODO: we will change this to better support imports
//		CompilationUnit compilableCU = new CompilationUnit( Collections.emptyList(), interfaces, classes, enums );
//
//		if( true ){
//			JavaSourceWriter.writeSources(
//							Paths.get( outputLocation ),
//							"choral.BookSelling.BookSelling",
//							JavaCompiler.compile( compilableCU )
//			);
//
//			try {
//				String imports = Arrays.stream( Files
//						.readString( Paths.get( classPath + "/" + "BuyerSellerShipper.ch" ) )
//						.split( "\n" ) )
//						.filter( s -> s.contains( "import" ) )
//						.map( s -> s.substring( 2 ) )
//						.collect( Collectors.joining( "\n" ) );
//
//				Files.walk( Paths.get( outputLocation ) )
//						.filter( Files::isRegularFile )
//						.forEach( p -> {
//							try {
//								String c = Files.readString( p );
//								String pkg = Arrays.stream( c.split( "\n" ) )
//										.filter( s -> s.contains( "package" ) )
//										.collect( Collectors.joining( "\n" ) );
//								c = Arrays.stream( c.split( "\n" ) )
//										.filter( s -> !s.contains( "package" ) )
//										.collect( Collectors.joining( "\n" ) );
//								Files.write( p, ( pkg + "\n" + imports + "\n" + c ).getBytes() );
//							} catch ( IOException e ) {
//								e.printStackTrace();
//							}
//						} );
//			} catch ( IOException e ){
//				e.printStackTrace();
//			}
//		} else {
//			JavaCompiler.compile( compilableCU ).values().forEach( System.out::println );
//		}




	}

}
