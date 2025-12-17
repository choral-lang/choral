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

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import choral.ast.CompilationUnit;
import choral.exceptions.ChoralCompoundException;
import choral.grammar.ChoralLexer;

public class Parser {

	private static CompilationUnit parse( String fileName, CharStream content ){
		ChoralLexer lexer = new ChoralLexer(content);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		choral.grammar.ChoralParser cp = new choral.grammar.ChoralParser(tokens);
		cp.removeErrorListeners();
		ParsingErrorListener errorListener = new ParsingErrorListener(fileName);
		cp.addErrorListener( errorListener );
		choral.grammar.ChoralParser.CompilationUnitContext ctx = cp.compilationUnit();
		if( errorListener.getErrors().isEmpty() ) {
			return AstOptimizer
					.loadParameters( /* new String[]{ "showDebug" } */ )
					.optimise( ctx, fileName );
		} else {
			throw new ChoralCompoundException( errorListener.getErrors() );
		}
	}

	public static CompilationUnit parseString( String sourceCode ) {
		CharStream content = CharStreams.fromString(sourceCode);
		return parse(null, content);
	}

	public static CompilationUnit parseSourceFile( File file ) throws IOException {
		String filename = file.getCanonicalPath();
		CharStream input = CharStreams.fromFileName(filename);
		return parse(filename, input);
	}

	public static CompilationUnit parseSourceFile( InputStream in, String filename ) throws IOException {
		ANTLRInputStream input = new ANTLRInputStream( in );
		return parse(filename, input);
	}
}
