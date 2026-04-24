package choral.compiler.moveMeant;

import java.util.ArrayList;
import java.util.List;

import choral.ast.CompilationUnit;
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
import choral.ast.visitors.AbstractChoralVisitor;
import choral.types.GroundDataType;
import choral.types.GroundDataTypeOrVoid;
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

	public Normalizer() {}

	/**
	 * Returns a normalized copy of {@code cu}.
	 */
	public CompilationUnit normalize( CompilationUnit cu ) {
		return rebuildCompilationUnit( cu );
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
	 * worlds for that slot.
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
			return new ExpressionStatement(
					e.accept( new VisitExpression( worldsOf( e ) ) ),
					visitContinuation( n.continuation() ),
					n.position() );
		}

		@Override
		public Statement visit( VariableDeclarationStatement n ) {
			List< VariableDeclaration > newVariables = new ArrayList<>();
			for( VariableDeclaration vd : n.variables() ) {
				newVariables.add( visitVariableDeclaration( vd ) );
			}
			return new VariableDeclarationStatement(
					newVariables,
					visitContinuation( n.continuation() ),
					n.position() );
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
			return new IfStatement(
					cond.accept( new VisitExpression( worldsOf( cond ) ) ),
					visitContinuation( n.ifBranch() ),
					visitContinuation( n.elseBranch() ),
					visitContinuation( n.continuation() ),
					n.position() );
		}

		@Override
		public Statement visit( SwitchStatement n ) {
			throw new UnsupportedOperationException(
					"SwitchStatement not supported\n\tStatement at " + n.position().toString() );
		}

		@Override
		public Statement visit( TryCatchStatement n ) {
			List< Pair< VariableDeclaration, Statement > > newCatches = new ArrayList<>();
			for( Pair< VariableDeclaration, Statement > pair : n.catches() ) {
				newCatches.add( new Pair<>(
						visitVariableDeclaration( pair.left() ),
						pair.right().accept( this ) ) );
			}
			return new TryCatchStatement(
					n.body().accept( this ),
					newCatches,
					visitContinuation( n.continuation() ),
					n.position() );
		}

		@Override
		public Statement visit( ReturnStatement n ) {
			Expression e = n.returnExpression();
			return new ReturnStatement(
					e.accept( new VisitExpression( worldsOf( e ) ) ),
					visitContinuation( n.continuation() ),
					n.position() );
		}

		/** Dispatches to {@link #visit(Statement)} if the continuation is non-null. */
		private Statement visitContinuation( Statement continuation ) {
			if( continuation == null ) return null;
			return continuation.accept( this );
		}

		private VariableDeclaration visitVariableDeclaration( VariableDeclaration vd ) {
			if( vd.initializer().isEmpty() ) return vd;
			AssignExpression init = vd.initializer().get();
			// The initializer's value is expected at the declared variable's worlds
			// (i.e. the worlds of the assignment target).
			AssignExpression newInit = (AssignExpression) init.accept(
					new VisitExpression( worldsOf( init.target() ) ) );
			return new VariableDeclaration(
					vd.name(),
					vd.type(),
					vd.annotations(),
					newInit,
					vd.position() );
		}
	}

	/**
	 * Normalizes an expression node, given the worlds expected by the surrounding slot.
	 * <p>
	 * Each instance is bound to one slot's expected worlds; recursion into a child slot
	 * with a different expectation is done by constructing a new {@code VisitExpression}.
	 * Transparent wrappers ({@link NotExpression}, {@link EnclosedExpression},
	 * {@link ScopedExpression}'s right side) reuse {@code this} since the child slot
	 * inherits the parent's expected worlds.
	 * <p>
	 * In later steps, when the visited node's own worlds do not match
	 * {@link #expectedWorlds}, the whole node is hoisted into a fresh {@code tmp}
	 * declaration and replaced with a {@link FieldAccessExpression} referring to that
	 * {@code tmp}. For now {@code expectedWorlds} is recorded but not acted on.
	 */
	private class VisitExpression extends AbstractChoralVisitor< Expression > {

		@SuppressWarnings( "unused" ) // activated in Step 4
		private final List< ? extends World > expectedWorlds;

		VisitExpression( List< ? extends World > expectedWorlds ) {
			this.expectedWorlds = expectedWorlds;
		}

		@Override
		public Expression visit( Expression n ) {
			return n.accept( this );
		}

		@Override
		public Expression visit( MethodCallExpression n ) {
			List< Expression > newArgs = new ArrayList<>();
			for( int i = 0; i < n.arguments().size(); i++ ) {
				Expression arg = n.arguments().get( i );
				newArgs.add( arg.accept( new VisitExpression( paramWorlds( n, i ) ) ) );
			}
			return new MethodCallExpression(
					n.name(), newArgs, n.typeArguments(), n.position() );
		}

		@Override
		public Expression visit( BinaryExpression n ) {
			// Both operands should share a world; use the left operand's worlds as the
			// expected context for both (matches Typer's homeWorlds rule).
			VisitExpression sub = new VisitExpression( worldsOf( n.left() ) );
			return new BinaryExpression(
					n.left().accept( sub ),
					n.right().accept( sub ),
					n.operator(),
					n.position() );
		}

		@Override
		public Expression visit( AssignExpression n ) {
			VisitExpression sub = new VisitExpression( worldsOf( n.target() ) );
			return new AssignExpression(
					n.value().accept( sub ),
					n.target(),
					n.operator(),
					n.position() );
		}

		@Override
		public Expression visit( ClassInstantiationExpression n ) {
			// Constructor argument slots are analogous to method-call args. Until we
			// have a per-parameter world lookup for constructors (Step 5), use the
			// instantiated type's worlds for all arguments.
			VisitExpression sub = new VisitExpression( worldsOf( n ) );
			List< Expression > newArgs = new ArrayList<>();
			for( Expression arg : n.arguments() ) {
				newArgs.add( arg.accept( sub ) );
			}
			return new ClassInstantiationExpression(
					n.typeExpression(), newArgs, n.typeArguments(), n.position() );
		}

		@Override
		public Expression visit( NotExpression n ) {
			return new NotExpression(
					n.expression().accept( this ),
					n.position() );
		}

		@Override
		public Expression visit( EnclosedExpression n ) {
			return new EnclosedExpression(
					n.nestedExpression().accept( this ),
					n.position() );
		}

		@Override
		public Expression visit( ScopedExpression n ) {
			return new ScopedExpression(
					n.scope(),
					n.scopedExpression().accept( this ),
					n.position() );
		}

		@Override
		public Expression visit( FieldAccessExpression n ) {
			return n;
		}

		@Override
		public Expression visit( StaticAccessExpression n ) {
			return n;
		}

		@Override
		public Expression visit( NullExpression n ) {
			return n;
		}

		@Override
		public Expression visit( LiteralExpression.BooleanLiteralExpression n ) {
			return n;
		}

		@Override
		public Expression visit( LiteralExpression.IntegerLiteralExpression n ) {
			return n;
		}

		@Override
		public Expression visit( LiteralExpression.DoubleLiteralExpression n ) {
			return n;
		}

		@Override
		public Expression visit( LiteralExpression.StringLiteralExpression n ) {
			return n;
		}

		@Override
		public Expression visit( ThisExpression n ) {
			throw new UnsupportedOperationException(
					"ThisExpression not supported\n\tExpression at " + n.position().toString() );
		}

		@Override
		public Expression visit( SuperExpression n ) {
			throw new UnsupportedOperationException(
					"SuperExpression not supported\n\tExpression at " + n.position().toString() );
		}

		@Override
		public Expression visit( TypeExpression n ) {
			throw new UnsupportedOperationException(
					"TypeExpression not supported\n\tExpression at " + n.position().toString() );
		}

		@Override
		public Expression visit( BlankExpression n ) {
			throw new UnsupportedOperationException(
					"BlankExpression not supported\n\tExpression at " + n.position().toString() );
		}

		@Override
		public Expression visit( EnumCaseInstantiationExpression n ) {
			throw new UnsupportedOperationException(
					"EnumCaseInstantiationExpression not supported\n\tExpression at " + n.position().toString() );
		}
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
		GroundDataTypeOrVoid t;
		if( n instanceof MethodCallExpression mc ) {
			if( mc.methodAnnotation().isEmpty() ) return List.of();
			t = mc.methodAnnotation().get().returnType();
		} else {
			if( n.typeAnnotation().isEmpty() ) return List.of();
			t = n.typeAnnotation().get();
		}
		if( t.isVoid() ) return List.of();
		return ( (GroundDataType) t ).worldArguments();
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
	@SuppressWarnings( "unused" ) // activated in Step 4
	private static boolean isCrossWorld( Expression child, List< ? extends World > expectedWorlds ) {
		List< ? extends World > childWorlds = worldsOf( child );
		return !expectedWorlds.containsAll( childWorlds );
	}
}
