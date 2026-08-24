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

package choral.ast.expression;

import choral.ast.Node;
import choral.ast.Position;
import choral.ast.type.WorldArgument;
import choral.ast.visitors.ChoralVisitorInterface;
import choral.ast.visitors.MergerInterface;
import choral.ast.visitors.PrettyPrinterVisitor;
import choral.exceptions.ChoralException;

import java.util.List;

/**
 * A wrapper for the invocation of `null`
 */

public class NullExpression extends Expression {

	private final List< WorldArgument > worlds;

	public NullExpression( List< WorldArgument > worlds ) {
		this.worlds = worlds;
	}

	public NullExpression( List< WorldArgument > worlds, final Position position ) {
		super( position );
		this.worlds = worlds;
	}

	public List< WorldArgument > worlds() {
		return worlds;
	}

	@Override
	public < R > R accept( ChoralVisitorInterface< R > v ) {
		return v.visit( this );
	}

	@Override
	public < R, T extends Node > R merge( MergerInterface< R > m, T n ) {
		try {
			return m.merge( this, ( this.getClass().cast( n ) ) );
		} catch( ClassCastException e ) {
			throw new ChoralException(
					this.position().line() + ":"
							+ this.position().column() + ":"
							+ "error: Could not merge \n" + new PrettyPrinterVisitor().visit(
							this ) + "\n with " + n.getClass().getSimpleName() );
		}
	}

}
