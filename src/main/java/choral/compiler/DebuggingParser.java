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

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import choral.grammar.ChoralLexer;
import choral.grammar.ChoralParser;

import java.io.IOException;

public class DebuggingParser {
	public static void main( String[] args ) throws IOException {
		String src = args[ 0 ];
		//		String sourceFile = Files.readString( Paths.get( src ) );
		System.out.println( "Parsing " + src );
		ANTLRFileStream input = null;
		try {
			input = new ANTLRFileStream( src );
		} catch( IOException e ) {
			e.printStackTrace();
		}
		ChoralLexer lexer = new ChoralLexer( input );
		CommonTokenStream tokens = new CommonTokenStream( lexer );
		ChoralParser p = new ChoralParser( tokens );
		p.removeErrorListeners();
		p.addErrorListener( new ParsingErrorListener( src ) );
		p.compilationUnit();
	}
}
