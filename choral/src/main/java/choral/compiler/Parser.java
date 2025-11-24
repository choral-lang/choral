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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import choral.ast.CompilationUnit;
import choral.ast.Position;
import choral.exceptions.AstPositionedException;
import choral.exceptions.ChoralCompoundException;
import choral.grammar.ChoralLexer;

public class Parser {

	private static CompilationUnit parsingHelper(String source, CharStream content){
		ChoralLexer lexer = new ChoralLexer(content);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		choral.grammar.ChoralParser cp = new choral.grammar.ChoralParser(tokens);
		cp.removeErrorListeners();
		ParsingErrorListener errorListener = new ParsingErrorListener(source);
		cp.addErrorListener( errorListener );
		choral.grammar.ChoralParser.CompilationUnitContext ctx = cp.compilationUnit();
		if( errorListener.getErrors().isEmpty() ) {
			return AstOptimizer
					.loadParameters( /* new String[]{ "showDebug" } */ )
					.optimise( ctx, null ); //null when parsing string, source otherwise 
		} else {
			List<? extends AstPositionedException> errors = errorListener.getErrors();
			List<Position> positions = new ArrayList<>();
			for (AstPositionedException error : errors){
				positions.add(error.position());
			}
			throw new ChoralCompoundException( errors, positions );
		}
	}

	public static CompilationUnit parseString(String sourceCode) throws IOException {
		CharStream content = CharStreams.fromString(sourceCode);
		return parsingHelper(sourceCode, content);
	}

	public static CompilationUnit parseSourceFile(
			File file
	) throws IOException {
		return parseSourceFile( file.getCanonicalPath() );
	}

	public static CompilationUnit parseSourceFile(
			String file
	) throws IOException {
		CharStream input = CharStreams.fromFileName(file);
		return parsingHelper(file, input);
	}

	public static CompilationUnit parseSourceFile( // used by HeaderLoader.java
			InputStream in, String file
	) throws IOException {
		ANTLRInputStream input = new ANTLRInputStream( in );
		ChoralLexer lexer = new ChoralLexer( input );
		CommonTokenStream tokens = new CommonTokenStream( lexer );
		choral.grammar.ChoralParser cp = new choral.grammar.ChoralParser( tokens );
		cp.removeErrorListeners();
		ParsingErrorListener errorListener = new ParsingErrorListener( file );
		cp.addErrorListener( errorListener );
		if( errorListener.getErrors().isEmpty() ) {
			choral.grammar.ChoralParser.CompilationUnitContext ctx = cp.compilationUnit();
			return AstOptimizer
					.loadParameters( /* new String[]{ "showDebug" } */ )
					.optimise( ctx, file );
		} else {
			throw new ChoralCompoundException( errorListener.getErrors() );
		}
//		cp.addErrorListener( new ParsingErrorListener( file ) );
//		choral.grammar.ChoralParser.CompilationUnitContext ctx = cp.compilationUnit();
//		return AstOptimizer
//				.loadParameters( /* new String[]{ "showDebug" } */ )
//				.optimise( ctx, file );
	}
}
