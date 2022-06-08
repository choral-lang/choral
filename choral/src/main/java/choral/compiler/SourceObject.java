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
import java.util.Optional;

public abstract class SourceObject {
	protected final String source;
	protected final String fullname;
	protected final String sourcefile;

	public SourceObject( String sourceCode, String fullname, String sourceFile ) {
		this.source = sourceCode;
		this.fullname = fullname;
		this.sourcefile = sourceFile;
	}

	static String combineName( Optional<String> qualifier, String name ) {
		if( qualifier.isEmpty() ) {
			return name;
		} else {
			return qualifier + "." + name;
		}
	}

	static String combineName( String qualifier, String name ) {
		if( qualifier == null || qualifier.isBlank() ) {
			return name;
		} else {
			return qualifier + "." + name;
		}
	}

	public final String sourceFile() {
		return sourcefile;
	}

	public final String sourceCode() {
		return source;
	}

	public final String name() {
		String[] x = fullName().split( "\\." );
		return x[ x.length - 1 ];
	}

	public final String fullName() {
		return fullname;
	}

	public abstract String fileExtension();

	public final String getCanonicalPath() {
		return fullname.replace( '.', File.separatorChar ) + fileExtension();
	}

	public final String getCanonicalFile() {
		return name() + fileExtension();
	}

	public static final class ChoralSourceObject extends SourceObject {

		public static String FILE_EXTENSION = ".ch";

		public ChoralSourceObject( String sourceCode, String fullname, String sourceFile ) {
			super( sourceCode, fullname, sourceFile );
		}

		public ChoralSourceObject( String sourceCode, String fullname ) {
			this( sourceCode, fullname, null );
		}

		@Override
		public String fileExtension() {
			return FILE_EXTENSION;
		}
	}

	public static final class HeaderSourceObject extends SourceObject {

		public static String FILE_EXTENSION = ".chh";

		public HeaderSourceObject( String sourceCode, String fullname, String sourceFile ) {
			super( sourceCode, fullname, sourceFile );
		}

		public HeaderSourceObject( String sourceCode, String fullname ) {
			this( sourceCode, fullname, null );
		}

		@Override
		public String fileExtension() {
			return FILE_EXTENSION;
		}
	}

	public static final class JavaSourceObject extends SourceObject {

		public static String FILE_EXTENSION = ".java";

		public JavaSourceObject( String sourceCode, String fullname, String sourceFile ) {
			super( sourceCode, fullname, sourceFile );
		}

		public JavaSourceObject( String sourceCode, String fullname ) {
			this( sourceCode, fullname, null );
		}

		@Override
		public String fileExtension() {
			return FILE_EXTENSION;
		}

	}
}