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

package choral.ast.type;

import choral.ast.Name;
import choral.ast.Node;
import choral.ast.Position;
import choral.ast.visitors.ChoralVisitorInterface;
import choral.types.HigherTypeParameter;

import java.util.List;
import java.util.Optional;

/**
 *
 */

public class FormalTypeParameter extends Node {
	private final Name name;
	private final List< FormalWorldParameter > worlds;
	private final List< TypeExpression > upperBound;

	public FormalTypeParameter(
			final Name name, final List< FormalWorldParameter > worlds,
			final List< TypeExpression > upperBound
	) {
		this.name = name;
		this.worlds = worlds;
		this.upperBound = upperBound;
	}

	public FormalTypeParameter(
			final Name name, final List< FormalWorldParameter > worlds,
			final List< TypeExpression > upperBound, final Position position
	) {
		super( position );
		this.name = name;
		this.worlds = worlds;
		this.upperBound = upperBound;
	}

	private HigherTypeParameter typeAnnotation;

	public Optional< ? extends HigherTypeParameter > typeAnnotation() {
		return Optional.ofNullable( typeAnnotation );
	}

	public void setTypeAnnotation( HigherTypeParameter typeAnnotation ) {
		this.typeAnnotation = typeAnnotation;
	}

	public List< FormalWorldParameter > worldParameters() {
		return worlds;
	}

	public List< TypeExpression > upperBound() {
		return upperBound;
	}

	/*public TypeExpression toTypeExpression(){
		return new TypeExpression(
						name, this.worlds.stream().map( FormalWorldParameter::toWorld ).collect( Collectors.toList() ),
						Collections.emptyList()
		);
	}*/

	public Name name() {
		return name;
	}

	@Override
	public < R > R accept( ChoralVisitorInterface< R > v ) {
		return v.visit( this );
	}

}
