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

package org.choral.compiler.unitNormaliser;

import org.choral.ast.expression.*;
import org.choral.ast.type.TypeExpression;
import org.choral.ast.visitors.AbstractChoralVisitor;
import org.choral.compiler.soloist.Utils;
import org.choral.exceptions.ChoralException;
import org.choral.utils.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class ExpressionUnitNormaliser extends AbstractChoralVisitor< ExpressionUnitNormaliser.EUNResult > {

	private ExpressionUnitNormaliser() {
	}

	public static Expression visitExpression( Expression n ) {
		return new ExpressionUnitNormaliser().visit( n ).expression.get(); // this is always present
	}

	public static boolean isNoop( Expression expression ) {
		// CHECK FOR id, this, and null
		if( expression instanceof FieldAccessExpression
				|| expression instanceof ThisExpression
				|| expression instanceof NullExpression
		){
			return true;
		}
		if( expression instanceof ScopedExpression ){
			Expression scope = ( (ScopedExpression) expression ).scope();
			Expression scoped = ( (ScopedExpression) expression ).scopedExpression();
			// CHECK FOR Unit.id
			if( scope instanceof StaticAccessExpression
					&& ( (StaticAccessExpression) scope ).typeExpression().name().equals( UnitRepresentation.UNIT )
					&& scoped instanceof FieldAccessExpression
					&& ( (FieldAccessExpression) scoped ).name() == ( UnitRepresentation.UID )
			){
				return true;
			}
			// CHECK FOR id.id.id....
			if( scope instanceof FieldAccessExpression && isNoop( scoped ) ){
				return true;
			}
		}
		return false;
	}

	@Override
	public EUNResult visit( Expression n ) {
		return n.accept( this );
	}

	@Override
	public EUNResult visit( ScopedExpression n ) {
		EUNResult er = _visit( n );
		return er.changed() ? visit( er.expression().get() ) : er;
	}

	private EUNResult _visit( ScopedExpression n ) {
		Pair< Expression, Expression > ht = Utils.headAndTail( n );
		if( // we check if we have Unit.id.id( Exp_1, ..., Exp_n )
				ht.left() instanceof StaticAccessExpression
						&& ( (StaticAccessExpression) ht.left() ).typeExpression().name().equals( UnitRepresentation.UNIT )
						&& ht.right() instanceof ScopedExpression
						&& ( (ScopedExpression) ht.right() ).scope() instanceof FieldAccessExpression
						&& ( (FieldAccessExpression) ( (ScopedExpression) ht.right() ).scope() ).name().equals( UnitRepresentation.UID )
						&& ( (ScopedExpression) ht.right() ).scopedExpression() instanceof MethodCallExpression
						&& ( (MethodCallExpression) ( (ScopedExpression) ht.right() ).scopedExpression() ).name().equals( UnitRepresentation.UID )
		){
			return EUNResult.changed(
					UnitRepresentation.unitMC(
							( (MethodCallExpression) ( (ScopedExpression) ht.right() ).scopedExpression() ).arguments(),
							null
					) );
		} else if( // we check if we have Unit.id( Exp ) *only one expression*
				ht.left() instanceof StaticAccessExpression
						&& ( (StaticAccessExpression) ht.left() ).typeExpression().name().equals( UnitRepresentation.UNIT )
						&& ht.right() instanceof MethodCallExpression
						&& ( (MethodCallExpression) ht.right() ).name().equals( UnitRepresentation.UID )
						&& ( (MethodCallExpression) ht.right() ).arguments().size() == 1
		){
			return EUNResult.changed( ( (MethodCallExpression) ht.right() ).arguments().get( 0 ) );
		} else{
			EUNResult scopedExpression = visitScoped( ht.right() ); // this is always present
			if( // we check if we would have Unit.[blank] and return Unit.id
					ht.left() instanceof StaticAccessExpression
							&& ( (StaticAccessExpression) ht.left() ).typeExpression().name().equals( UnitRepresentation.UNIT )
							&& scopedExpression.expression().isEmpty()
			){
				return EUNResult.unChanged( UnitRepresentation.UnitFD( null ) ); // this will stop the recursive visit because change is false
			} else{
				EUNResult headER = visit( ht.left() );
				return new EUNResult(
						headER.changed() || scopedExpression.changed(),
						Optional.of(
								scopedExpression.expression().isPresent() ?
										new ScopedExpression(
												headER.expression().get(), // safe to get, since we never remove the head of the chain
												scopedExpression.expression().get() )
										: headER.expression.get()
						)
				);
			}
		}
	}

	private Expression getScopedOrJustHead( Expression head, Optional< Expression > scoped ) {
		return scoped.isPresent() ? new ScopedExpression( head, scoped.get() ) : head;
	}

	private EUNResult visitScoped( Expression e ) {
		if( e instanceof FieldAccessExpression ){
			return _visitScoped( (FieldAccessExpression) e );
		} else if( e instanceof MethodCallExpression ){
			return _visitScoped( (MethodCallExpression) e );
		} else if( e instanceof ScopedExpression ){
			return _visitScoped( (ScopedExpression) e );
		} else{
			throw new ChoralException( "Found unexpected kind of expression in scoped visit: " + e.getClass().getCanonicalName() );
		}
	}

	private EUNResult _visitScoped( FieldAccessExpression n ) {
		return EUNResult.unChanged( n );
	}

	private EUNResult _visitScoped( MethodCallExpression n ) {
		List< EUNResult > argumentsVisit = new ArrayList<>();
		n.arguments().forEach( a -> argumentsVisit.add( visit( a ) ) );
		// either it is a non-.id call ...
		if( n.name() != UnitRepresentation.UID ){
			return new EUNResult(
					argumentsVisit.stream().anyMatch( EUNResult::changed ),
					Optional.of( new MethodCallExpression(
									n.name(),
									argumentsVisit.stream()
											.map( er -> er.expression().get() ) // these cannot be blank
											.collect( Collectors.toList() ),
									n.typeArguments()
							)
					)
			);
		} else
			// ... an empty .id call (which return [blank]) ...
			if( argumentsVisit.stream().allMatch(
					er -> er.expression().isEmpty()
							|| isNoop( er.expression().get() ) )
			){
				return new EUNResult( true, Optional.empty() );
			}
			// ... or an .id call where we can remove blank/NOOP arguments
			else{
				return new EUNResult(
						argumentsVisit.stream().anyMatch( a ->
								a.changed()
										|| a.expression().isEmpty()          // the arguments will change
										|| isNoop( a.expression().get() ) ),  // in size (we remove some)
						Optional.of(
								new MethodCallExpression(
										UnitRepresentation.UID,
										argumentsVisit.stream()
												.filter( er -> er.expression().isPresent() && !isNoop( er.expression().get() ) )
												.map( er -> er.expression().get() )
												.collect( Collectors.toList() ),
										n.typeArguments()
								)
						)
				);
			}
	}

	private EUNResult _visitScoped( ScopedExpression n ) {
		Pair< Expression, Expression > ht = Utils.headAndTail( n );
		Expression head = ht.left();
		// either it's id.Exp ...
		if( head instanceof FieldAccessExpression ){
			EUNResult er = visitScoped( ht.right() );
			return new EUNResult( er.changed(), Optional.of( getScopedOrJustHead( ht.left(), er.expression() ) ) );
		} // or id( Exp_1, ..., Exp_n ).Exp
		else{
			EUNResult headER = _visitScoped( (MethodCallExpression) head );
			EUNResult tailER = visitScoped( ht.right() );
			return new EUNResult(
					headER.changed() || tailER.changed(),
					headER.expression().isEmpty() ?
							tailER.expression
							: Optional.of( getScopedOrJustHead( headER.expression().get(), tailER.expression() ) )
			);
		}
	}

	@Override
	public EUNResult visit( EnumCaseInstantiationExpression n ) {
		return EUNResult.unChanged( n );
	}

	@Override
	public EUNResult visit( EnclosedExpression n ) {
		EUNResult er = visit( n.nestedExpression() );
		return new EUNResult( er.changed(), Optional.of( new EnclosedExpression( er.expression().get() ) ) );
	}

	@Override
	public EUNResult visit( MethodCallExpression n ) { // this also catches ClassInstantiationExpression and dispatches it
		List< EUNResult > argumentsList = n.arguments().stream().map( this::visit ).collect( Collectors.toList() );
		return new EUNResult(
				argumentsList.stream().anyMatch( EUNResult::changed ),
				Optional.of( new MethodCallExpression(
								n.name(),
								argumentsList.stream().map( er -> er.expression().get() ).collect( Collectors.toList() ),
								n.typeArguments()
						)
				)
		);
	}

	@Override
	public EUNResult visit( ClassInstantiationExpression n ) {
		List< EUNResult > argumentsList = n.arguments().stream().map( this::visit ).collect( Collectors.toList() );
		return new EUNResult(
				argumentsList.stream().anyMatch( EUNResult::changed ),
				Optional.of(
						new ClassInstantiationExpression(
								new TypeExpression(
										n.typeExpression().name(),
										n.typeExpression().worldArguments(),
										n.typeExpression().typeArguments()
								),
								argumentsList.stream().map( er -> er.expression().get() ).collect( Collectors.toList() ),
								n.typeArguments()
						)
				)
		);
	}

	@Override
	public EUNResult visit( AssignExpression n ) {
		return EUNResult.unChanged( new AssignExpression(
						visit( n.value() ).expression().get(),
						visit( n.target() ).expression().get(),
						n.operator()
				)
		);
	}

	@Override
	public EUNResult visit( BinaryExpression n ) {
		return EUNResult.unChanged( new BinaryExpression(
						visit( n.left() ).expression().get(),
						visit( n.right() ).expression().get(),
						n.operator()
				)
		);
	}

	@Override
	public EUNResult visit( NotExpression n ) {
		EUNResult er = visit( n.expression() );
		return new EUNResult( er.changed(), Optional.of( new NotExpression( er.expression().get() ) ) );
	}

	@Override
	public EUNResult visit( FieldAccessExpression n ) {
		return EUNResult.unChanged( n );
	}

	@Override
	public EUNResult visit( StaticAccessExpression n ) {
		return EUNResult.unChanged( n );
	}

	@Override
	public EUNResult visit( ThisExpression n ) {
		return EUNResult.unChanged( n );
	}
//
//	@Override
//	public EUNResult visit( LiteralExpression n ) {
//		return EUNResult.unChanged( n );
//	}


	@Override
	public EUNResult visit( LiteralExpression.BooleanLiteralExpression n ) {
		return EUNResult.unChanged( n );
	}

	@Override
	public EUNResult visit( LiteralExpression.DoubleLiteralExpression n ) {
		return EUNResult.unChanged( n );
	}

	@Override
	public EUNResult visit( LiteralExpression.IntegerLiteralExpression n ) {
		return EUNResult.unChanged( n );
	}

	@Override
	public EUNResult visit( LiteralExpression.StringLiteralExpression n ) {
		return EUNResult.unChanged( n );
	}

	/* / / / / / / / / / / / / / / / / PUBLIC UTILITIES / / / / / / / / / / / / / / / / */

	@Override
	public EUNResult visit( NullExpression n ) {
		return EUNResult.unChanged( n );
	}

	/* / / / / / / / / / / / / / / / / UTILITIES / / / / / / / / / / / / / / / / */

	static class EUNResult {
		private final Boolean changed;
		private final Optional< Expression > expression;

		private EUNResult( Boolean changed, Optional< Expression > expression ) {
			this.changed = changed;
			this.expression = expression;
		}

		private static EUNResult unChanged( Expression expression ) {
			return new EUNResult( false, Optional.of( expression ) );
		}

		private static EUNResult changed( Expression expression ) {
			return new EUNResult( true, Optional.of( expression ) );
		}

		public Boolean changed() {
			return changed;
		}

		public Optional< Expression > expression() {
			return expression;
		}

	}

}
