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

package org.choral.ast.type;

import org.choral.ast.Name;
import org.choral.ast.Node;
import org.choral.ast.Position;
import org.choral.ast.WithTypeAnnotation;
import org.choral.ast.visitors.ChoralVisitorInterface;
import org.choral.types.Type;

import java.util.List;
import java.util.Optional;

/**
 * HashMap( World1, World2 )< String( World1 ), List( World2 )< Integer > >
 */

public class TypeExpression extends Node implements WithTypeAnnotation< Type > {
	private final Name name;
	private final List< WorldArgument > worlds;
	private final List< TypeExpression > parameters;
	private Type type;

	public TypeExpression(
			final Name name, final List< WorldArgument > worlds,
			final List< TypeExpression > parameters
	) {
		this.name = name;
		this.worlds = worlds;
		this.parameters = parameters;
	}

	public TypeExpression(
			final Name name, final List< WorldArgument > worlds,
			final List< TypeExpression > parameters, final Position position
	) {
		super( position );
		this.name = name;
		this.worlds = worlds;
		this.parameters = parameters;
	}

	private Type typeAnnotation;

	public Optional< ? extends Type > typeAnnotation() {
		return Optional.ofNullable( typeAnnotation );
	}

	public void setTypeAnnotation( Type typeAnnotation ) {
		this.typeAnnotation = typeAnnotation;
	}

	public Name name() {
		return name;
	}

	public List< WorldArgument > worldArguments() {
		return worlds;
	}

	public List< TypeExpression > typeArguments() {
		return parameters;
	}

	@Override
	// WARNING: used in the merge, does not check world correspondence
	public boolean equals( Object n ) {
		if( n instanceof TypeExpression && ( (TypeExpression) n ).typeArguments().size() == this.typeArguments().size() ) {
			for( int i = 0; i < this.typeArguments().size(); i++ ) {
				if( ( (TypeExpression) n ).typeArguments().get( i ).equals(
						this.typeArguments().get( i ) ) ) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public < R > R accept( ChoralVisitorInterface< R > v ) {
		return v.visit( this );
	}

}
