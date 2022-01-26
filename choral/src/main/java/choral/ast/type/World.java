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

package choral.ast.type;

import choral.ast.Name;
import choral.ast.Node;
import choral.ast.Position;
import choral.ast.WithTypeAnnotation;

import java.util.Optional;

public abstract class World extends Node implements WithTypeAnnotation< choral.types.World > {
	protected final Name name;

	protected World( final Name name ) {
		this.name = name;
	}

	protected World( final Name name, final Position position ) {
		super( position );
		this.name = name;
	}

	private choral.types.World typeAnnotation;

	public Optional< ? extends choral.types.World > typeAnnotation() {
		return Optional.ofNullable( typeAnnotation );
	}

	public void setTypeAnnotation( choral.types.World typeAnnotation ) {
		this.typeAnnotation = typeAnnotation;
	}

	public Name name() {
		return name;
	}

	@Override
	public boolean equals( final Object o ) {
		if( o instanceof World ) {
			return this.name.equals( ( (World) o ).name() );
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
