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

import choral.ast.Position;
import choral.exceptions.AstPositionedException;
import choral.exceptions.SyntaxException;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static choral.Choral.relativizePath;

public class ParsingErrorListener extends BaseErrorListener {

	private final String file;
	private final List< AstPositionedException > errors;

	public ParsingErrorListener( String file ) {
		this.file = file;
		this.errors = new ArrayList<>();
	}

	public List< ? extends AstPositionedException > getErrors() {
		return this.errors;
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
		String file = recognizer.getInputStream().getSourceName().equals( "<unknown>" )
				? this.file
				: relativizePath( recognizer.getInputStream().getSourceName() );
		errors.add( new SyntaxException( new Position( file, line, charPositionInLine ), msg ) );
	}
}
