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

/**
 * Expression of the type: expression binary-operator expression, e.g.,
 * a.b && c.d
 * 155 * 33
 */

public class BinaryExpression extends Expression {

	private final Expression left, right;
	private final Operator operator;

	public BinaryExpression(
			final Expression left, final Expression right, final Operator operator
	) {
		this.left = left;
		this.right = right;
		this.operator = operator;
	}

	public BinaryExpression(
			final Expression left, final Expression right, final Operator operator,
			final Position position
	) {
		super( position );
		this.left = left;
		this.right = right;
		this.operator = operator;
	}

	@Override
	public < R > R accept( ChoralVisitorInterface< R > v ) {
		return v.visit( this );
	}

	@Override
	public < R, T extends Node > R merge( MergerInterface< R > m, T n ) {
		assert n instanceof BinaryExpression;
		return m.merge( this, (BinaryExpression) n );
	}

	public Expression left() {
		return left;
	}

	public Expression right() {
		return right;
	}

	public Operator operator() {
		return operator;
	}

	public enum Operator {

		SHORT_CIRCUITED_OR( "||" ),
		SHORT_CIRCUITED_AND( "&&" ),
		OR( "|" ),
		AND( "&" ),
		EQUALS( "==" ),
		NOT_EQUALS( "!=" ),
		LESS( "<" ),
		GREATER( ">" ),
		LESS_EQUALS( "<=" ),
		GREATER_EQUALS( ">=" ),
		PLUS( "+" ),
		MINUS( "-" ),
		MULTIPLY( "*" ),
		DIVIDE( "/" ),
		REMAINDER( "%" );

		private final String symbol;

		Operator( String symbol ) {
			this.symbol = symbol;
		}

		public static BinaryExpression.Operator getIfPresent( String name ) {
			return switch( name ) {
				case "==" -> EQUALS;
				case "!=" -> NOT_EQUALS;
				case "<" -> LESS;
				case ">" -> GREATER;
				case "<=" -> LESS_EQUALS;
				case ">=" -> GREATER_EQUALS;
				case "+" -> PLUS;
				case "-" -> MINUS;
				case "*" -> MULTIPLY;
				case "/" -> DIVIDE;
				case "%" -> REMAINDER;
				default -> throw new RuntimeException( "Unexpected operator " + name );
			};
		}

		public String symbol() {
			return symbol;
		}

	}

}
