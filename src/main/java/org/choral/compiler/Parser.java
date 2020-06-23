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

package org.choral.compiler;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.choral.ast.CompilationUnit;
import org.choral.grammar.ChoralLexer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Parser {

    /* ToDo: parse choral source from string
    public static CompilationUnit parseSource(String sourceCode) {
        // InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
        throw new UnsupportedOperationException();
    } */

	public static CompilationUnit parseSourceFile( File file ) throws IOException {
		return parseSourceFile( file.getCanonicalPath() );
	}

	public static CompilationUnit parseSourceFile( String file ) throws IOException {
		ANTLRInputStream input = new ANTLRFileStream( file );
		ChoralLexer lexer = new ChoralLexer( input );
		CommonTokenStream tokens = new CommonTokenStream( lexer );
		org.choral.grammar.ChoralParser cp = new org.choral.grammar.ChoralParser( tokens );
		cp.removeErrorListeners();
		// ToDo: common error messages
		cp.addErrorListener( new ParsingErrorListener( file ) );
		org.choral.grammar.ChoralParser.CompilationUnitContext ctx = cp.compilationUnit();
		return AstOptimizer
				.loadParameters( /* new String[]{ "showDebug" } */ )
				.optimise( ctx, file );
	}

	public static CompilationUnit parseSourceFile(
			InputStream in, String file
	) throws IOException {
		ANTLRInputStream input = new ANTLRInputStream( in );
		ChoralLexer lexer = new ChoralLexer( input );
		CommonTokenStream tokens = new CommonTokenStream( lexer );
		org.choral.grammar.ChoralParser cp = new org.choral.grammar.ChoralParser( tokens );
		cp.removeErrorListeners();
		// ToDo: common error messages
		cp.addErrorListener( new ParsingErrorListener( file ) );
		org.choral.grammar.ChoralParser.CompilationUnitContext ctx = cp.compilationUnit();
		return AstOptimizer
				.loadParameters( /* new String[]{ "showDebug" } */ )
				.optimise( ctx, file );
	}
}
