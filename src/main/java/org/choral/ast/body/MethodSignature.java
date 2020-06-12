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

import java.util.List;
import java.util.Optional;

import org.choral.ast.Name;
import org.choral.ast.Node;
import org.choral.ast.Position;
import org.choral.ast.type.FormalTypeParameter;
import org.choral.ast.type.TypeExpression;
import org.choral.ast.visitors.ChoralVisitorInterface;
import org.choral.types.Member;

public class MethodSignature extends Node {
	private final Name name;
	private final List< FormalTypeParameter > typeParameters;
	private final List< FormalMethodParameter > parameters;
	private final TypeExpression returnType;

	public MethodSignature(
			final Name name,
			final List< FormalTypeParameter > typeParameters,
			final List< FormalMethodParameter > parameters,
			final TypeExpression returnType,
			final Position position
	) {
		super( position );
		this.name = name;
		this.typeParameters = typeParameters;
		this.parameters = parameters;
		this.returnType = returnType;
	}

	public Name name() {
		return this.name;
	}

	public List< FormalMethodParameter > parameters() {
		return this.parameters;
	}

	public List< FormalTypeParameter > typeParameters() {
		return this.typeParameters;
	}

	public TypeExpression returnType() {
		return returnType;
	}


	private Member.HigherMethod typeAnnotation;

	public Optional< Member.HigherMethod > typeAnnotation() {
		return Optional.ofNullable( typeAnnotation );
	}

	public void setTypeAnnotation( Member.HigherMethod typeAnnotation ) {
		this.typeAnnotation = typeAnnotation;
	}

	@Override
	public < R > R accept( ChoralVisitorInterface< R > v ) {
		return v.visit( this );
	}

}
