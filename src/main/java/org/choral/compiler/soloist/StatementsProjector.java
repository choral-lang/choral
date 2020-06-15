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

package org.choral.compiler.soloist;

import org.choral.ast.Name;
import org.choral.ast.body.VariableDeclaration;
import org.choral.ast.expression.*;
import org.choral.ast.statement.*;
import org.choral.ast.type.WorldArgument;
import org.choral.ast.visitors.AbstractSoloistProjector;
import org.choral.ast.visitors.PrettyPrinterVisitor;
import org.choral.compiler.merge.StatementsMerger;
import org.choral.types.GroundDataType;
import org.choral.utils.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class StatementsProjector extends AbstractSoloistProjector< Statement > {

	private final Name SELECT_CHANNEL_METHOD = new Name( "select" );
	public static final SwitchArgument.SwitchArgumentLabel SELECT_DEFAULT = new SwitchArgument.SwitchArgumentLabel(
			new Name( "$SELECT_DEFAULT" ) );

	private StatementsProjector( WorldArgument w ) {
		super( w );
	}

	public static Statement visit( WorldArgument w, Statement n ) {
		return ( n == null ) ? null : new StatementsProjector( w ).visit( n );
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
						.filter( p -> p.left().type().worldArguments().contains( this.world ) )
						.map( p -> {
							Pair< VariableDeclaration, Statement > r =
									new Pair< VariableDeclaration, Statement >(
											new VariableDeclaration(
													p.left().name(),
													TypesProjector.visit( this.world(),
															p.left().type() ).get( 0 ) ),
											visit( p.right() ) );
							return r;
						} ) // add exceptions to gamma here
						.collect( Collectors.toList() )
				,
				visit( n.continuation() )
		).copyPosition( n );
		// TODO: do we check that we do not project an empty try clause without catches?
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
		VariableDeclaration v = n.variables().get(
				0 ); // there is only one variable after desugaring
		if( v.type().worldArguments().contains( this.world() ) ) {
			return new VariableDeclarationStatement(
					Collections.singletonList(
							new VariableDeclaration( v.name(),
									TypesProjector.visit( this.world(), v.type() ).get( 0 ) )
					),
					visit( n.continuation() )
			).copyPosition( n );
		} else {
			return visit( n.continuation() );
		}
	}

	private boolean isSelectionMethodAtWorld( Expression e ) {
		if( e instanceof ScopedExpression ) {
			Pair< Expression, Expression > ht = Utils.headAndTail( (ScopedExpression) e );
			return isSelectionMethodAtWorld( ht.right() );
		}
		if( e instanceof MethodCallExpression ) {
//			if( ( (MethodCallExpression) e ).arguments().size() > 1 ){
//				throw new SoloistProjectorException( "Found improper expression " + new PrettyPrinterVisitor().visit( e ) + " as selection method" );
//			}
//			System.out.println( new PrettyPrinterVisitor().visit( e ) + " selectMehod: " + ( (MethodCallExpression) e ).isSelect() );
			MethodCallExpression methodCall = ( (MethodCallExpression) e );
			return methodCall.isSelect()
					&& ( (GroundDataType) methodCall.methodAnnotation().get().returnType() )
					.worldArguments().stream().map(
							w -> new WorldArgument( new Name( w.identifier() ) ) ).collect(
							Collectors.toList() ).contains( this.world() );
		}
		return false;
	}

	private Name getSelectionMethodEnum( Expression e ) {
		if( e instanceof ScopedExpression ) {
			Pair< Expression, Expression > ht = Utils.headAndTail( (ScopedExpression) e );
			return getSelectionMethodEnum( ht.right() );
		}
		if( e instanceof MethodCallExpression ) {
			if( ( (MethodCallExpression) e ).arguments().size() > 1 ) {
				throw new SoloistProjectorException(
						"Found improper expression " + new PrettyPrinterVisitor().visit(
								e ) + " as selection method" );
			}
			return getSelectionMethodEnum( ( (MethodCallExpression) e ).arguments().get( 0 ) );
		}
		if( e instanceof EnumCaseInstantiationExpression ) {
			return ( (EnumCaseInstantiationExpression) e )._case();
		}
		if( e instanceof FieldAccessExpression ) {
			return ( (FieldAccessExpression) e ).name();
		}
		throw new SoloistProjectorException(
				"Found improper expression " + new PrettyPrinterVisitor().visit(
						e ) + " as selection method " );
	}

	@Override
	public Statement visit( ExpressionStatement n ) {
		if( isSelectionMethodAtWorld( n.expression() ) ) {
			Map< SwitchArgument< ? >, Statement > cases = new HashMap<>();
			cases.put( SELECT_DEFAULT,
					new NilStatement() ); // reminder: the JavaCompiler adds a throw in place of the SELECT_DEFAULT
			cases.put( new SwitchArgument.SwitchArgumentLabel(
					getSelectionMethodEnum( n.expression() ) ), visit( n.continuation() ) );
			return new SwitchStatement(
					ExpressionProjector.visit( this.world(), n.expression() ),
					cases,
					new NilStatement()
			);
		} else {
			return new ExpressionStatement(
					ExpressionProjector.visit( this.world(), n.expression() ),
					visit( n.continuation() )
			).copyPosition( n );
		}
	}

	@Override
	public Statement visit( IfStatement n ) {
		if( ExpressionProjector.atWorld( n.condition(), this.world() ) ) {
			return new IfStatement(
					ExpressionProjector.visit( this.world, n.condition() ),
					visit( n.ifBranch() ),
					visit( n.elseBranch() ),
					visit( n.continuation() )
			).copyPosition( n );
		} else {
			return new BlockStatement(
					new ExpressionStatement(
							ExpressionProjector.visit( this.world(), n.condition() ),
							StatementsMerger.merge( Arrays.asList( visit( n.ifBranch() ),
									visit( n.elseBranch() ) ) ) ),
					visit( n.continuation() )
			).copyPosition( n );
		}
	}

	@Override
	public Statement visit( SwitchStatement n ) {
		if( ExpressionProjector.atWorld( n.guard(), this.world() ) ) {
			return new SwitchStatement(
					ExpressionProjector.visit( this.world, n.guard() ),
					n.cases().entrySet().stream().map( e ->
							new Pair<>( e.getKey(),
									visit( e.getValue() ) )
					).collect( Collectors.toMap( Pair::left, Pair::right ) ),
					visit( n.continuation() )
			).copyPosition( n );
		} else {
			return new BlockStatement(
					new ExpressionStatement(
							ExpressionProjector.visit( this.world(), n.guard() ),
							StatementsMerger.merge(
									n.cases().values().stream()
											.map( this::visit ).collect( Collectors.toList() )
							)
					),
					visit( n.continuation() )
			).copyPosition( n );
		}
	}

	@Override
	public Statement visit( NilStatement n ) {
		return n;
	}

	@Override
	public Statement visit( ReturnStatement n ) {
		return new ReturnStatement(
				ExpressionProjector.visit( this.world(), n.returnExpression() ),
				visit( n.continuation() )
		).copyPosition( n );
	}

}
