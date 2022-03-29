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

package choral.compiler.unitNormaliser;

import choral.ast.body.VariableDeclaration;
import choral.ast.expression.AssignExpression;
import choral.ast.expression.Expression;
import choral.ast.statement.*;
import choral.ast.visitors.AbstractChoralVisitor;
import choral.utils.Pair;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;

public class StatementsUnitNormaliser extends AbstractChoralVisitor< Statement > {

	public static Statement visitStatement( Statement n ) {
		return new StatementsUnitNormaliser().visit( n );
	}

	@Override
	public Statement visit( Statement n ) {
		return n.accept( this );
	}

	@Override
	public TryCatchStatement visit( TryCatchStatement n ) {
		return new TryCatchStatement(
				visit( n.body() ),
				n.catches().stream()
						.map( p -> new Pair<>(
								new VariableDeclaration(
										p.left().name(),
										p.left().type(),
										p.left().annotations(),
										p.left().initializer().orElse( null )
								),
								visit( p.right() ) ) )
						.collect( Collectors.toList() ),
				visit( n.continuation() )
		).copyPosition( n );
	}

	@Override
	public Statement visit( BlockStatement n ) {
		return new BlockStatement(
				visit( n.enclosedStatement() ),
				visit( n.continuation() )
		).copyPosition( n );
	}

	@Override
	public Statement visit( VariableDeclarationStatement n ) {
		return new VariableDeclarationStatement(
				n.variables().stream().map(
						v -> new VariableDeclaration(
								v.name(),
								v.type(),
								v.annotations(),
								v.initializer().isPresent() ?
										(AssignExpression) ExpressionUnitNormaliser.visitExpression(
												v.initializer().get() ) : null
						)
				).collect( Collectors.toList() ),
				visit( n.continuation() )
		).copyPosition( n );
	}

	@Override
	public Statement visit( ExpressionStatement n ) {
		Expression e = ExpressionUnitNormaliser.visitExpression( n.expression() );
		if( ExpressionUnitNormaliser.isNoop( e ) ) {
			return visit( n.continuation() );
		} else {
			return new ExpressionStatement( e, visit( n.continuation() ) ).copyPosition( n );
		}
	}

	@Override
	public Statement visit( IfStatement n ) {
		return new IfStatement(
				ExpressionUnitNormaliser.visitExpression( n.condition() ),
				visit( n.ifBranch() ),
				visit( n.elseBranch() ),
				visit( n.continuation() )
		).copyPosition( n );
	}

	@Override
	public Statement visit( SwitchStatement n ) {
		return new SwitchStatement(
				ExpressionUnitNormaliser.visitExpression( n.guard() ),
				// this should be always present
				n.cases().entrySet().stream().map( e ->
						new AbstractMap.SimpleEntry<>( e.getKey(), visit( e.getValue() ) )
				).collect( Collectors.toMap( Map.Entry::getKey, Map.Entry::getValue ) ),
				visit( n.continuation() )
		).copyPosition( n );
	}

	@Override
	public Statement visit( NilStatement n ) {
		return n;
	}

	@Override
	public Statement visit( ReturnStatement n ) {
		return new ReturnStatement(
				ExpressionUnitNormaliser.visitExpression( n.returnExpression() ),
				visit( n.continuation() )
		).copyPosition( n );
	}

}
