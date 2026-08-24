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
import choral.ast.Node;
import choral.ast.Position;
import choral.ast.expression.AssignExpression;
import choral.ast.type.TypeExpression;
import choral.ast.visitors.ChoralVisitorInterface;
import choral.types.GroundDataType;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static choral.ast.body.VariableModifier.FINAL;

public class VariableDeclaration extends Node {

	private final Name name;
	private final TypeExpression type;
	private final List< Annotation > annotations;
	private final AssignExpression initializer;
	private final EnumSet< VariableModifier > modifiers;

	public VariableDeclaration(
			final Name name,
			final TypeExpression type,
			final List< Annotation > annotations,
			final AssignExpression initializer,
			final Position position
	) {
		this( name, type, annotations, initializer, EnumSet.noneOf( VariableModifier.class ), position );
	}

	public VariableDeclaration(
			final Name name,
			final TypeExpression type,
			final List< Annotation > annotations,
			final AssignExpression initializer,
			final EnumSet< VariableModifier > modifiers
	) {
		this.name = name;
		this.type = type;
		this.annotations = annotations;
		this.initializer = initializer;
		this.modifiers = copyModifiers( modifiers );
	}

	public VariableDeclaration(
			final Name name,
			final TypeExpression type,
			final List< Annotation > annotations,
			final AssignExpression initializer,
			final EnumSet< VariableModifier > modifiers,
			final Position position
	) {
		super( position );
		this.name = name;
		this.type = type;
		this.annotations = annotations;
		this.initializer = initializer;
		this.modifiers = copyModifiers( modifiers );
	}

	public Name name() {
		return name;
	}

	public TypeExpression type() {
		return type;
	}

	public List< Annotation > annotations() {
		return annotations;
	}

	public EnumSet< VariableModifier > modifiers() {
		return modifiers;
	}

	public boolean isFinal() {
		return modifiers.contains( FINAL );
	}

	public Optional< AssignExpression > initializer() {
		return Optional.ofNullable( initializer );
	}

	private GroundDataType typeAnnotation;

	public Optional< ? extends GroundDataType > typeAnnotation() {
		return Optional.ofNullable( typeAnnotation );
	}

	public void setTypeAnnotation( GroundDataType typeAnnotation ) {
		this.typeAnnotation = typeAnnotation;
	}

	private static EnumSet< VariableModifier > copyModifiers(
			EnumSet< VariableModifier > modifiers ) {
		return modifiers.isEmpty()
				? EnumSet.noneOf( VariableModifier.class )
				: EnumSet.copyOf( modifiers );
	}

	@Override
	public < R > R accept( ChoralVisitorInterface< R > v ) {
		return v.visit( this );
	}

}
