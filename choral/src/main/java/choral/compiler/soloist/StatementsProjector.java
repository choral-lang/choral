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

package choral.compiler.soloist;

import choral.ast.Name;
import choral.ast.body.VariableDeclaration;
import choral.ast.expression.*;
import choral.ast.statement.*;
import choral.ast.type.TypeExpression;
import choral.ast.type.WorldArgument;
import choral.ast.visitors.AbstractSoloistProjector;
import choral.ast.visitors.PrettyPrinterVisitor;
import choral.compiler.merge.StatementsMerger;
import choral.compiler.unitNormaliser.UnitRepresentation;
import choral.types.GroundClass;
import choral.types.GroundDataType;
import choral.types.GroundDataTypeOrVoid;
import choral.types.HigherClassOrInterface;
import choral.types.HigherReferenceType;
import choral.types.HigherTypeParameter;
import choral.types.Member;
import choral.utils.Pair;
import choral.utils.Streams;

import java.util.*;
import java.util.stream.Collectors;

public class StatementsProjector extends AbstractSoloistProjector< Statement > {

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
						.map( p -> new Pair<>(
								new VariableDeclaration(
										p.left().name(),
										TypesProjector.visit(
												this.world(), p.left().type()
										).get( 0 ),
										p.left().annotations(),
										null
								),
								visit( p.right() ) ) ) // add exceptions to gamma here
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

	private GroundClass getTypeSelectionPattern( MethodCallExpression method ) {
		Optional< ? extends Member.GroundMethod > ann = method.methodAnnotation();

		if( !ann.isPresent() ) {
			throw new SoloistProjectorException(
					"The selection method's method annotation is missing: "
							+ new PrettyPrinterVisitor().visit( method ) );
		}

		GroundDataTypeOrVoid type = ann.get().returnType();

		if( !type.isClass() ) {
			throw new SoloistProjectorException(
					"The return type of a type selection method must be a class type: "
							+ new PrettyPrinterVisitor().visit( method ) );
		}

		return (GroundClass) type;
	}

	private ScopedExpression getTypeSelectionGuard( Expression scope,
			MethodCallExpression method ) {
		Optional< ? extends Member.GroundMethod > ann = method.methodAnnotation();

		if( !ann.isPresent() ) {
			throw new SoloistProjectorException(
					"The selection method's method annotation is missing: "
							+ new PrettyPrinterVisitor().visit( method ) );
		}

		List< ? extends HigherTypeParameter > params = ann.get().higherCallable().typeParameters();

		if( params.size() != 1 ) {
			throw new SoloistProjectorException(
					"Expected the selection method to have a single type parameter: "
							+ new PrettyPrinterVisitor().visit( method ) );
		}

		HigherReferenceType upper = params.get( 0 ).innerType().upperClass().typeConstructor();

		TypeExpression te =
				new TypeExpression( new Name( ( (HigherClassOrInterface) upper ).identifier() ),
						List.of(), List.of() );
		te.setTypeAnnotation( upper );

		return new ScopedExpression( scope,
				new MethodCallExpression( method.name(), method.arguments(), List.of( te ) ) );
	}

	private Optional< Pair< ScopedExpression, GroundClass > > isTypeSelectionMethodAtWorld(
			Expression e ) {
		// A type-driven selection is a `ScopedExpression`, where the scope is
		// an expression that evaluates to a channel, and the nested expression
		// is a `MethodCallExpression` that invokes a channel's type selection
		// method.
		//
		// We assume that the type selection method is a generic method whose
		// first (and only) type argument is the type of the method's first (and
		// only) argument and its return type.
		//
		// The upper bound, if any (and Object otherwise), of the single type argument is used as
		// the type argument in the rewritten selection method call that serves as the switch guard
		// at the receiver.

		if( e instanceof ScopedExpression s ) {
			if( s.scopedExpression() instanceof MethodCallExpression method ) {
				if( !method.isTypeSelect() ) {
					return Optional.empty();
				}

				GroundClass c = getTypeSelectionPattern( method );
				if( !c.worldArguments().stream().anyMatch(
						w -> this.world()
								.equals( new WorldArgument( new Name( w.identifier() ) ) ) ) ) {
					return Optional.empty();
				}

				return Optional.of( new Pair<>( getTypeSelectionGuard( s.scope(), method ), c ) );
			}
		}

		return Optional.empty();
	}

	@Override
	public Statement visit( VariableDeclarationStatement n ) {
		List< VariableDeclaration > vars = n.variables();
		if( vars.size() == 1 ) {
			VariableDeclaration var = vars.get( 0 );
			Optional< AssignExpression > init = var.initializer();

			if( init.isPresent() ) {
				Expression e = init.get().value();

				Optional< Pair< ScopedExpression, GroundClass > > ssm =
						isTypeSelectionMethodAtWorld( e );
				if( ssm.isPresent() ) {
					Map< SwitchArgument< ? >, Statement > cases = new HashMap<>();
					// NOTE: When generating code, `JavaCompiler` replaces the default
					// case of a projection-generated switch statement with a throw
					// statement. We use `NilStatement` here to simplify the merge.
					cases.put( SwitchArgument.SwitchArgumentMergeDefault.getInstance(),
							new NilStatement() );
					cases.put( new SwitchArgument.SwitchArgumentClassLabel(
							new Pair< Name, Name >( new Name(
									ssm.get().right().typeConstructor()
											.identifier(),
									n.position() ),
									var.name() ),
							n.position() ), visit( n.continuation() ) );
					return new SwitchStatement(
							ExpressionProjector.visit( this.world(), ssm.get().left() ),
							cases,
							new NilStatement() );
				}
			}
		}

		TypeExpression t = n.variables().get( 0 ).type();
		TypeExpression tp = TypesProjector.visit( this.world(), t ).get( 0 );
		if( t.worldArguments().contains( this.world() ) ) {
			return new VariableDeclarationStatement(
					n.variables().stream().map(
							v -> new VariableDeclaration(
									v.name(),
									tp,
									v.annotations(),
									v.initializer().isEmpty() ? null : (AssignExpression) ExpressionProjector.visit(
											this.world(), v.initializer().get() )
							)
					).collect( Collectors.toList() ),
					visit( n.continuation() )
			).copyPosition( n );
		}

		return new ExpressionStatement( UnitRepresentation.unitMC(
				n.variables().stream()
						.filter( v -> v.initializer().isPresent() )
						.map( v -> ExpressionProjector.visit( this.world(),
								v.initializer().get() ) )
						.collect( Collectors.toList() ), this.world()
		), visit( n.continuation() ), n.position() );
	}

	private boolean isSelectionMethodAtWorld( Expression e ) {
		if( e instanceof ScopedExpression ) {
			Pair< Expression, Expression > ht = Utils.headAndTail( (ScopedExpression) e );
			return isSelectionMethodAtWorld( ht.right() );
		}
		if( e instanceof MethodCallExpression ) {
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
			// NOTE: When generating code, `JavaCompiler` replaces the default
			// case of a projection-generated switch statement with a throw
			// statement. We use `NilStatement` here to simplify the merge.
			cases.put( SwitchArgument.SwitchArgumentMergeDefault.getInstance(), new NilStatement() );
			cases.put( new SwitchArgument.SwitchArgumentLabel(
					getSelectionMethodEnum( n.expression() ) ), visit( n.continuation() ) );
			return new SwitchStatement(
					ExpressionProjector.visit( this.world(), n.expression() ),
					cases,
					new NilStatement()
			);
		}

		Optional< Pair< ScopedExpression, GroundClass > > ssm =
				isTypeSelectionMethodAtWorld( n.expression() );
		if ( ssm.isPresent() ) {
			Map< SwitchArgument< ? >, Statement > cases = new HashMap<>();
			// NOTE: When generating code, `JavaCompiler` replaces the default
			// case of a projection-generated switch statement with a throw
			// statement. We use `NilStatement` here to simplify the merge.
			cases.put( SwitchArgument.SwitchArgumentMergeDefault.getInstance(), new NilStatement() );
			cases.put( new SwitchArgument.SwitchArgumentClassLabel(
					new Pair< Name, Name >( new Name(
							 ssm.get().right().typeConstructor().identifier(),
							n.position() ),
							new Name( "__unusedVar__", n.position() ) ),
					n.position() ), visit( n.continuation() ) );
			return new SwitchStatement(
					ExpressionProjector.visit( this.world(), ssm.get().left() ),
					cases,
					new NilStatement()
			);
		}

		return new ExpressionStatement(
				ExpressionProjector.visit( this.world(), n.expression() ),
				visit( n.continuation() )
		).copyPosition( n );
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
			List< Statement > cases = List.of( visit( n.ifBranch() ), visit( n.elseBranch() ) );
			return new BlockStatement(
					new ExpressionStatement(
							ExpressionProjector.visit( this.world(), n.condition() ),
							StatementsMerger.merge( cases ) ),
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
					).collect( Streams.toLinkedHashMap( Pair::left, Pair::right ) ),
					visit( n.continuation() )
			).copyPosition( n );
		} else {
			List< Statement > cases = n.cases().values().stream()
					.map( this::visit ).collect( Collectors.toList() );
			return new BlockStatement(
					new ExpressionStatement(
							ExpressionProjector.visit( this.world(), n.guard() ),
							StatementsMerger.merge( cases )
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
