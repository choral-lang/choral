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
import org.choral.ast.body.Annotation;
import org.choral.ast.expression.*;
import org.choral.ast.type.TypeExpression;
import org.choral.ast.type.WorldArgument;
import org.choral.ast.visitors.AbstractSoloistProjector;
import org.choral.ast.visitors.PrettyPrinterVisitor;
import org.choral.compiler.Compiler;
import org.choral.compiler.Logger;
import org.choral.compiler.unitNormaliser.UnitRepresentation;
import org.choral.exceptions.ChoralException;
import org.choral.types.GroundDataType;
import org.choral.types.GroundDataTypeOrVoid;
import org.choral.types.World;
import org.choral.utils.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExpressionProjector extends AbstractSoloistProjector< Expression > {

	private ExpressionProjector( WorldArgument w ) {
		super( w );
	}

	protected static Expression visit( WorldArgument w, Expression n ) {
		return new ExpressionProjector( w ).visit( n );
	}

	public static boolean atWorld( Expression e, WorldArgument w ) {
		return worlds( e ).contains( w );
	}

	@Override
	public Expression visit( Expression n ) {
		return n.accept( this );
	}

	@Override
	public Expression visit( AssignExpression n ) {
		return assignable( n.target() ) ?
				new AssignExpression( visit( n.value() ), visit( n.target() ), n.operator() )
				: new ScopedExpression( visit( n.target() ),
				new MethodCallExpression(
						UnitRepresentation.UID,
						Collections.singletonList( visit( n.value() ) ),
						Collections.emptyList()
				)
		);
	}

	private boolean assignable( Expression e ) {
//		if ( e instanceof FieldAccessExpression && atWorld( worlds( e ) ) ) {
//			return true;
//		}
//		if ( e instanceof ScopedExpression ) {
//			Pair< Expression, Expression > ht = Utils.headAndTail( ( ScopedExpression ) e );
//			boolean assignable = assignable( ht.right() );
//			return assignable;
//		}
//		return false;
		return atWorld( worlds( e ) );
	}

	@Override
	public Expression visit( BinaryExpression n ) {
		if( atWorld( worlds( n.left() ) ) && atWorld( worlds( n.right() ) ) ) {
			return new BinaryExpression( visit( n.left() ), visit( n.right() ), n.operator() );
		} else {
			ArrayList< Expression > e = new ArrayList<>();
			e.add( visit( n.left() ) );
			e.add( visit( n.right() ) );
			return UnitRepresentation.unitMC( e, this.world() );
		}
	}

	@Override
	public Expression visit( ScopedExpression n ) {
		Pair< Expression, Expression > ht = Utils.headAndTail( n );
		Optional< Expression > scopedExpression;
		scopedExpression = visitScoped( ht.right(), atWorld( worlds( ht.left() ) ) );
		if( scopedExpression.isEmpty() ) {
			Expression h = visit( ht.left() );
			return h instanceof ThisExpression ?
					UnitRepresentation.UnitFD( this.world() )
					: h;
		} else {
			return new ScopedExpression( visit( ht.left() ), scopedExpression.get() );
		}
	}

	private Optional< Expression > visitScoped( Expression e, Boolean atWorld ) {
		if( e instanceof FieldAccessExpression ) {
			return _visitScoped( (FieldAccessExpression) e );
		} else if( e instanceof MethodCallExpression ) {
			return _visitScoped( (MethodCallExpression) e, atWorld );
		} else if( e instanceof ScopedExpression ) {
			return _visitScoped( (ScopedExpression) e, atWorld );
		} else {
			throw new ChoralException(
					"Found unexpected kind of expression in scoped visit: " + e.getClass().getCanonicalName() );
		}
	}

	private Optional< Expression > _visitScoped( FieldAccessExpression n ) {
		return atWorld( n, this.world() ) ? Optional.of( n ) : Optional.empty();
	}

	private Optional< Expression > _visitScoped( MethodCallExpression n, Boolean atWorld ) {
		return Optional.of(
				new MethodCallExpression(
						atWorld ? n.name() : UnitRepresentation.UID,
						n.arguments().stream().map( this::visit ).collect( Collectors.toList() ),
						TypesProjector.visitAndCollect( this.world(), n.typeArguments() )
				)
		);
	}

	private Optional< Expression > _visitScoped( ScopedExpression n, Boolean atWorld ) {
		Pair< Expression, Expression > ht = Utils.headAndTail( n );
		Optional< Expression > scopedExpression;
		scopedExpression = visitScoped( ht.right(), atWorld && atWorld(
				worlds( ht.left() ) ) );
		Optional< Expression > scope = visitScoped( ht.left(), atWorld );
		return scope.isEmpty() ?
				scopedExpression
				: scopedExpression.isEmpty() ?
				scope
				: Optional.of( new ScopedExpression( scope.get(), scopedExpression.get() ) );
	}

	@Override
	public Expression visit( EnumCaseInstantiationExpression n ) {
		return atWorld( Collections.singletonList( n.world() ) ) ? n : UnitRepresentation.UnitFD(
				this.world() );
	}

	@Override
	public Expression visit( EnclosedExpression n ) {
		return new EnclosedExpression( visit( n.nestedExpression() ) );
	}

	@Override
	public Expression visit( FieldAccessExpression n ) {
		if( atWorld( worlds( n ) ) ) {
			return n;
		} else {
			return UnitRepresentation.UnitFD( this.world() );
		}
	}

	@Override
	public Expression visit( StaticAccessExpression n ) {
		return atWorld( n.typeExpression().worldArguments() ) ?
				new StaticAccessExpression(
						TypesProjector.visit( this.world(), n.typeExpression() ).get( 0 ) )
				: UnitRepresentation.UnitFD( this.world() );
	}

	@Override
	public Expression visit( MethodCallExpression n ) {
		return new MethodCallExpression(
				n.name(),
				n.arguments().stream().map( this::visit ).collect( Collectors.toList() ),
				TypesProjector.visitAndCollect( this.world(), n.typeArguments() ) );
	}

	@Override
	public Expression visit( ClassInstantiationExpression n ) {
		List< Expression > arguments = n.arguments().stream().map( this::visit ).collect(
				Collectors.toList() );
		if( atWorld( n.typeExpression().worldArguments() ) ) {
			GroundDataType dataType = (GroundDataType) n.typeExpression().typeAnnotation().get();
			return new ClassInstantiationExpression(
					new TypeExpression(
							new Name( Utils.getProjectionName(
									n.typeExpression().name().identifier(),
									this.world(),
									n.typeExpression().worldArguments(),
									dataType.typeConstructor().worldParameters()
											.stream().map(
											w -> new WorldArgument( new Name( w.identifier() ) ) )
											.collect( Collectors.toList() )
							) ),
							Collections.singletonList( this.world() ),
							n.typeExpression().typeArguments().stream().map( t -> {
								List< TypeExpression > l = TypesProjector.visit( this.world(), t );
								return l.size() > 0 ? l.get( 0 ) : null;
							} ).filter( Objects::nonNull ).collect( Collectors.toList() )
					),
					arguments,
					TypesProjector.visitAndCollect( this.world, n.typeArguments() )
			);
//			return new ClassInstantiationExpression(
//					new Name( Utils.getProjectionName( n.typeExpression().name().identifier(), this.world() ) ),
//					Collections.singletonList( this.world() ),
//					arguments,
//					n.typeArguments().stream().map( t -> {
//						List< TypeExpression > l = TypesProjector.visit( this.world(), t );
//						return l.size() > 0 ? l.get( 0 ) : null;
//					} ).filter( Objects::nonNull ).collect( Collectors.toList() )
//			);
		} else {
			return UnitRepresentation.unitMC( arguments, this.world() );
		}
	}

	@Override
	public Expression visit( NotExpression n ) {
		return atWorld( worlds( n.expression() ) ) ? // similar to binary expressions
				new NotExpression( visit( n.expression() ) )
				: UnitRepresentation.unitMC( Collections.singletonList( visit( n.expression() ) ),
				this.world() );
	}

	@Override
	public Expression visit( ThisExpression n ) {
		return n;
	}

	@Override
	public Expression visit( SuperExpression n ) {
		return n;
	}

//	@Override
//	public Expression visit( LiteralExpression n ) {
//		return atWorld( Collections.singletonList( n.world() ) ) ? n
//				: UnitRepresentation.UnitFD( this.world() );
//	}

	@Override
	public Expression visit( LiteralExpression.BooleanLiteralExpression n ) {
		return atWorld( Collections.singletonList( n.world() ) ) ? n
				: UnitRepresentation.UnitFD( this.world() );
	}

	@Override
	public Expression visit( LiteralExpression.DoubleLiteralExpression n ) {
		return atWorld( Collections.singletonList( n.world() ) ) ? n
				: UnitRepresentation.UnitFD( this.world() );
	}

	@Override
	public Expression visit( LiteralExpression.IntegerLiteralExpression n ) {
		return atWorld( Collections.singletonList( n.world() ) ) ? n
				: UnitRepresentation.UnitFD( this.world() );
	}

	@Override
	public Expression visit( LiteralExpression.StringLiteralExpression n ) {
		return atWorld( Collections.singletonList( n.world() ) ) ? n
				: UnitRepresentation.UnitFD( this.world() );
	}

	@Override
	public Expression visit( Annotation n ) {
		return null;
	}

	@Override
	public Expression visit( NullExpression n ) {
		return atWorld( n.worlds() ) ? new NullExpression(
				Collections.singletonList( this.world() ) )
				: UnitRepresentation.UnitFD( this.world() );
	}

	/* / / / / / / / / / / / / / / / / UTILITIES / / / / / / / / / / / / / / / / */

	private boolean atWorld( Collection< WorldArgument > w ) {
		return w.contains( this.world() );
	}

	private static List< WorldArgument > worlds( Expression e ) {
		if( e instanceof AssignExpression ) {
			return worlds( ( (AssignExpression) e ).target() );
		} else if( e instanceof BinaryExpression ) {
			return worlds( ( (BinaryExpression) e ).left() );
		} else if( e instanceof BlankExpression ) {
			throw new UnsupportedOperationException(
					"worlds( Expression e ) should not be called on a BlankExpression" );
		} else if( e instanceof ClassInstantiationExpression ) {
			return ( (ClassInstantiationExpression) e ).typeExpression().worldArguments();
		} else if( e instanceof EnclosedExpression ) {
			return worlds( ( (EnclosedExpression) e ).nestedExpression() );
		} else if( e instanceof EnumCaseInstantiationExpression ) {
			return Collections.singletonList( ( (EnumCaseInstantiationExpression) e ).world() );
		} else if( e instanceof FieldAccessExpression ) {
			List< WorldArgument > worlds = worldsFromAnnotation( e ).map(
					w -> new WorldArgument( new Name( w.identifier() ) ) )
					.collect( Collectors.toList() );
//			System.out.println( new PrettyPrinterVisitor().visit( e ) + " has worlds " +
//					worlds.stream().map( w -> w.name().identifier() ).collect( Collectors.joining( ", " ) ) ); // TODO: remove this
			return worlds;
		} else if( e instanceof LiteralExpression ) {
			return Collections.singletonList( ( (LiteralExpression) e ).world() );
		} else if( e instanceof MethodCallExpression ) {
			if( !( (MethodCallExpression) e ).methodAnnotation().get().returnType().isVoid() ) {
				GroundDataTypeOrVoid t = ( (MethodCallExpression) e ).methodAnnotation().get().returnType();
				if( !t.isVoid() ) {
					return ( (GroundDataType) t ).worldArguments().stream().map(
							w -> new WorldArgument( new Name( w.identifier() ) ) )
							.collect( Collectors.toList() );
				} else {
					return List.of();
				}
			} else {
				return Collections.emptyList();
			}
		} else if( e instanceof NotExpression ) {
			return worlds( ( (NotExpression) e ).expression() );
		} else if( e instanceof NullExpression ) {
			return ( (NullExpression) e ).worlds();
		} else if( e instanceof ScopedExpression ) {
			List< WorldArgument > worlds;
			Pair< Expression, Expression > ht = Utils.headAndTail( ( (ScopedExpression) e ) );
			worlds = worlds( ht.right() );
			return worlds;
		} else if( e instanceof StaticAccessExpression ) {
			return ( (StaticAccessExpression) e ).typeExpression().worldArguments();
		} else { // if ( e instanceof SuperExpression || e instanceof ThisExpression ) {
			List< WorldArgument > worlds;
			worlds = worldsFromAnnotation( e )
					.map( w -> new WorldArgument( new Name( w.identifier() ) ) )
					.collect( Collectors.toList() );
//			System.out.println( new PrettyPrinterVisitor().visit( e ) + " at worlds " +
//					worlds.stream().map( w -> w.name().identifier() ).collect( Collectors.joining( ", " ) ) ); // TODO: remove this
			return worlds;
		}
	}

	private static Stream< ? extends World > worldsFromAnnotation( Expression e ) {
		if( e.typeAnnotation().isPresent() && !e.typeAnnotation().get().isVoid() ) {
			return ( (GroundDataType) e.typeAnnotation().get() ).worldArguments().stream();
		} else {
			if( e.typeAnnotation().isEmpty() ) {
				System.out.println( new PrettyPrinterVisitor().visit( e ) + " is not annotated" );
			}
			return Stream.of();
		}
	}

}
