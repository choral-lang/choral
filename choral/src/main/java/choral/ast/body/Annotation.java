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

package choral.ast.body;

import choral.ast.Name;
import choral.ast.Node;
import choral.ast.Position;
import choral.ast.expression.LiteralExpression;
import choral.ast.visitors.ChoralVisitorInterface;

import java.util.Map;

public class Annotation extends Node {

	private final Name name;
	private final Map< Name, LiteralExpression > values;

	public Annotation( Name name, Map< Name, LiteralExpression > values ) {
		this.name = name;
		this.values = values;
	}


	public Annotation(
			Name name, Map< Name, LiteralExpression > values, final Position position
	) {
		super( position );
		this.name = name;
		this.values = values;
	}

	public Name getName() {
		return name;
	}

	public Map< Name, LiteralExpression > getValues() {
		return values;
	}

	@Override
	public < R > R accept( ChoralVisitorInterface< R > v ) {
		return v.visit( this );
	}

}
