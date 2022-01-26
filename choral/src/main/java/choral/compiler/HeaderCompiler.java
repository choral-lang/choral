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
import choral.ast.statement.Statement;
import choral.ast.visitors.PrettyPrinterVisitor;
import choral.compiler.SourceObject.ChoralSourceObject;
import choral.compiler.SourceObject.HeaderSourceObject;

import java.nio.file.Paths;

public class HeaderCompiler {

	private HeaderCompiler() {
	}

	public static HeaderSourceObject compile( CompilationUnit n ) {
		if( n.position().sourceFile() == null ) {
			// Trying to generate a header file for a compilation unit without a source file
			throw new UnsupportedOperationException( "Headers can be generated only for " );
		}
		String choralFile = n.position().sourceFile();
		String name = Paths.get( choralFile ).getFileName().toString();
		name = name.substring( 0, name.length() - ChoralSourceObject.FILE_EXTENSION.length() );
		String headerFile = choralFile.substring( 0,
				choralFile.length() - ChoralSourceObject.FILE_EXTENSION.length() ) + HeaderSourceObject.FILE_EXTENSION;

		return new HeaderSourceObject(
				new Compiler().visit( n ),
				HeaderSourceObject.combineName( n.packageDeclaration(), name ),
				headerFile
		);
	}

	private static class Compiler extends PrettyPrinterVisitor {

		@Override
		public String visit( Statement n ) {
			return "/* NOTHING TO SEE HERE... */";
		}
	}
}
