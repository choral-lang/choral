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

package choral.chaining;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import choral.ast.CompilationUnit;
import choral.ast.visitors.PrettyPrinterVisitor;
import choral.compiler.*;
import choral.grammar.ChoralLexer;
import choral.grammar.ChoralParser;

import java.io.IOException;

public class testCompiler {

	public static void main( String[] args ) throws IOException {

		String file = "src/tests/java/ChainingTests/ChainingOperator.ch";

		ANTLRInputStream input = new ANTLRFileStream( file );
		ChoralLexer lexer = new ChoralLexer( input );
		CommonTokenStream tokens = new CommonTokenStream( lexer );
		ChoralParser cp = new ChoralParser( tokens );
		cp.removeErrorListeners();
		cp.addErrorListener( new ParsingErrorListener( file ) );
		ChoralParser.CompilationUnitContext ctx = cp.compilationUnit();
		CompilationUnit cu = AstOptimizer
				.loadParameters()
				.optimise( ctx, file );
		cu = AstDesugarer.desugar( cu );

		System.out.println( new PrettyPrinterVisitor().visit( cu ) );

//		JavaSourceWriter.writeSources(
//			Paths.get( "src/tests/java/ChainingTests/ChainingOperator" ),
//			"ChainingTests.ChainingOperator",
//			JavaCompiler.compile( cu )
//		);

	}

}
