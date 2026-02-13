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
import choral.ast.visitors.ChoralVisitorInterface;
import choral.ast.visitors.MergerInterface;
import choral.ast.visitors.PrettyPrinterVisitor;
import choral.exceptions.ChoralException;

/**
 * A qualified access expression like {@code a.b.c}. For example, {@code System.out.println} is a
 * {@link ScopedExpression} where {@code out.println} is its "scoped expression" and {@code System}
 * is its "scope". Likewise, {@code out.println} is a {@link ScopedExpression} where {@code out} is
 * its "scope", {@code println} is its "scoped expression". In that case, {@code println} would be
 * a {@link MethodCallExpression}. (If {@code println} were a field instead of a method, it would be
 * a {@link FieldAccessExpression} instead).
 */
public class ScopedExpression extends Expression {

	final private Expression scope, scopedExpression;

	public ScopedExpression( final Expression scope, final Expression scopedExpression ) {
		this.scope = scope;
		this.scopedExpression = scopedExpression;
	}

	public ScopedExpression(
			final Expression scope, final Expression scopedExpression, final Position position
	) {
		super( position );
		this.scope = scope;
		this.scopedExpression = scopedExpression;
	}

	public Expression scope() {
		return scope;
	}

	public Expression scopedExpression() {
		return scopedExpression;
	}

	@Override
	public String toString(){
		return scope + "." + scopedExpression;
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
