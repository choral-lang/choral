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
import choral.ast.visitors.ChoralVisitorInterface;

import java.util.List;

/**
 * enum E (W1,...)<T1(W1,...),T2,...> {
 * case C1( T1 x );     	<----\  C1, C2, C3 are the name of the cases,
 * case C2( T2 y );				<------ T1, T2 are types (in cases, types
 * case C3( T1 x, T2 y ); <----/  and worlds are bound)
 * }
 */

public class CaseSignature extends Node {
	private final Name name;
	private final List< FormalMethodParameter > parameters;

	public CaseSignature( final Name name, final List< FormalMethodParameter > parameters ) {
		this.name = name;
		this.parameters = parameters;
	}

	public CaseSignature(
			final Name name, final List< FormalMethodParameter > parameters, final Position position
	) {
		super( position );
		this.name = name;
		this.parameters = parameters;
	}

	public Name name() {
		return name;
	}

	public List< FormalMethodParameter > parameters() {
		return parameters;
	}

	@Override
	public < R > R accept( ChoralVisitorInterface< R > v ) {
		return v.visit( this );
	}

}
