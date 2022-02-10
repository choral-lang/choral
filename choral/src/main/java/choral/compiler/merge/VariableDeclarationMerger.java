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

package choral.compiler.merge;

import choral.ast.body.VariableDeclaration;
import choral.ast.expression.AssignExpression;
import choral.ast.visitors.AbstractMerger;

import java.util.Optional;

class VariableDeclarationMerger extends AbstractMerger< VariableDeclaration > {
	private VariableDeclarationMerger() {
		super();
	}

	static VariableDeclaration mergeVariableDeclarations(
			VariableDeclaration n1, VariableDeclaration n2
	) {
		String errorPrefix = "Cannot merge variable declarations due to ";
		MergeException._assert(
				n1.name().equals( n2.name() ),
				errorPrefix + "different variable names: " + n1.name() + " and " + n2.name(), n1, n2
		);
		MergeException._assert(
				n1.type().equals( n2.type() ),
				errorPrefix + "different types:  " + n1.type() + " and " + n2.type(), n1, n2
		);

		Optional< AssignExpression > i1 = n1.initializer(), i2 = n2.initializer();
		MergeException._assert(
				( i1.isPresent() && i2.isPresent() ) || ( i1.isEmpty() && i2.isEmpty() ),
				errorPrefix + "different initializers: " + i1.orElse( null ) + " and " + i2.orElse(
						null ), n1, n2
		);

		return new VariableDeclaration(
				n1.name(),
				n1.type(),
				i1.isPresent() && i2.isPresent()
						? (AssignExpression) ExpressionsMerger.mergeExpressions( i1.get(),
						i2.get() )
						: null
		);
	}
}
