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

package choral.compiler.soloist;

import choral.ast.ImportDeclaration;
import choral.ast.Node;
import choral.ast.type.WorldArgument;

import java.util.List;
import java.util.Optional;

public class ProjectableTemplate {
	private final WorldArgument worldArgument;
	private final Optional< String > packageDeclaration;
	private final List< ImportDeclaration > imports;
	private final Node node;

	public ProjectableTemplate(
			Optional< String > packageDeclaration, List< ImportDeclaration > imports, Node node,
			WorldArgument worldArgument
	) {
		this.worldArgument = worldArgument;
		this.packageDeclaration = packageDeclaration;
		this.imports = imports;
		this.node = node;
	}

	public WorldArgument worldArgument() {
		return worldArgument;
	}

	public Optional< String > packageDeclaration() {
		return packageDeclaration;
	}

	public List< ImportDeclaration > imports() {
		return imports;
	}

	public Node node() {
		return node;
	}
}
