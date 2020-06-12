/*
 *     Copyright (C) 2019 by Saverio Giallorenzo <saverio.giallorenzo@gmail.com>
 *     Copyright (C) 2019 by Fabrizio Montesi <famontesi@gmail.com>
 *     Copyright (C) 2019 by Marco Peressotti <marco.peressotti@gmail.com>
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU Library General Public License as
 *     published by the Free Software Foundation; either version 2 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU Library General Public
 *     License along with this program; if not, write to the
 *     Free Software Foundation, Inc.,
 *     59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.choral.ast.body;

import java.util.EnumSet;
import java.util.Optional;

import org.choral.ast.Name;
import org.choral.ast.Node;
import org.choral.ast.Position;
import org.choral.ast.type.TypeExpression;
import org.choral.ast.visitors.ChoralVisitorInterface;
import org.choral.types.Member;

import static org.choral.ast.body.FieldModifier.*;

public class Field extends Node {
	private final Name name;
	private final TypeExpression type;
	private final EnumSet< FieldModifier > modifiers;

	public Field( final Name name, final TypeExpression type, final EnumSet< FieldModifier > modifiers, final Position position ) {
		super( position );
		this.name = name;
		this.type = type;
		this.modifiers = EnumSet.copyOf( modifiers );
	}

	private Member.Field typeAnnotation;

	public Optional< ? extends Member.Field > typeAnnotation() {
		return Optional.ofNullable( typeAnnotation );
	}

	public void setTypeAnnotation( Member.Field typeAnnotation ) {
		this.typeAnnotation = typeAnnotation;
	}

	public EnumSet< FieldModifier > modifiers() {
		return modifiers;
	}

	public boolean isStatic() {
		return modifiers.contains( STATIC );
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

	public boolean isFinal() {
		return modifiers.contains( FINAL );
	}

	public boolean isPackagePrivate() {
		return !( isPrivate() || isProtected() || isPublic() );
	}

	public Name name() {
		return name;
	}

	public TypeExpression typeExpression() {
		return type;
	}

	@Override
	public < R > R accept( ChoralVisitorInterface< R > v ) {
		return v.visit( this );
	}

}
