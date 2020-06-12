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

package org.choral.compiler.merge;

import org.choral.ast.body.VariableDeclaration;
import org.choral.ast.expression.EnumCaseInstantiationExpression;
import org.choral.ast.statement.*;
import org.choral.ast.visitors.AbstractMerger;
import org.choral.compiler.unitNormaliser.StatementsUnitNormaliser;
import org.choral.exceptions.ChoralException;
import org.choral.utils.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class StatementsMerger extends AbstractMerger< Statement > {

	private StatementsMerger() {
		super();
	}

	public static Statement merge( List< Statement > n ) {
		if( n.size() < 2 ){
			return StatementsUnitNormaliser.visitStatement( n.get( 0 ) );
		} else{
			return new StatementsMerger().merge(
					StatementsUnitNormaliser.visitStatement( n.get( 0 ) ),
					merge( n.subList( 1, n.size() ) ) );
		}
	}

	@Override
	public Statement merge( Statement n1, Statement n2 ) {
		try{
			return n1.merge( this, n2 );
		} catch( ChoralException e ){
			throw new MergeException( e.getMessage(), n1, n2 );
		}
	}

	@Override
	public Statement merge( BlockStatement n1, BlockStatement n2 ) {
		return new BlockStatement(
				merge( n1.enclosedStatement(), n2.enclosedStatement() ),
				merge( n1.continuation(), n2.continuation() )
		);
	}

	@Override
	public Statement merge( SelectStatement n1, SelectStatement n2 ) {
		return new SelectStatement(
				(EnumCaseInstantiationExpression) ExpressionsMerger.mergeExpressions( n1.enumConstructor(), n2.enumConstructor() ),
				ExpressionsMerger.mergeExpressions( n1.channelExpression(), n2.channelExpression() ),
				merge( n1.continuation(), n2.continuation() )
		);
	}

	@Override
	public Statement merge( ExpressionStatement n1, ExpressionStatement n2 ) {
		return new ExpressionStatement(
				ExpressionsMerger.mergeExpressions( n1.expression(), n2.expression() ),
				merge( n1.continuation(), n2.continuation() )
		);
	}


	@Override
	public Statement merge( VariableDeclarationStatement n1, VariableDeclarationStatement n2 ) {
		MergeException._assert(
				n1.variables().size() == n2.variables().size(),
				"Cannot merge variable declaration expressions due to different variable sizes: "
						+ n1.variables().size() + " and " + n2.variables().size(), n1, n2
		);
		List< VariableDeclaration > v = new ArrayList<>();
		for( int i = 0; i < n1.variables().size(); i++ ){
			v.add( VariableDeclarationMerger.mergeVariableDeclarations(
					n1.variables().get( i ),
					n2.variables().get( i ) )
			);
		}
		return new VariableDeclarationStatement( v, merge( n1.continuation(), n2.continuation() ) );
	}

	@Override
	public Statement merge( IfStatement n1, IfStatement n2 ) {
		return new IfStatement(
				ExpressionsMerger.mergeExpressions( n1.condition(), n2.condition() ),
				merge( n1.ifBranch(), n2.ifBranch() ),
				merge( n1.elseBranch(), n2.elseBranch() ),
				merge( n1.continuation(), n2.continuation() )
		);
	}

	@Override
	public Statement merge( SwitchStatement n1, SwitchStatement n2 ) {
		Map< SwitchArgument< ? >, Statement > cases = new HashMap<>();
		for( SwitchArgument key : n1.cases().keySet() ){
			// we merge the cases present also in n2
			if( n2.cases().containsKey( key ) ){
				cases.put( key, merge( n1.cases().get( key ), n2.cases().get( key ) )
				);
			} else{
				// we include the cases present only in n1
				cases.put( key, n1.cases().get( key ) );
			}
		}
		// we include the cases present only in n2
		for( SwitchArgument key : n2.cases().keySet().stream().filter( k -> !cases.containsKey( k ) ).collect( Collectors.toSet() ) ){
			cases.put( key, n2.cases().get( key ) );
		}
		return new SwitchStatement(
				ExpressionsMerger.mergeExpressions( n1.guard(), n2.guard() ),
				cases,
				merge( n1.continuation(), n2.continuation() )
		);
	}

	public TryCatchStatement merge( TryCatchStatement n1, TryCatchStatement n2 ) {
		MergeException._assert( n1.catches().size() == n2.catches().size(),
				"Cannot merge try-catch statements with different number of catch clauses: "
						+ n1.catches().size() + " and " + n2.catches().size(), n1, n2
		);
		List< Pair< VariableDeclaration, Statement > > mergedCatches = new ArrayList<>();
		for( int i = 0; i < n1.catches().size(); i++ ){
			MergeException._assert(
					n1.catches().get( i ).left().type().equals( n2.catches().get( i ).left().type() ),
					"Cannot merge try-catch statements with non-corresponding catch clauses: ", n1, n2
			);
			mergedCatches.add( new Pair<>(
					n1.catches().get( i ).left(),
					merge( n1.catches().get( i ).right(), n2.catches().get( i ).right() )
			) );
		}
		return new TryCatchStatement(
				merge( n1.body(), n2.body() ),
				mergedCatches,
				merge( n1.continuation(), n2.continuation() )
		);
	}

	@Override
	public Statement merge( NilStatement n1, NilStatement n2 ) {
		return n1;
	}

	@Override
	public Statement merge( ReturnStatement n1, ReturnStatement n2 ) {
		return new ReturnStatement(
				ExpressionsMerger.mergeExpressions( n1.returnExpression(), n2.returnExpression() ),
				merge( n1.continuation(), n2.continuation() )
		);
	}

}
