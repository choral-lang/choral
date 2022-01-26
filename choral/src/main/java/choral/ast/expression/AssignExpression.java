
/*
 *   Copyright (C) 2019 by Saverio Giallorenzo <saverio.giallorenzo@gmail.com>
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as
 *   published by the Free Software Foundation; either version 2 of the
 *   License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the
 *   Free Software Foundation, Inc.,
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 *   For details about the authors of this software, see the AUTHORS file.
 */

package choral.ast.expression;

import choral.ast.Node;
import choral.ast.Position;
import choral.ast.visitors.ChoralVisitorInterface;
import choral.ast.visitors.MergerInterface;

public class AssignExpression extends Expression {

	private final Expression target, value;
	private final Operator operator;

	@Override
	public < R > R accept( ChoralVisitorInterface< R > v ) {
		return v.visit( this );
	}


	@Override
	public < R, T extends Node > R merge( MergerInterface< R > m, T n ) {
		assert n instanceof AssignExpression;
		return m.merge( this, (AssignExpression) n );
	}

	public enum Operator {

		ASSIGN( "=", null ),
		ADD_ASSIGN( "+=", BinaryExpression.Operator.PLUS ),
		SUB_ASSIGN( "-=", BinaryExpression.Operator.MINUS ),
		MUL_ASSIGN( "*=", BinaryExpression.Operator.MULTIPLY ),
		DIV_ASSIGN( "/=", BinaryExpression.Operator.DIVIDE ),
		AND_ASSIGN( "&=", BinaryExpression.Operator.AND ),
		OR_ASSIGN( "|=", BinaryExpression.Operator.OR ),
		MOD_ASSIGN( "%=", BinaryExpression.Operator.REMAINDER );

		private final String symbol;

		private final BinaryExpression.Operator op;

		public boolean isLogical() {
			switch( this ) {
				case AND_ASSIGN:
				case OR_ASSIGN:
					return true;
				default:
					return false;
			}
		}

		public boolean isArithmetic() {
			switch( this ) {
				case ADD_ASSIGN:
				case SUB_ASSIGN:
				case MUL_ASSIGN:
				case DIV_ASSIGN:
				case MOD_ASSIGN:
					return true;
				default:
					return false;
			}
		}

		public String symbol() {
			return symbol;
		}

		public boolean hasOperation() {
			return this != ASSIGN;
		}

		public BinaryExpression.Operator operation() {
			return op;
		}

		Operator( final String symbol, final BinaryExpression.Operator op ) {
			this.symbol = symbol;
			this.op = op;
		}

		public static Operator getIfPresent( String name ) {
			switch( name ) {
				case "=":
					return ASSIGN;
				case "+=":
					return ADD_ASSIGN;
				case "-=":
					return SUB_ASSIGN;
				case "*=":
					return MUL_ASSIGN;
				case "/=":
					return DIV_ASSIGN;
				case "&=":
					return AND_ASSIGN;
				case "|=":
					return OR_ASSIGN;
				case "%=":
					return MOD_ASSIGN;
				default:
					throw new RuntimeException( "Unexpected operator " + name );
			}
		}

	}

	/**
	 * Expression of the type: value = target, e.g.,
	 * -> a.b = 5
	 * -> ( ( ( a ) ) ) = 5 + 1
	 */

	public AssignExpression(
			final Expression value, final Expression target, final Operator operator
	) {
		super();
		this.target = target;
		this.value = value;
		this.operator = operator;
	}

	public AssignExpression(
			final Expression value, final Expression target, final Operator operator,
			final Position position
	) {
		super( position );
		this.target = target;
		this.value = value;
		this.operator = operator;
	}

	public Expression target() {
		return target;
	}

	public Expression value() {
		return value;
	}

	public Operator operator() {
		return operator;
	}
}
