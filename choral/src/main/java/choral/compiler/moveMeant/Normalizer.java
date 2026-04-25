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
	 * The result of normalizing an expression: a list of {@code tmp = ...} variable
	 * declaration statements that must be inserted before the enclosing statement,
	 * and the rewritten expression that takes the place of the original.
	 * <p>
	 * Hoist statements appear in source/evaluation order: index 0 is emitted first.
	 */
	private record NormalizedExpr(List< Statement > hoists, Expression expression) {
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
				Statement newBody = chainStatements( constructor.blockStatements()
						.accept( new VisitStatement() ) );
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
					newBody = chainStatements(
							method.body().get().accept( new VisitStatement() ) );
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
	 * worlds for that slot; the visitor returns a {@code List<Statement>} where the
	 * last element is the rewritten statement and any preceding elements are hoists
	 * that must be spliced in immediately before it.
	 * <p>
	 * Hoists from a statement's expression slots travel up to the immediately enclosing
	 * sibling chain (where they're spliced before the statement they came from), but
	 * never past a scope boundary like {@link BlockStatement} or a catch body.
	 */
	private class VisitStatement extends AbstractChoralVisitor< List< Statement > > {

		VisitStatement() {
		}

		@Override
		public List< Statement > visit( Statement n ) {
			return n.accept( this );
		}

		@Override
		public List< Statement > visit( ExpressionStatement n ) {
			Expression e = n.expression();
			NormalizedExpr res = e.accept( new VisitExpression( worldsOf( e ) ) );
			return appended( res.hoists(), new ExpressionStatement(
					res.expression(),
					visitContinuation( n.continuation() ),
					n.position() ) );
		}

		@Override
		public List< Statement > visit( VariableDeclarationStatement n ) {
			List< Statement > hoists = new ArrayList<>();
			List< VariableDeclaration > newVariables = new ArrayList<>();
			for( VariableDeclaration vd : n.variables() ) {
				if( vd.initializer().isEmpty() ) {
					newVariables.add( vd );
				} else {
					AssignExpression init = vd.initializer().get();
					NormalizedExpr res =
							init.accept( new VisitExpression( worldsOf( init.target() ) ) );
					hoists.addAll( res.hoists() );
					newVariables.add( new VariableDeclaration(
							vd.name(),
							vd.type(),
							vd.annotations(),
							(AssignExpression) res.expression(),
							vd.position() ) );
				}
			}
			return appended( hoists, new VariableDeclarationStatement(
					newVariables,
					visitContinuation( n.continuation() ),
					n.position() ) );
		}

		@Override
		public List< Statement > visit( NilStatement n ) {
			return List.of( new NilStatement( n.position() ) );
		}

		@Override
		public List< Statement > visit( BlockStatement n ) {
			// A block introduces a scope: hoists from the enclosed statement are spliced
			// inside the block, not lifted out of it.
			Statement enclosed = chainStatements( n.enclosedStatement().accept( this ) );
			return List.of( new BlockStatement(
					enclosed,
					visitContinuation( n.continuation() ),
					n.position() ) );
		}

		@Override
		public List< Statement > visit( IfStatement n ) {
			Expression cond = n.condition();
			NormalizedExpr res = cond.accept( new VisitExpression( worldsOf( cond ) ) );
			return appended( res.hoists(), new IfStatement(
					res.expression(),
					visitContinuation( n.ifBranch() ),
					visitContinuation( n.elseBranch() ),
					visitContinuation( n.continuation() ),
					n.position() ) );
		}

		@Override
		public List< Statement > visit( SwitchStatement n ) {
			throw new UnsupportedOperationException(
					"SwitchStatement not supported\n\tStatement at " + n.position().toString() );
		}

		@Override
		public List< Statement > visit( TryCatchStatement n ) {
			// Try and catch bodies are scopes; their hoists stay inside. Catch
			// declarations have no initializer, so they cannot generate hoists.
			Statement body = chainStatements( n.body().accept( this ) );
			List< Pair< VariableDeclaration, Statement > > newCatches = new ArrayList<>();
			for( Pair< VariableDeclaration, Statement > pair : n.catches() ) {
				newCatches.add( new Pair<>(
						pair.left(),
						chainStatements( pair.right().accept( this ) ) ) );
			}
			return List.of( new TryCatchStatement(
					body,
					newCatches,
					visitContinuation( n.continuation() ),
					n.position() ) );
		}

		@Override
		public List< Statement > visit( ReturnStatement n ) {
			Expression e = n.returnExpression();
			NormalizedExpr res = e.accept( new VisitExpression( worldsOf( e ) ) );
			return appended( res.hoists(), new ReturnStatement(
					res.expression(),
					visitContinuation( n.continuation() ),
					n.position() ) );
		}

		/**
		 * Visits a continuation (which is just another statement in the same scope) and
		 * splices its hoists in front of it. Returns {@code null} if no continuation.
		 */
		private Statement visitContinuation( Statement continuation ) {
			if( continuation == null ) return null;
			return chainStatements( continuation.accept( this ) );
		}

	}

	/**
	 * Normalizes an expression node, given the worlds expected by the surrounding slot.
	 * <p>
	 * Each instance is bound to one slot's expected worlds; recursion into a child slot
	 * with a different expectation is done by constructing a new {@code VisitExpression}.
	 * <p>
	 * Every {@code visit} ends with {@link #maybeHoist}: if the node's own worlds do not
	 * fit {@link #expectedWorlds}, the node is hoisted into a fresh {@code tmp} and
	 * replaced by a {@link FieldAccessExpression}. Transparent wrappers
	 * ({@link NotExpression}, {@link EnclosedExpression}, {@link ScopedExpression}'s
	 * right side) recurse with the wrapper's own worlds as the child's expected worlds,
	 * so the wrapper hoists as a single unit rather than its child.
	 */
	private class VisitExpression extends AbstractChoralVisitor< NormalizedExpr > {

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
				NormalizedExpr argRes = arg.accept(
						new VisitExpression( paramWorlds( n, i ) ) );
				hoists.addAll( argRes.hoists() );
				newArgs.add( argRes.expression() );
			}
			Expression rebuilt = new MethodCallExpression(
					n.name(), newArgs, n.typeArguments(), n.position() );
			return maybeHoist( n, rebuilt, hoists );
		}

		@Override
		public NormalizedExpr visit( BinaryExpression n ) {
			NormalizedExpr l = n.left().accept( this );
			NormalizedExpr r = n.right().accept( this );
			List< Statement > hoists = new ArrayList<>( l.hoists() );
			hoists.addAll( r.hoists() );
			Expression rebuilt = new BinaryExpression(
					l.expression(), r.expression(), n.operator(), n.position() );
			// No need for maybeHoist: each operand is already checked against the
			// parent's expected worlds. After VariableReplacement rewrites cross-world
			// operand tmps via ch.com(...), all operands align with the destination world,
			// so the BinaryExpression itself need not be hoisted.
			return new NormalizedExpr( hoists, rebuilt );
		}

		@Override
		public NormalizedExpr visit( AssignExpression n ) {
			NormalizedExpr v = n.value().accept(
					new VisitExpression( worldsOf( n.target() ) ) );
			Expression rebuilt = new AssignExpression(
					v.expression(), n.target(), n.operator(), n.position() );
			return maybeHoist( n, rebuilt, v.hoists() );
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
			Expression rebuilt = new ClassInstantiationExpression(
					n.typeExpression(), newArgs, n.typeArguments(), n.position() );
			return maybeHoist( n, rebuilt, hoists );
		}

		@Override
		public NormalizedExpr visit( NotExpression n ) {
			// Transparent wrapper: descend with the wrapper's own worlds so the child
			// matches and we hoist the whole wrapper (not the child) at this boundary.
			NormalizedExpr inner = n.expression().accept(
					new VisitExpression( worldsOf( n ) ) );
			Expression rebuilt = new NotExpression( inner.expression(), n.position() );
			return maybeHoist( n, rebuilt, inner.hoists() );
		}

		@Override
		public NormalizedExpr visit( EnclosedExpression n ) {
			NormalizedExpr inner = n.nestedExpression().accept(
					new VisitExpression( worldsOf( n ) ) );
			Expression rebuilt = new EnclosedExpression( inner.expression(), n.position() );
			return maybeHoist( n, rebuilt, inner.hoists() );
		}

		@Override
		public NormalizedExpr visit( ScopedExpression n ) {
			// The scope (left of the dot) is preserved as-is — it can't be hoisted on
			// its own. The scoped expression descends with the ScopedExpression's own
			// worlds so the whole expression hoists at this boundary if cross-world.
			NormalizedExpr inner = n.scopedExpression().accept(
					new VisitExpression( worldsOf( n ) ) );
			Expression rebuilt = new ScopedExpression(
					n.scope(), inner.expression(), n.position() );
			return maybeHoist( n, rebuilt, inner.hoists() );
		}

		@Override
		public NormalizedExpr visit( FieldAccessExpression n ) {
			return maybeHoist( n, n, Collections.emptyList() );
		}

		@Override
		public NormalizedExpr visit( StaticAccessExpression n ) {
			return maybeHoist( n, n, Collections.emptyList() );
		}

		@Override
		public NormalizedExpr visit( NullExpression n ) {
			return NormalizedExpr.unchanged( n );
		}

		@Override
		public NormalizedExpr visit( LiteralExpression.BooleanLiteralExpression n ) {
			// Literals carry their own world annotation — they are never hoisted.
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
		 * If {@code original} is cross-world relative to {@link #expectedWorlds}, hoist
		 * {@code rebuilt} into a fresh {@code tmp = rebuilt} declaration (appended to
		 * {@code innerHoists}) and return a {@link FieldAccessExpression} pointing at
		 * {@code tmp}. Otherwise return {@code rebuilt} unchanged.
		 */
		private NormalizedExpr maybeHoist(
				Expression original, Expression rebuilt, List< Statement > innerHoists ) {
			if( !isCrossWorld( original, expectedWorlds ) ) {
				return new NormalizedExpr( innerHoists, rebuilt );
			}
			Name tmp = freshTmpName();
			List< Statement > hoists = new ArrayList<>( innerHoists );
			hoists.add( makeHoist( tmp, rebuilt, original ) );
			return new NormalizedExpr(
					hoists, new FieldAccessExpression( tmp, original.position() ) );
		}

		/**
		 * Builds a {@code T@W tmp = init} {@link VariableDeclarationStatement} where
		 * {@code T} and {@code W} come from {@code original}'s type annotation (i.e.,
		 * the sender's worlds). The continuation is left null; {@link #chainStatements}
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
	 * Chains a non-empty list of statements via continuations: returns a single
	 * statement equivalent to {@code stmts[0]; stmts[1]; ...; stmts[n-1]}, where
	 * {@code stmts[n-1]} keeps its existing continuation.
	 */
	private static Statement chainStatements( List< Statement > stmts ) {
		Statement result = stmts.get( stmts.size() - 1 );
		for( int i = stmts.size() - 2; i >= 0; i-- ) {
			result = stmts.get( i ).cloneWithContinuation( result );
		}
		return result;
	}

	/** Returns a new mutable list containing {@code prefix} followed by {@code last}. */
	private static List< Statement > appended( List< Statement > prefix, Statement last ) {
		List< Statement > result = new ArrayList<>( prefix.size() + 1 );
		result.addAll( prefix );
		result.add( last );
		return result;
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
		GroundDataTypeOrVoid t = n.typeAnnotation().orElseThrow();
		if( t instanceof GroundPrimitiveDataType pt ) {
			return pt.boxedType();
		} else {
			return t;
		}
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
