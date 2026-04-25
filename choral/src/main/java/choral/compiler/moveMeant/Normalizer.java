package choral.compiler.moveMeant;

import choral.ast.CompilationUnit;
import choral.ast.Name;
import choral.ast.body.*;
import choral.ast.body.Class;
import choral.ast.body.Enum;
import choral.ast.expression.*;
import choral.ast.statement.*;
import choral.ast.type.TypeExpression;
import choral.ast.type.WorldArgument;
import choral.ast.visitors.AbstractChoralVisitor;
import choral.types.*;
import choral.utils.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
 * This pass is driven purely by type annotations on the input AST; it does not
 * consult the Typer's {@code worldDependencies} side-table. It assumes every visited
 * expression carries a {@code typeAnnotation()} — the Typer satisfies this for every
 * node we visit. Primitive result types (e.g. {@code !Boolean@B} typed as
 * {@code boolean@B}) are boxed before use so hoisted variables remain reference types.
 */
public class Normalizer {

	private int seqnum;

	public Normalizer() {
	}

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
	 * A non-empty chain of statements, represented as its head ({@code first}) and
	 * its tail ({@code last}). The chain is
	 * {@code first.continuation() -> ... -> last}, or {@code first == last} for a
	 * singleton.
	 */
	private record NormalizedStmt(Statement first, Statement last) {
		/**
		 * Builds a {@link NormalizedStmt} by linking the hoist chain
		 * {@code firstHoist..lastHoist} (which may be empty) in front of {@code stmt}.
		 */
		static NormalizedStmt of( Statement firstHoist, Statement lastHoist, Statement stmt ) {
			if( firstHoist == null ) return new NormalizedStmt( stmt, stmt );
			lastHoist.dangerouslySetContinuation( stmt );
			return new NormalizedStmt( firstHoist, stmt );
		}
	}

	/**
	 * The result of normalizing an expression: a (possibly empty) hoist chain of
	 * {@code tmp = ...} variable declaration statements that must be inserted before
	 * the enclosing statement, and the rewritten expression that takes the place of
	 * the original.
	 */
	private record NormalizedExpr(Statement first, Statement last, Expression expression) {
		static NormalizedExpr unchanged( Expression e ) {
			return new NormalizedExpr( null, null, e );
		}
	}

	private class NormalizedResults< T > {
		Statement first;
		Statement last;
		List< T > results;

		NormalizedResults() {
			this.first = null;
			this.last = null;
			this.results = new ArrayList<>();
		}

		void add( Statement first, Statement last, T result ) {
			if( first != null ) {
				if( this.first == null ) {
					this.first = first;
					this.last = last;
				} else {
					this.last.dangerouslySetContinuation( first );
					this.last = last;
				}
			}
			this.results.add( result );
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
				Statement newBody = constructor.blockStatements()
						.accept( new VisitStatement( null ) ).first();
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
					newBody = method.body().get()
							.accept( new VisitStatement(
									method.signature().typeAnnotation().orElseThrow()
											.innerCallable().returnType() ) ).first();
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
	 * Normalizes a statement.
	 */
	private class VisitStatement extends AbstractChoralVisitor< NormalizedStmt > {

		private final GroundDataTypeOrVoid expectedReturnType;

		VisitStatement( GroundDataTypeOrVoid expectedReturnType ) {
			this.expectedReturnType = expectedReturnType;
		}

		@Override
		public NormalizedStmt visit( ExpressionStatement n ) {
			Expression e = n.expression();
			NormalizedExpr res = e.accept( new VisitExpression( typeOf( e ) ) );
			ExpressionStatement newStmt = new ExpressionStatement(
					res.expression(),
					visitContinuation( n.continuation() ),
					n.position() );
			return NormalizedStmt.of( res.first(), res.last(), newStmt );
		}

		@Override
		public NormalizedStmt visit( VariableDeclarationStatement n ) {
			var exprs = new NormalizedResults< VariableDeclaration >();
			for( VariableDeclaration vd : n.variables() ) {
				if( vd.initializer().isEmpty() ) {
					exprs.add( null, null, vd );
				} else {
					AssignExpression init = vd.initializer().get();
					NormalizedExpr res =
							init.accept( new VisitExpression(
									(GroundDataTypeOrVoid) vd.type().typeAnnotation().orElseThrow() ) );
					VariableDeclaration newVd = new VariableDeclaration(
							vd.name(),
							vd.type(),
							vd.annotations(),
							(AssignExpression) res.expression(),
							vd.position() );
					exprs.add( res.first, res.last, newVd );
				}
			}
			VariableDeclarationStatement newStmt = new VariableDeclarationStatement(
					exprs.results,
					visitContinuation( n.continuation() ),
					n.position() );
			return NormalizedStmt.of( exprs.first, exprs.last, newStmt );
		}

		@Override
		public NormalizedStmt visit( NilStatement n ) {
			return NormalizedStmt.of( null, null, n );
		}

		@Override
		public NormalizedStmt visit( BlockStatement n ) {
			// A block introduces a scope: hoists from the enclosed statement are spliced
			// inside the block, not lifted out of it.
			Statement enclosed = n.enclosedStatement().accept( this ).first();
			BlockStatement blk = new BlockStatement(
					enclosed,
					visitContinuation( n.continuation() ),
					n.position() );
			return NormalizedStmt.of( null, null, blk );
		}

		@Override
		public NormalizedStmt visit( IfStatement n ) {
			Expression cond = n.condition();
			NormalizedExpr res = cond.accept( new VisitExpression( typeOf( cond ) ) );
			IfStatement newStmt = new IfStatement(
					res.expression(),
					visitContinuation( n.ifBranch() ),
					visitContinuation( n.elseBranch() ),
					visitContinuation( n.continuation() ),
					n.position() );
			return NormalizedStmt.of( res.first(), res.last(), newStmt );
		}

		@Override
		public NormalizedStmt visit( SwitchStatement n ) {
			throw new UnsupportedOperationException(
					"SwitchStatement not supported\n\tStatement at " + n.position().toString() );
		}

		@Override
		public NormalizedStmt visit( TryCatchStatement n ) {
			// Try and catch bodies are scopes; their hoists stay inside. Catch
			// declarations have no initializer, so they cannot generate hoists.
			Statement body = n.body().accept( this ).first();
			List< Pair< VariableDeclaration, Statement > > newCatches = new ArrayList<>();
			for( Pair< VariableDeclaration, Statement > pair : n.catches() ) {
				newCatches.add( new Pair<>(
						pair.left(),
						pair.right().accept( this ).first() ) );
			}
			TryCatchStatement newStmt = new TryCatchStatement(
					body,
					newCatches,
					visitContinuation( n.continuation() ),
					n.position() );
			return NormalizedStmt.of( null, null, newStmt );
		}

		@Override
		public NormalizedStmt visit( ReturnStatement n ) {
			Expression e = n.returnExpression();
			NormalizedExpr res = e.accept( new VisitExpression( expectedReturnType ) );
			ReturnStatement newStmt = new ReturnStatement(
					res.expression(),
					visitContinuation( n.continuation() ),
					n.position() );
			return NormalizedStmt.of( res.first(), res.last(), newStmt );
		}

		/**
		 * Visits a continuation (which is just another statement in the same scope) and
		 * returns the head of its rebuilt chain. Returns {@code null} if no continuation.
		 */
		private Statement visitContinuation( Statement continuation ) {
			if( continuation == null ) return null;
			return continuation.accept( this ).first();
		}

	}

	/**
	 * Normalizes an expression node, given the type expected by the surrounding context.
	 * <p>
	 * Each instance is bound to one context's expected type; recursion into a child context
	 * with a different expectation is done by constructing a new {@code VisitExpression}.
	 * <p>
	 * Every {@code visit} ends with {@link #maybeHoist}: if the node's own worlds do not
	 * fit {@link #expectedType}, the node is hoisted into a fresh {@code tmp} and
	 * replaced by a {@link FieldAccessExpression}. Transparent wrappers
	 * ({@link NotExpression}, {@link EnclosedExpression}, {@link ScopedExpression}'s
	 * right side) recurse with the wrapper's own type as the child's expected type,
	 * so the wrapper hoists as a single unit rather than its child.
	 */
	private class VisitExpression extends AbstractChoralVisitor< NormalizedExpr > {

		private final GroundDataTypeOrVoid expectedType;

		VisitExpression( GroundDataTypeOrVoid expectedType ) {
			this.expectedType = box( expectedType );
		}

		VisitExpression visitor( GroundDataTypeOrVoid expectedType ) {
			if( this.expectedType == expectedType ) {
				return this;
			} else {
				return new VisitExpression( expectedType );
			}
		}

		@Override
		public NormalizedExpr visit( MethodCallExpression n ) {
			var newArgs = new NormalizedResults< Expression >();
			for( int i = 0; i < n.arguments().size(); i++ ) {
				Expression arg = n.arguments().get( i );
				NormalizedExpr res = arg.accept( visitor( paramType( n, i ) ) );
				newArgs.add( res.first, res.last, res.expression );
			}
			Expression rebuilt = new MethodCallExpression(
					n.name(), newArgs.results, n.typeArguments(), n.position() );
			rebuilt.setTypeAnnotation( n.typeAnnotation().orElseThrow() );
			( (MethodCallExpression) rebuilt ).setMethodAnnotation( n.methodAnnotation().orElseThrow() );
			return maybeHoist( newArgs.first, newArgs.last, rebuilt );
		}

		@Override
		public NormalizedExpr visit( BinaryExpression n ) {
			NormalizedExpr l = n.left().accept( visitor( typeWithWorldsOf( n.left(), expectedType ) ) );
			NormalizedExpr r = n.right().accept( visitor( typeWithWorldsOf( n.right(), expectedType ) ) );

			var newArgs = new NormalizedResults<>();
			newArgs.add( l.first, l.last, l.expression );
			newArgs.add( r.first, r.last, r.expression );

			Expression rebuilt = new BinaryExpression(
					l.expression, r.expression, n.operator(), n.position() );
			rebuilt.setTypeAnnotation( n.typeAnnotation().orElseThrow() );
			// TODO The typer should set the world of a binary expression to match the expected world
			// No need for maybeHoist: each operand is already checked against the
			// parent's expected worlds. After VariableReplacement rewrites cross-world
			// operand tmps via ch.com(...), all operands align with the destination world,
			// so the BinaryExpression itself need not be hoisted.
			return new NormalizedExpr( newArgs.first, newArgs.last, rebuilt );
		}

		@Override
		public NormalizedExpr visit( AssignExpression n ) {
			NormalizedExpr v = n.value().accept( visitor( typeOf( n.target() ) ) );
			Expression rebuilt = new AssignExpression(
					v.expression(), n.target(), n.operator(), n.position() );
			rebuilt.setTypeAnnotation( n.typeAnnotation().orElseThrow() );
			return maybeHoist( v.first(), v.last(), rebuilt );
		}

		@Override
		public NormalizedExpr visit( ClassInstantiationExpression n ) {
			var newArgs = new NormalizedResults< Expression >();
			for( int i = 0; i < n.arguments().size(); i++ ) {
				Expression arg = n.arguments().get( i );
				NormalizedExpr res = arg.accept( visitor( constructorParamType( n, i ) ) );
				newArgs.add( res.first, res.last, res.expression );
			}
			Expression rebuilt = new ClassInstantiationExpression(
					n.typeExpression(), newArgs.results, n.typeArguments(), n.position() );
			rebuilt.setTypeAnnotation( n.typeAnnotation().orElseThrow() );
			( (ClassInstantiationExpression) rebuilt )
					.setConstructorAnnotation( n.constructorAnnotation().orElseThrow() );
			return maybeHoist( newArgs.first, newArgs.last, rebuilt );
		}

		@Override
		public NormalizedExpr visit( NotExpression n ) {
			NormalizedExpr inner = n.expression().accept( visitor( typeOf( n ) ) );
			Expression rebuilt = new NotExpression( inner.expression(), n.position() );
			rebuilt.setTypeAnnotation( n.typeAnnotation().orElseThrow() );
			return maybeHoist( inner.first(), inner.last(), rebuilt );
		}

		@Override
		public NormalizedExpr visit( EnclosedExpression n ) {
			NormalizedExpr inner = n.nestedExpression().accept( visitor( typeOf( n ) ) );
			Expression rebuilt = new EnclosedExpression( inner.expression(), n.position() );
			rebuilt.setTypeAnnotation( n.typeAnnotation().orElseThrow() );
			return maybeHoist( inner.first(), inner.last(), rebuilt );
		}

		@Override
		public NormalizedExpr visit( ScopedExpression n ) {
			// TODO Not sure this is correct. Needs more tests.
			NormalizedExpr inner = n.scopedExpression().accept( visitor( typeOf( n ) ) );
			Expression rebuilt = new ScopedExpression(
					n.scope(), inner.expression(), n.position() );
			rebuilt.setTypeAnnotation( n.typeAnnotation().orElseThrow() );
			return maybeHoist( inner.first(), inner.last(), rebuilt );
		}

		@Override
		public NormalizedExpr visit( FieldAccessExpression n ) {
			// TODO How could a field access be hoisted without its scope?
			return maybeHoist( null, null, n );
		}

		@Override
		public NormalizedExpr visit( StaticAccessExpression n ) {
			// TODO What happens here? Needs tests.
			return maybeHoist( null, null, n );
		}

		@Override
		public NormalizedExpr visit( NullExpression n ) {
			return NormalizedExpr.unchanged( n );
		}

		@Override
		public NormalizedExpr visit( LiteralExpression.BooleanLiteralExpression n ) {
			// TODO What happens here? Needs tests.
			return maybeHoist( null, null, n );
		}

		@Override
		public NormalizedExpr visit( LiteralExpression.IntegerLiteralExpression n ) {
			return maybeHoist( null, null, n );
		}

		@Override
		public NormalizedExpr visit( LiteralExpression.DoubleLiteralExpression n ) {
			return maybeHoist( null, null, n );
		}

		@Override
		public NormalizedExpr visit( LiteralExpression.StringLiteralExpression n ) {
			return maybeHoist( null, null, n );
		}

		@Override
		public NormalizedExpr visit( ThisExpression n ) {
			// TODO
			throw new UnsupportedOperationException(
					"ThisExpression not supported\n\tExpression at " + n.position().toString() );
		}

		@Override
		public NormalizedExpr visit( SuperExpression n ) {
			// TODO
			throw new UnsupportedOperationException(
					"SuperExpression not supported\n\tExpression at " + n.position().toString() );
		}

		@Override
		public NormalizedExpr visit( EnumCaseInstantiationExpression n ) {
			// TODO
			throw new UnsupportedOperationException(
					"EnumCaseInstantiationExpression not supported\n\tExpression at " + n.position().toString() );
		}

		/**
		 * If {@code original} is cross-world relative to {@link #expectedWorlds}, append
		 * a fresh {@code tmp = rebuilt} hoist after the existing chain
		 * {@code (firstHoist, lastHoist)} and return a {@link FieldAccessExpression}
		 * pointing at {@code tmp}. Otherwise return {@code rebuilt} with the chain
		 * unchanged.
		 */
		private NormalizedExpr maybeHoist( Statement first, Statement last, Expression expr ) {
			if( !isCrossWorld( expr, expectedType ) ) {
				return new NormalizedExpr( first, last, expr );
			}
			Name tmp = freshTmpName();
			Statement newHoist = makeHoist( tmp, expr, expectedType );
			Statement newFirst;
			if( first == null ) {
				newFirst = newHoist;
			} else {
				last.dangerouslySetContinuation( newHoist );
				newFirst = first;
			}
			FieldAccessExpression tmpAccess = new FieldAccessExpression( tmp, expr.position() );
			tmpAccess.setTypeAnnotation( expectedType );
			return new NormalizedExpr( newFirst, newHoist, tmpAccess );
		}

		/**
		 * Builds a {@code T@W tmp = init} {@link VariableDeclarationStatement} where
		 * {@code T} and {@code W} come from the type expected by the context. The
		 * continuation is left null.
		 */
		private VariableDeclarationStatement makeHoist(
				Name tmp, Expression expr, GroundDataTypeOrVoid expectedType ) {
			FieldAccessExpression target = new FieldAccessExpression( tmp, expr.position() );
			target.setTypeAnnotation( expectedType );
			AssignExpression initializer = new AssignExpression(
					expr,
					target,
					AssignExpression.Operator.ASSIGN,
					expr.position() );
			initializer.setTypeAnnotation( expectedType );
			VariableDeclaration vd = new VariableDeclaration(
					tmp,
					getType( expectedType ),
					Collections.emptyList(),
					initializer,
					expr.position() );
			return new VariableDeclarationStatement(
					List.of( vd ), null, expr.position() );
		}
	}

	private Name freshTmpName() {
		return new Name( "tmp" + seqnum() );
	}

	/**
	 * Returns the worlds carried by {@code n}'s type annotation, or an empty list if
	 * the type is void.
	 */
	private static List< ? extends World > worldsOf( Expression n ) {
		GroundDataTypeOrVoid t = typeOf( n );
		if( t.isVoid() ) return List.of();
		return ( (GroundDataType) t ).worldArguments();
	}

	/**
	 * Returns {@code n}'s type annotation as a reference type.
	 */
	private static GroundDataTypeOrVoid typeOf( Expression n ) {
		return box( n.typeAnnotation().orElseThrow() );
	}

	private static GroundDataTypeOrVoid box( GroundDataTypeOrVoid t ) {
		if( t instanceof GroundPrimitiveDataType pt ) {
			return pt.boxedType();
		} else {
			return t;
		}
	}

	/**
	 * Returns the expected type of the {@code i}th argument of a method call: the
	 * type of that parameter in the method signature.
	 */
	private static GroundDataType paramType( MethodCallExpression n, int i ) {
		Member.GroundMethod method = n.methodAnnotation().get();
		return method.signature().parameters().get( i ).type();
	}

	private static GroundDataType constructorParamType( ClassInstantiationExpression n, int i ) {
		Member.GroundConstructor constructor = n.constructorAnnotation().get();
		return constructor.signature().parameters().get( i ).type();
	}

	/**
	 * Whether {@code child} is "cross-world" relative to the given expected type.
	 */
	private static boolean isCrossWorld( Expression child, GroundDataTypeOrVoid expectedType ) {
		if( expectedType == null || expectedType.isVoid() ) return false;
		List< ? extends World > childWorlds = worldsOf( child );
		List< ? extends World > expectedWorlds = ( (GroundDataType) expectedType ).worldArguments();
		return !childWorlds.containsAll( expectedWorlds );
	}

	private static GroundDataTypeOrVoid typeWithWorldsOf(
			Expression typedLike, GroundDataTypeOrVoid worldSource ) {
		if( worldSource == null || worldSource.isVoid() ) return typeOf( typedLike );
		GroundDataType type = (GroundDataType) typeOf( typedLike );
		List< ? extends World > worlds = ( (GroundDataType) worldSource ).worldArguments();
		if( type instanceof GroundClassOrInterface gc ) {
			return gc.typeConstructor().applyTo( worlds, gc.typeArguments() );
		}
		if( type instanceof GroundTypeParameter gtp ) {
			return gtp.typeConstructor().applyTo( worlds );
		}
		throw new IllegalStateException(
				"Unsupported type for expected context: " + type.getClass().getSimpleName() );
	}

	/**
	 * Returns {@code t} as a {@link TypeExpression}. Used to type fresh hoisted
	 * temporaries.
	 */
	private static TypeExpression getType( GroundDataTypeOrVoid t ) {
		t = box( t );
		if( t == null || t.isVoid() ) {
			throw new IllegalStateException(
					"Cannot construct type expression for void or unannotated expected type" );
		}
		return outerTypeExpr( (GroundDataType) t );
	}

	/**
	 * Outer {@code TypeExpression}: includes the type's own world annotation.
	 */
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
		// GroundPrimitiveDataType is unreachable here because typeOf boxes primitives.
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
