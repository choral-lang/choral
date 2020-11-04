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

package choral.ast.body;

import choral.ast.Node;
import choral.ast.Position;
import choral.ast.statement.Statement;
import choral.ast.visitors.ChoralVisitorInterface;
import choral.types.Member;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static choral.ast.body.ClassMethodModifier.*;

public class ClassMethodDefinition extends MethodDefinition {
	private final Statement body;
	private final EnumSet< ClassMethodModifier > modifiers;

	public ClassMethodDefinition(
			final MethodSignature signature,
			final Statement body,
			final List< Annotation > annotations,
			final EnumSet< ClassMethodModifier > modifiers,
			final Position position
	) {
		super( signature, annotations, position );
		this.body = body;
		this.modifiers = modifiers;
	}

	public Optional< Statement > body() {
		return Optional.ofNullable( body );
	}

	public EnumSet< ClassMethodModifier > modifiers() {
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

	public boolean isAbstract() {
		return modifiers.contains( ABSTRACT );
	}

	public boolean isPackagePrivate() {
		return !( isPrivate() || isProtected() || isPublic() );
	}

	@Override
	public < R > R accept( ChoralVisitorInterface< R > v ) {
		return v.visit( this );
	}

}
