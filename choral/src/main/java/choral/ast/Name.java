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

package choral.ast;

import choral.ast.visitors.ChoralVisitorInterface;
import choral.exceptions.ChoralException;

/**
 * A name that may consist of multiple identifiers (a.b.c).
 */

public class Name extends Node {

	private final String identifier;

	public Name( final String identifier ) {
		this.identifier = identifier;
	}

	public Name( final String identifier, final Position position ) {
		super( position );
		this.identifier = identifier;
	}

	public String identifier() {
		return identifier;
	}

	@Override
	public boolean equals( Object n ) {
		if( n == null ) {
			throw new ChoralException( "Undefined name comparison" );
		}
		return this.identifier().equals( ( (Name) n ).identifier() );
	}

	@Override
	public int hashCode() {
		return this.identifier().hashCode();
	}

	@Override
	public String toString() {
		return this.identifier();
	}

	@Override
	public < R > R accept( ChoralVisitorInterface< R > v ) {
		return v.visit( this );
	}

}
