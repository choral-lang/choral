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
import org.choral.ast.visitors.PrettyPrinterVisitor;
import org.choral.exceptions.ChoralException;

/**
 * A statement wrapping an expression
 * expressionStatement; continuation
 */

public class ExpressionStatement extends Statement {

	private final Expression expression;

	public ExpressionStatement( final Expression expression, final Statement continuation ) {
		super( continuation );
		this.expression = expression;
	}

	public ExpressionStatement(
			final Expression expression, final Statement continuation, final Position position
	) {
		super( continuation, position );
		this.expression = expression;
	}

	public Expression expression() {
		return expression;
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
					+ "error: Could not merge \n" + new PrettyPrinterVisitor().visit( this ) + "\n with " + n.getClass().getSimpleName() );
		}
	}

	@Override
	public Statement cloneWithContinuation( Statement continuation ) {
		return new ExpressionStatement(
				this.expression(),
				this.continuation() == null ? continuation : continuation().cloneWithContinuation(
						continuation ),
				this.position() );
	}
}
