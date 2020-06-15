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

package org.choral.ast.statement;

import org.choral.ast.Node;
import org.choral.ast.Position;
import org.choral.ast.expression.Expression;
import org.choral.ast.visitors.ChoralVisitorInterface;
import org.choral.ast.visitors.MergerInterface;
import org.choral.exceptions.ChoralException;

import java.util.Map;

/**
 * switch ( Expression ) {
 * case Identifier | Literal at World ( Identifier | Literal at World )* -> block
 * case Identifier | Literal at World ( Identifier | Literal at World )* -> block
 * case default -> { block }
 * }
 */

public class SwitchStatement extends Statement {

	private final Map< SwitchArgument< ? >, Statement > cases;
	private final Expression guard;

	public SwitchStatement(
			final Expression guard, final Map< SwitchArgument< ? >, Statement > cases,
			final Statement continuation
	) {
		super( continuation );
		this.guard = guard;
		this.cases = cases;
	}

	public SwitchStatement(
			final Expression guard, final Map< SwitchArgument< ? >, Statement > cases,
			final Statement continuation, final Position position
	) {
		super( continuation, position );
		this.guard = guard;
		this.cases = cases;
	}

	public Map< SwitchArgument< ? >, Statement > cases() {
		return cases;
	}

	public Expression guard() {
		return guard;
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
					"Could not merge " + this.getClass().getSimpleName() + " with " + n.getClass().getSimpleName() );
		}
	}

	@Override
	public Statement cloneWithContinuation( Statement continuation ) {
		return new SwitchStatement(
				this.guard,
				this.cases,
				this.continuation() == null ? continuation : continuation().cloneWithContinuation(
						continuation ),
				this.position() );
	}

}
