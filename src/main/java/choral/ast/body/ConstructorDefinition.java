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

package choral.ast.body;

import choral.ast.Node;
import choral.ast.Position;
import choral.ast.expression.MethodCallExpression;
import choral.ast.statement.BlockStatement;
import choral.ast.statement.ExpressionStatement;
import choral.ast.statement.NilStatement;
import choral.ast.statement.Statement;
import choral.ast.visitors.ChoralVisitorInterface;
import choral.types.Member;

import static choral.ast.body.ConstructorModifier.*;

import java.util.EnumSet;
import java.util.Optional;

public class ConstructorDefinition extends Node {
	private final ConstructorSignature signature;
	private final MethodCallExpression explicitConstructorInvocation;
	private final Statement blockStatements;
	private final EnumSet< ConstructorModifier > modifiers;

	public ConstructorDefinition(
			final ConstructorSignature signature,
			final MethodCallExpression explicitConstructorInvocation,
			final Statement blockStatements,
			final EnumSet< ConstructorModifier > modifiers,
			final Position position
	) {
		super( position );
		this.signature = signature;
		this.explicitConstructorInvocation = explicitConstructorInvocation;
		this.blockStatements = blockStatements;
		this.modifiers = modifiers;
	}

	private Member.HigherConstructor typeAnnotation;

	public Optional< ? extends Member.HigherConstructor > typeAnnotation() {
		return Optional.ofNullable( typeAnnotation );
	}

	public void setTypeAnnotation( Member.HigherConstructor typeAnnotation ) {
		this.typeAnnotation = typeAnnotation;
		this.signature.setTypeAnnotation( typeAnnotation );
	}

	public EnumSet< ConstructorModifier > modifiers() {
		return modifiers;
	}

	public boolean isPublic() {
		return modifiers.contains( PUBLIC );
	}

	public boolean isProtected() {
		return modifiers.contains( PROTECTED );
	}

	public boolean isPrivate() {
		return modifiers.contains( PRIVATE );
	}

	public boolean isPackagePrivate() {
		return !( isPrivate() || isProtected() || isPublic() );
	}

	public ConstructorSignature signature() {
		return signature;
	}

	public Optional< MethodCallExpression > explicitConstructorInvocation() {
		return Optional.ofNullable( explicitConstructorInvocation );
	}

	public Statement blockStatements() {
		return blockStatements;
	}

	public Statement body() {
		if( explicitConstructorInvocation == null ) {
			return blockStatements;
		} else {
			return new ExpressionStatement( explicitConstructorInvocation, blockStatements,
					explicitConstructorInvocation.position() );
		}
	}

	@Override
	public < R > R accept( ChoralVisitorInterface< R > v ) {
		return v.visit( this );
	}

}
