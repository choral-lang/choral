package choral.compiler.moveMeant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import choral.ast.CompilationUnit;
import choral.ast.Name;
import choral.ast.body.Class;
import choral.ast.body.ClassMethodDefinition;
import choral.ast.body.ConstructorDefinition;
import choral.ast.body.Enum;
import choral.ast.body.VariableDeclaration;
import choral.ast.expression.AssignExpression;
import choral.ast.expression.BinaryExpression;
import choral.ast.expression.BlankExpression;
import choral.ast.expression.ClassInstantiationExpression;
import choral.ast.expression.EnclosedExpression;
import choral.ast.expression.EnumCaseInstantiationExpression;
import choral.ast.expression.Expression;
import choral.ast.expression.FieldAccessExpression;
import choral.ast.expression.LiteralExpression;
import choral.ast.expression.MethodCallExpression;
import choral.ast.expression.NotExpression;
import choral.ast.expression.NullExpression;
import choral.ast.expression.ScopedExpression;
import choral.ast.expression.StaticAccessExpression;
import choral.ast.expression.SuperExpression;
import choral.ast.expression.ThisExpression;
import choral.ast.statement.BlockStatement;
import choral.ast.statement.ExpressionStatement;
import choral.ast.statement.IfStatement;
import choral.ast.statement.NilStatement;
import choral.ast.statement.ReturnStatement;
import choral.ast.statement.Statement;
import choral.ast.statement.SwitchStatement;
import choral.ast.statement.TryCatchStatement;
import choral.ast.statement.VariableDeclarationStatement;
import choral.ast.type.TypeExpression;
import choral.ast.type.WorldArgument;
import choral.ast.visitors.AbstractChoralVisitor;
import choral.types.GroundClass;
import choral.types.GroundClassOrInterface;
import choral.types.GroundDataType;
import choral.types.GroundDataTypeOrVoid;
import choral.types.GroundReferenceType;
import choral.types.GroundTypeParameter;
import choral.types.Member;
import choral.types.World;
import choral.utils.Pair;

/**
 * Normalizes a typed {@link CompilationUnit} by hoisting cross-world subexpressions
 * into fresh temporary variables, producing an A-normal-form-like AST.
 * <p>
 * For example,
 * <pre>{@code
 * A_state.m( B_state.x );
 * }</pre>
 * is rewritten to
 * <pre>{@code
 * Int@B tmp0 = B_state.x;
 * A_state.m( tmp0 );
 * }</pre>
 * <p>
 * The output is intentionally not well-typed on its own: a subsequent pass is
 * responsible for wrapping each cross-world initializer in a channel communication
 * (e.g. {@code tmp0 = ch.com(B_state.x)}) so the resulting program typechecks.
 * <p>
 * This class is driven purely by type annotations on the input AST; it does not
 * consult the Typer's {@code worldDependencies} side-table.
 */
public class Normalizer {

	private int seqnum;

	public Normalizer() {}

	private int seqnum() {
		return seqnum++;
	}

	/**
	 * Returns a normalized copy of {@code cu}.
	 */
	public CompilationUnit normalize( CompilationUnit cu ) {
		return rebuildCompilationUnit( cu );
	}

	/**
	 * The result of normalizing an expression: a list of {@code tmp = ...} variable
	 * declaration statements that must be inserted before the enclosing statement,
	 * and the rewritten expression that takes the place of the original.
	 * <p>
	 * Hoist statements appear in source/evaluation order: index 0 is emitted first.
	 */
	private record NormalizedExpr( List< Statement > hoists, Expression expression ) {
		static NormalizedExpr unchanged( Expression e ) {
			return new NormalizedExpr( Collections.emptyList(), e );
		}
	}

	/**
	 * Rebuilds {@code cu} by visiting every method and constructor body. {@code Statement}s
	 * and {@code Expression}s are immutable, so rebuilding is how we insert nodes.
	 */
	private CompilationUnit rebuildCompilationUnit( CompilationUnit cu ) {
		List< Class > newClasses = new ArrayList<>();
		for( Class cls : cu.classes() ) {
			List< ConstructorDefinition > newConstructors = new ArrayList<>();
			for( ConstructorDefinition constructor : cls.constructors() ) {
				Statement newBody = constructor.blockStatements().accept( new VisitStatement() );
				newConstructors.add( new ConstructorDefinition(
						constructor.signature(),
						constructor.explicitConstructorInvocation().orElse( null ),
						newBody,
						constructor.annotations(),
						constructor.modifiers(),
						constructor.position() ) );
			}

			List< ClassMethodDefinition > newMethods = new ArrayList<>();
			for( ClassMethodDefinition method : cls.methods() ) {
				Statement newBody = null;
				if( method.body().isPresent() ) {
					newBody = method.body().get().accept( new VisitStatement() );
				}
				newMethods.add( new ClassMethodDefinition(
						method.signature(),
						newBody,
						method.annotations(),
						method.modifiers(),
						method.position() ) );
			}

			newClasses.add( new Class(
					cls.name(),
					cls.worldParameters(),
					cls.typeParameters(),
					cls.extendsClass(),
					cls.implementsInterfaces(),
					cls.fields(),
					newMethods,
					newConstructors,
					cls.annotations(),
					cls.modifiers(),
					cls.position() ) );
		}

		List< Enum > newEnums = cu.enums();

		return new CompilationUnit(
				cu.packageDeclaration(),
				cu.imports(),
				cu.interfaces(),
				newClasses,
				newEnums,
				cu.position().sourceFile() );
	}

	/**
	 * Walks a method body and returns a structurally equivalent tree. Each expression
	 * slot is normalized by a {@link VisitExpression} constructed with the expected
	 * worlds for that slot; the resulting {@link NormalizedExpr#hoists hoists} are
	 * chained in front of the statement they came from via {@link #chainHoists}.
	 */
	private class VisitStatement extends AbstractChoralVisitor< Statement > {

		VisitStatement() {}

		@Override
		public Statement visit( Statement n ) {
			return n.accept( this );
		}

		@Override
		public Statement visit( ExpressionStatement n ) {
			Expression e = n.expression();
			NormalizedExpr res = e.accept( new VisitExpression( worldsOf( e ) ) );
			Statement tail = new ExpressionStatement(
					res.expression(),
					visitContinuation( n.continuation() ),
					n.position() );
			return chainHoists( res.hoists(), tail );
		}

		@Override
		public Statement visit( VariableDeclarationStatement n ) {
			List< Statement > hoists = new ArrayList<>();
			List< VariableDeclaration > newVariables = new ArrayList<>();
			for( VariableDeclaration vd : n.variables() ) {
				newVariables.add( visitVariableDeclaration( vd, hoists ) );
			}
			Statement tail = new VariableDeclarationStatement(
					newVariables,
					visitContinuation( n.continuation() ),
					n.position() );
			return chainHoists( hoists, tail );
		}

		@Override
		public Statement visit( NilStatement n ) {
			return new NilStatement( n.position() );
		}

		@Override
		public Statement visit( BlockStatement n ) {
			return new BlockStatement(
					n.enclosedStatement().accept( this ),
					visitContinuation( n.continuation() ),
					n.position() );
		}

		@Override
		public Statement visit( IfStatement n ) {
			Expression cond = n.condition();
			NormalizedExpr res = cond.accept( new VisitExpression( worldsOf( cond ) ) );
			Statement tail = new IfStatement(
					res.expression(),
					visitContinuation( n.ifBranch() ),
					visitContinuation( n.elseBranch() ),
					visitContinuation( n.continuation() ),
					n.position() );
			return chainHoists( res.hoists(), tail );
		}

		@Override
		public Statement visit( SwitchStatement n ) {
			throw new UnsupportedOperationException(
					"SwitchStatement not supported\n\tStatement at " + n.position().toString() );
		}

		@Override
		public Statement visit( TryCatchStatement n ) {
			List< Statement > hoists = new ArrayList<>();
			List< Pair< VariableDeclaration, Statement > > newCatches = new ArrayList<>();
			for( Pair< VariableDeclaration, Statement > pair : n.catches() ) {
				newCatches.add( new Pair<>(
						visitVariableDeclaration( pair.left(), hoists ),
						pair.right().accept( this ) ) );
			}
			Statement tail = new TryCatchStatement(
					n.body().accept( this ),
					newCatches,
					visitContinuation( n.continuation() ),
					n.position() );
			return chainHoists( hoists, tail );
		}

		@Override
		public Statement visit( ReturnStatement n ) {
			Expression e = n.returnExpression();
			NormalizedExpr res = e.accept( new VisitExpression( worldsOf( e ) ) );
			Statement tail = new ReturnStatement(
					res.expression(),
					visitContinuation( n.continuation() ),
					n.position() );
			return chainHoists( res.hoists(), tail );
		}

		/** Dispatches to {@link #visit(Statement)} if the continuation is non-null. */
		private Statement visitContinuation( Statement continuation ) {
			if( continuation == null ) return null;
			return continuation.accept( this );
		}

		/**
		 * Normalizes {@code vd}'s initializer (if any) and appends any hoists generated
		 * to {@code hoists}. Returns the (possibly rewritten) declaration.
		 */
		private VariableDeclaration visitVariableDeclaration(
				VariableDeclaration vd, List< Statement > hoists ) {
			if( vd.initializer().isEmpty() ) return vd;
			AssignExpression init = vd.initializer().get();
			NormalizedExpr res = init.accept(
					new VisitExpression( worldsOf( init.target() ) ) );
			hoists.addAll( res.hoists() );
			return new VariableDeclaration(
					vd.name(),
					vd.type(),
					vd.annotations(),
					(AssignExpression) res.expression(),
					vd.position() );
		}
	}

	/**
	 * Normalizes an expression node, given the worlds expected by the surrounding slot.
	 * <p>
	 * Each instance is bound to one slot's expected worlds; recursion into a child slot
	 * with a different expectation is done by constructing a new {@code VisitExpression}.
	 * Transparent wrappers ({@link NotExpression}, {@link EnclosedExpression},
	 * {@link ScopedExpression}'s right side) reuse {@code this}.
	 * <p>
	 * Hoisting is currently performed only on cross-world method-call arguments; other
	 * imposing slots collect/forward hoists from children but do not yet hoist
	 * themselves (added in later steps).
	 */
	private class VisitExpression extends AbstractChoralVisitor< NormalizedExpr > {

		@SuppressWarnings( "unused" ) // activated for self-hoisting in Step 5+
		private final List< ? extends World > expectedWorlds;

		VisitExpression( List< ? extends World > expectedWorlds ) {
			this.expectedWorlds = expectedWorlds;
		}

		@Override
		public NormalizedExpr visit( Expression n ) {
			return n.accept( this );
		}

		@Override
		public NormalizedExpr visit( MethodCallExpression n ) {
			List< Statement > hoists = new ArrayList<>();
			List< Expression > newArgs = new ArrayList<>();
			for( int i = 0; i < n.arguments().size(); i++ ) {
				Expression arg = n.arguments().get( i );
				List< ? extends World > slotWorlds = paramWorlds( n, i );
				NormalizedExpr argRes = arg.accept( new VisitExpression( slotWorlds ) );
				hoists.addAll( argRes.hoists() );
				if( isCrossWorld( arg, slotWorlds ) ) {
					Name tmp = freshTmpName();
					hoists.add( makeHoist( tmp, argRes.expression(), arg ) );
					newArgs.add( new FieldAccessExpression( tmp, arg.position() ) );
				} else {
					newArgs.add( argRes.expression() );
				}
			}
			return new NormalizedExpr(
					hoists,
					new MethodCallExpression(
							n.name(), newArgs, n.typeArguments(), n.position() ) );
		}

		@Override
		public NormalizedExpr visit( BinaryExpression n ) {
			VisitExpression sub = new VisitExpression( worldsOf( n.left() ) );
			NormalizedExpr l = n.left().accept( sub );
			NormalizedExpr r = n.right().accept( sub );
			List< Statement > hoists = new ArrayList<>( l.hoists() );
			hoists.addAll( r.hoists() );
			return new NormalizedExpr(
					hoists,
					new BinaryExpression(
							l.expression(), r.expression(), n.operator(), n.position() ) );
		}

		@Override
		public NormalizedExpr visit( AssignExpression n ) {
			NormalizedExpr v = n.value().accept(
					new VisitExpression( worldsOf( n.target() ) ) );
			return new NormalizedExpr(
					v.hoists(),
					new AssignExpression(
							v.expression(), n.target(), n.operator(), n.position() ) );
		}

		@Override
		public NormalizedExpr visit( ClassInstantiationExpression n ) {
			VisitExpression sub = new VisitExpression( worldsOf( n ) );
			List< Statement > hoists = new ArrayList<>();
			List< Expression > newArgs = new ArrayList<>();
			for( Expression arg : n.arguments() ) {
				NormalizedExpr argRes = arg.accept( sub );
				hoists.addAll( argRes.hoists() );
				newArgs.add( argRes.expression() );
			}
			return new NormalizedExpr(
					hoists,
					new ClassInstantiationExpression(
							n.typeExpression(), newArgs, n.typeArguments(), n.position() ) );
		}

		@Override
		public NormalizedExpr visit( NotExpression n ) {
			NormalizedExpr inner = n.expression().accept( this );
			return new NormalizedExpr(
					inner.hoists(),
					new NotExpression( inner.expression(), n.position() ) );
		}

		@Override
		public NormalizedExpr visit( EnclosedExpression n ) {
			NormalizedExpr inner = n.nestedExpression().accept( this );
			return new NormalizedExpr(
					inner.hoists(),
					new EnclosedExpression( inner.expression(), n.position() ) );
		}

		@Override
		public NormalizedExpr visit( ScopedExpression n ) {
			NormalizedExpr inner = n.scopedExpression().accept( this );
			return new NormalizedExpr(
					inner.hoists(),
					new ScopedExpression( n.scope(), inner.expression(), n.position() ) );
		}

		@Override
		public NormalizedExpr visit( FieldAccessExpression n ) {
			return NormalizedExpr.unchanged( n );
		}

		@Override
		public NormalizedExpr visit( StaticAccessExpression n ) {
			return NormalizedExpr.unchanged( n );
		}

		@Override
		public NormalizedExpr visit( NullExpression n ) {
			return NormalizedExpr.unchanged( n );
		}

		@Override
		public NormalizedExpr visit( LiteralExpression.BooleanLiteralExpression n ) {
			return NormalizedExpr.unchanged( n );
		}

		@Override
		public NormalizedExpr visit( LiteralExpression.IntegerLiteralExpression n ) {
			return NormalizedExpr.unchanged( n );
		}

		@Override
		public NormalizedExpr visit( LiteralExpression.DoubleLiteralExpression n ) {
			return NormalizedExpr.unchanged( n );
		}

		@Override
		public NormalizedExpr visit( LiteralExpression.StringLiteralExpression n ) {
			return NormalizedExpr.unchanged( n );
		}

		@Override
		public NormalizedExpr visit( ThisExpression n ) {
			throw new UnsupportedOperationException(
					"ThisExpression not supported\n\tExpression at " + n.position().toString() );
		}

		@Override
		public NormalizedExpr visit( SuperExpression n ) {
			throw new UnsupportedOperationException(
					"SuperExpression not supported\n\tExpression at " + n.position().toString() );
		}

		@Override
		public NormalizedExpr visit( TypeExpression n ) {
			throw new UnsupportedOperationException(
					"TypeExpression not supported\n\tExpression at " + n.position().toString() );
		}

		@Override
		public NormalizedExpr visit( BlankExpression n ) {
			throw new UnsupportedOperationException(
					"BlankExpression not supported\n\tExpression at " + n.position().toString() );
		}

		@Override
		public NormalizedExpr visit( EnumCaseInstantiationExpression n ) {
			throw new UnsupportedOperationException(
					"EnumCaseInstantiationExpression not supported\n\tExpression at " + n.position().toString() );
		}

		/**
		 * Builds a {@code T@W tmp = init} {@link VariableDeclarationStatement} where
		 * {@code T} and {@code W} come from {@code original}'s type annotation (i.e.,
		 * the sender's worlds). The continuation is left null; {@link #chainHoists}
		 * splices it into the surrounding statement chain.
		 */
		private VariableDeclarationStatement makeHoist(
				Name tmp, Expression init, Expression original ) {
			AssignExpression initializer = new AssignExpression(
					init,
					new FieldAccessExpression( tmp, original.position() ),
					AssignExpression.Operator.ASSIGN,
					original.position() );
			VariableDeclaration vd = new VariableDeclaration(
					tmp,
					getType( original ),
					Collections.emptyList(),
					initializer,
					original.position() );
			return new VariableDeclarationStatement(
					List.of( vd ), null, original.position() );
		}
	}

	private Name freshTmpName() {
		return new Name( "tmp" + seqnum() );
	}

	/**
	 * Chains {@code hoists} (in source order) before {@code tail}: returns a statement
	 * equivalent to {@code hoists[0]; hoists[1]; ...; tail}.
	 */
	private static Statement chainHoists( List< Statement > hoists, Statement tail ) {
		Statement result = tail;
		for( int i = hoists.size() - 1; i >= 0; i-- ) {
			result = hoists.get( i ).cloneWithContinuation( result );
		}
		return result;
	}

	/**
	 * Returns the worlds of {@code n}'s value.
	 * <p>
	 * {@link MethodCallExpression} uses its {@code methodAnnotation().returnType()}
	 * instead of {@code typeAnnotation()} (typeAnnotation is not populated until
	 * {@link VariableReplacement#getSender} rewrites it). All other nodes use
	 * {@code typeAnnotation()}. Returns an empty list for void types or missing
	 * annotations.
	 */
	private static List< ? extends World > worldsOf( Expression n ) {
		GroundDataTypeOrVoid t = typeOf( n );
		if( t == null || t.isVoid() ) return List.of();
		return ( (GroundDataType) t ).worldArguments();
	}

	/** Returns {@code n}'s type annotation, or {@code null} if not annotated. */
	private static GroundDataTypeOrVoid typeOf( Expression n ) {
		if( n instanceof MethodCallExpression mc ) {
			return mc.methodAnnotation().map( m -> m.returnType() ).orElse( null );
		}
		return n.typeAnnotation().orElse( null );
	}

	/**
	 * Returns the expected worlds of the {@code i}th argument of a method call: the
	 * worlds of that parameter in the method signature.
	 * <p>
	 * Mirrors {@code Typer.getParamWorlds}.
	 */
	private static List< ? extends World > paramWorlds( MethodCallExpression n, int i ) {
		Member.GroundMethod method = n.methodAnnotation().get();
		return method.signature().parameters().get( i ).type().worldArguments();
	}

	/**
	 * Whether {@code child} is "cross-world" relative to the given expected worlds.
	 * <p>
	 * Matches {@code Typer.recordDependencies}: a child is cross-world iff its own
	 * worlds are not fully contained in the expected worlds of its slot.
	 */
	private static boolean isCrossWorld(
			Expression child, List< ? extends World > expectedWorlds ) {
		List< ? extends World > childWorlds = worldsOf( child );
		return !expectedWorlds.containsAll( childWorlds );
	}

	/**
	 * Returns {@code n}'s type as a {@link TypeExpression}, with the world annotation
	 * set to {@code n}'s own worlds (the sender's). Used to type fresh hoisted
	 * temporaries.
	 */
	private static TypeExpression getType( Expression n ) {
		GroundDataTypeOrVoid t = typeOf( n );
		if( t == null || t.isVoid() ) {
			throw new IllegalStateException(
					"Cannot construct type expression for void or unannotated expression at "
							+ n.position() );
		}
		return outerTypeExpr( (GroundDataType) t );
	}

	/** Outer {@code TypeExpression}: includes the type's own world annotation. */
	private static TypeExpression outerTypeExpr( GroundDataType t ) {
		List< WorldArgument > worldArgs = t.worldArguments().stream()
				.map( w -> new WorldArgument( new Name( w.identifier() ), null ) )
				.toList();
		if( t instanceof GroundClassOrInterface gc ) {
			List< TypeExpression > typeArgs = gc.typeArguments().stream()
					.map( ta -> innerTypeExpr( ta.applyTo( gc.worldArguments() ) ) )
					.toList();
			return new TypeExpression(
					new Name( gc.typeConstructor().identifier() ), worldArgs, typeArgs );
		}
		if( t instanceof GroundTypeParameter gtp ) {
			return new TypeExpression(
					new Name( gtp.typeConstructor().identifier() ),
					worldArgs,
					Collections.emptyList() );
		}
		throw new IllegalStateException(
				"Unsupported type for hoist: " + t.getClass().getSimpleName() );
	}

	/**
	 * Inner (nested) {@code TypeExpression}: no world annotation, matching the existing
	 * convention from {@code VariableReplacement.getTypeExpression}.
	 */
	private static TypeExpression innerTypeExpr( GroundReferenceType t ) {
		if( t instanceof GroundClass gc ) {
			List< TypeExpression > typeArgs = gc.typeArguments().stream()
					.map( ta -> innerTypeExpr( ta.applyTo( gc.worldArguments() ) ) )
					.toList();
			return new TypeExpression(
					new Name( gc.typeConstructor().identifier() ),
					Collections.emptyList(),
					typeArgs );
		}
		if( t instanceof GroundTypeParameter gtp ) {
			return new TypeExpression(
					new Name( gtp.typeConstructor().identifier() ),
					Collections.emptyList(),
					Collections.emptyList() );
		}
		throw new IllegalStateException(
				"Unsupported inner type for hoist: " + t.getClass().getSimpleName() );
	}
}
