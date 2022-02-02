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

import choral.ast.Name;
import choral.ast.Position;
import choral.ast.type.FormalWorldParameter;
import choral.ast.visitors.ChoralVisitorInterface;
import choral.types.HigherEnum;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static choral.ast.body.ClassModifier.*;


public class Enum extends TemplateDeclaration {
	private final List< EnumConstant > cases;
	private final List< Annotation > annotations;
	private final EnumSet< ClassModifier > modifiers;

	public Enum(
			final Name name,
			final FormalWorldParameter worldParameter,
			final List< EnumConstant > cases,
			List< Annotation > annotations,
			final EnumSet< ClassModifier > modifiers,
			final Position position
	) {
		super( name, Collections.singletonList( worldParameter ), Collections.emptyList(),
				position );
		this.cases = cases;
		this.annotations = annotations;
		this.modifiers = EnumSet.copyOf( modifiers );
	}

	private HigherEnum typeAnnotation;

	public Optional< ? extends HigherEnum > typeAnnotation() {
		return Optional.ofNullable( typeAnnotation );
	}

	public void setTypeAnnotation( HigherEnum typeAnnotation ) {
		this.typeAnnotation = typeAnnotation;
	}

	public EnumSet< ClassModifier > modifiers() {
		return modifiers;
	}

	/* public boolean isStatic() {
		return modifiers.contains(STATIC);
	} */

	@Override
	public boolean isPublic() {
		return modifiers.contains( PUBLIC );
	}

	@Override
	public boolean isProtected() {
		return modifiers.contains( PROTECTED );
	}

	@Override
	public boolean isPrivate() {
		return modifiers.contains( PRIVATE );
	}

	@Override
	public boolean isFinal() {
		return true; // enums are always final modifiers.contains(FINAL);
	}

	@Override
	public boolean isAbstract() {
		return false; // enums are never final;
	}

	public List< Annotation > annotations() {
		return annotations;
	}

	public List< EnumConstant > cases() {
		return cases;
	}

	@Override
	public < R > R accept( ChoralVisitorInterface< R > v ) {
		return v.visit( this );
	}

}
