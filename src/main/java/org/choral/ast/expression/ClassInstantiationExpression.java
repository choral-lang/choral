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

package org.choral.ast.expression;

import org.choral.ast.Node;
import org.choral.ast.Position;
import org.choral.ast.type.TypeExpression;
import org.choral.ast.visitors.ChoralVisitorInterface;
import org.choral.ast.visitors.MergerInterface;
import org.choral.types.Member;

import java.util.List;
import java.util.Optional;

public class ClassInstantiationExpression extends InvocationExpression {

	private final TypeExpression type;

	public ClassInstantiationExpression( TypeExpression type, List< Expression > arguments, List< TypeExpression > typeArguments) {
		super( arguments, typeArguments );
		this.type = type;
	}

	public ClassInstantiationExpression( TypeExpression type, List< Expression > arguments, List< TypeExpression > typeArguments, final Position position ) {
		super( arguments, typeArguments, position );
		this.type = type;
	}

	public TypeExpression typeExpression() {
		return type;
	}

	private Member.GroundConstructor constructorAnnotation;

	public Optional< ? extends Member.GroundConstructor > constructorAnnotation() {
		return Optional.ofNullable( constructorAnnotation );
	}

	public void setConstructorAnnotation( Member.GroundConstructor methodAnnotation ) {
		this.constructorAnnotation = methodAnnotation;
	}

	@Override
	public < R > R accept( ChoralVisitorInterface< R > v ) {
		return v.visit( this );
	}

	@Override
	public < R, T extends Node > R merge( MergerInterface< R > m, T n ) {
		assert n instanceof ClassInstantiationExpression;
		return m.merge( this, (ClassInstantiationExpression) n );
	}

	;

}
