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
import choral.ast.type.FormalTypeParameter;
import choral.ast.type.FormalWorldParameter;
import choral.ast.type.TypeExpression;
import choral.ast.visitors.ChoralVisitorInterface;
import choral.types.HigherClass;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static choral.ast.body.ClassModifier.*;

public class Class extends TemplateDeclaration {
	private final List< Field > fields;
	private final List< ClassMethodDefinition > methods;
	private final List< ConstructorDefinition > constructors;
	private final TypeExpression extendsClass;
	private final List< TypeExpression > implementsInterfaces;
	private final List< Annotation > annotations;
	private final EnumSet< ClassModifier > modifiers;

	public Class(
			final Name name,
			final List< FormalWorldParameter > worldparameters,
			final List< FormalTypeParameter > typeparameters,
			final TypeExpression extendsClass,
			final List< TypeExpression > implementsInterfaces,
			final List< Field > fields,
			final List< ClassMethodDefinition > methods,
			final List< ConstructorDefinition > constructors,
			final List< Annotation > annotations,
			final EnumSet< ClassModifier > modifiers,
			final Position position
	) {
		super( name, worldparameters, typeparameters, position );
		this.fields = fields;
		this.methods = methods;
		this.constructors = constructors;
		this.extendsClass = extendsClass;
		this.implementsInterfaces = implementsInterfaces;
		this.annotations = annotations;
		this.modifiers = ( modifiers == null ) ? EnumSet.noneOf(
				ClassModifier.class ) : EnumSet.copyOf( modifiers );
	}

	private HigherClass typeAnnotation;

	public Optional< ? extends HigherClass > typeAnnotation() {
		return Optional.ofNullable( typeAnnotation );
	}

	public void setTypeAnnotation( HigherClass typeAnnotation ) {
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
		return modifiers.contains( FINAL );
	}

	@Override
	public boolean isAbstract() {
		return modifiers.contains( ABSTRACT );
	}

	public TypeExpression extendsClass() {
		return extendsClass;
	}

	public List< TypeExpression > implementsInterfaces() {
		return implementsInterfaces;
	}

	public List< ConstructorDefinition > constructors() {
		return constructors;
	}

	public List< ClassMethodDefinition > methods() {
		return methods;
	}

	public List< Field > fields() {
		return fields;
	}

	public List< Annotation > annotations() {
		return annotations;
	}

	public Optional< TypeExpression > superClass() {
		return Optional.ofNullable( extendsClass );
	}

	@Override
	public < R > R accept( ChoralVisitorInterface< R > v ) {
		return v.visit( this );
	}

}
