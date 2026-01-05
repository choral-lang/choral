/*
 *   Copyright (C) 2019 by Saverio Giallorenzo <saverio.giallorenzo@gmail.com>
 *   Copyright (C) 2019 by Fabrizio Montesi <famontesi@gmail.com>
 *   Copyright (C) 2019 by Marco Peressotti <marco.peressotti@gmail.com>
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as
 *   published by the Free Software Foundation; either version 2 of the
 *   License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the
 *   Free Software Foundation, Inc.,
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package choral.ast;

import choral.ast.body.Class;
import choral.ast.body.Enum;
import choral.ast.body.Interface;
import choral.ast.visitors.ChoralVisitorInterface;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class CompilationUnit extends Node {

	private final Optional<String> packageDeclaration;
	private final List< ImportDeclaration > imports;
	private final List< Interface > interfaces;
	private final List< Class > classes;
	private final List< Enum > enums;
	private final String primaryType;

	public CompilationUnit(
			final Optional<String> packageDeclaration, final List< ImportDeclaration > imports,
			final List< Interface > interfaces, final List< Class > classes,
			final List< Enum > enums, final String sourceFile
	) {
		super( new Position( sourceFile, 0, 0 ) );
		this.packageDeclaration = packageDeclaration;
		this.imports = imports;
		this.interfaces = interfaces;
		this.classes = classes;
		this.enums = enums;
		if (sourceFile == null) {
			this.primaryType = null;
		}
		else if (sourceFile.lastIndexOf(File.separatorChar) == -1) {
			this.primaryType = sourceFile;
		}
		else {
			int k = Math.max( 0, sourceFile.lastIndexOf( '.' ) );
			int j = Math.min( k, sourceFile.lastIndexOf( File.separatorChar ) + 1 );
			this.primaryType = sourceFile.substring( j, k );
		}
	}

	public String primaryType() {
		return this.primaryType;
	}

	public List< ImportDeclaration > imports() {
		return imports;
	}

	public List< Interface > interfaces() {
		return interfaces;
	}

	public List< Class > classes() {
		return classes;
	}

	public List< Enum > enums() {
		return enums;
	}

	public Optional<String> packageDeclaration() {
		return packageDeclaration;
	}

	@Override
	public < R > R accept( ChoralVisitorInterface< R > v ) {
		return v.visit( this );
	}

}
