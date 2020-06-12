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

package org.choral.ast.body;

import org.choral.ast.Node;
import org.choral.ast.Position;
import org.choral.ast.statement.Statement;
import org.choral.ast.visitors.ChoralVisitorInterface;
import org.choral.types.Member;

import static org.choral.ast.body.ConstructorModifier.*;

import java.util.EnumSet;
import java.util.Optional;

public class ConstructorDefinition extends Node {
	private final ConstructorSignature signature;
	private final Statement body;
	private final EnumSet< ConstructorModifier > modifiers;

	public ConstructorDefinition(
			final ConstructorSignature signature,
			final Statement body,
			final EnumSet< ConstructorModifier > modifiers,
			final Position position
	) {
		super( position );
		this.signature = signature;
		this.body = body;
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

	public Statement body() {
		return body;
	}

	@Override
	public < R > R accept( ChoralVisitorInterface< R > v ) {
		return v.visit( this );
	}

}
