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
import java.util.List;
import java.util.Optional;

import org.choral.ast.Name;
import org.choral.ast.Position;
import org.choral.ast.type.FormalTypeParameter;
import org.choral.ast.type.FormalWorldParameter;
import org.choral.ast.type.TypeExpression;
import org.choral.ast.visitors.ChoralVisitorInterface;
import org.choral.types.HigherInterface;

import static org.choral.ast.body.InterfaceModifier.*;

public class Interface extends TemplateDeclaration {
	private final List< InterfaceMethodDefinition > methods;
	private final List< TypeExpression > extendsInterfaces;
	private final List< Annotation > annotations;
	private final EnumSet< InterfaceModifier > modifiers;

	public Interface(
			final Name name,
			final List< FormalWorldParameter > worldParameters,
			final List< FormalTypeParameter > typeParameters,
			final List< TypeExpression > extendsInterfaces,
			final List< InterfaceMethodDefinition > methods,
			final List< Annotation > annotations,
			final EnumSet< InterfaceModifier > modifiers,
			final Position position ) {
		super( name, worldParameters, typeParameters, position );
		this.methods = methods;
		this.extendsInterfaces = extendsInterfaces;
		this.annotations = annotations;
		this.modifiers = EnumSet.copyOf( modifiers );
	}

	private HigherInterface typeAnnotation;

	public Optional< ? extends HigherInterface > typeAnnotation() {
		return Optional.ofNullable( typeAnnotation );
	}

	public void setTypeAnnotation( HigherInterface typeAnnotation ) {
		this.typeAnnotation = typeAnnotation;
	}

	public EnumSet< InterfaceModifier > modifiers() {
		return modifiers;
	}

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
		return false;
	}

	@Override
	public boolean isAbstract() {
		return true; // modifiers.contains(ABSTRACT);
	}

	public List< TypeExpression > extendsInterfaces() {
		return extendsInterfaces;
	}

	public List< InterfaceMethodDefinition > methods() {
		return methods;
	}

	public List< Annotation > annotations() {
		return annotations;
	}

	@Override
	public < R > R accept( ChoralVisitorInterface< R > v ) {
		return v.visit( this );
	}

}
