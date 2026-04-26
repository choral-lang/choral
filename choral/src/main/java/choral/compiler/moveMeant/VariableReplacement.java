package choral.compiler.moveMeant;

import choral.ast.CompilationUnit;
import choral.ast.Name;
import choral.ast.body.VariableDeclaration;
import choral.ast.expression.*;
import choral.exceptions.AstPositionedException;
import choral.exceptions.ChoralCompoundException;
import choral.exceptions.CommunicationInferenceException;
import choral.types.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Inserts data communications for Normalizer-created hoists.
 * <p>
 * This pass expects a typed compilation unit. It first normalizes cross-world
 * expressions into temporary variables, then rewrites each hoist initializer from
 * {@code T@B msg = expr@A} into {@code T@B msg = ch.<T>com(expr@A)}.
 * The returned compilation unit preserves the type annotations needed by later
 * moveMeant passes.
 */
public class VariableReplacement {

	public VariableReplacement() {
	}

	public CompilationUnit inferComms( CompilationUnit cu ) {
		Normalizer.Result normalized = new Normalizer().normalize( cu );
		var errors = new ArrayList< AstPositionedException >();

		for( var entry : normalized.hoistedDefinitions().entrySet() ) {
			var callable = entry.getKey();
			var hoistedDeclarations = entry.getValue();
			for( var hoist : hoistedDeclarations ) {
				try {
					insertCommunication( callable, hoist );
				} catch( AstPositionedException e ) {
					errors.add( e );
				}
			}
		}

		if( !errors.isEmpty() ) {
			throw new ChoralCompoundException( errors );
		}
		return normalized.compilationUnit();
	}

	/**
	 * Given a variable declaration of the form {@code T@B msg = expr}, where expr is an expression
	 * at some other world A, mutate the initializer to be {@code T@B msg = ch.<T>com(expr)}, where
	 * ch is a channel from A to B accessible in the body of the callable.
	 *
	 * @param callable the method or constructor containing the declaration.
	 * @param hoist    the declaration to rewrite.
	 */
	private void insertCommunication( Member.HigherCallable callable, VariableDeclaration hoist ) {
		var initializer = hoist.initializer().orElseThrow();
		var expr = initializer.value();

		var inType = boxed( expr.typeAnnotation().orElseThrow() );
		var outType = boxed( initializer.typeAnnotation().orElseThrow() );
		var sender = inType.worldArguments().get( 0 );
		var receiver = outType.worldArguments().get( 0 );

		if( sender.equals( receiver ) ) return;

		// Find the communication channel
		var comPair = Utils.findComMethod( receiver, sender, inType, callable.channels() );
		if( comPair == null ) {
			throw new AstPositionedException( hoist.position(),
						new CommunicationInferenceException(
								"No viable communication method from " + sender.identifier()
										+ " to " + receiver.identifier()
										+ " for type " + inType	) );
		}
		var channelName = comPair.left().left();
		var channelType = comPair.left().right();
		var comMethod = comPair.right();

		// Build the communication expression
		var methodType = comMethod.applyTo( List.of( inType.typeConstructor() ) );
		var inTypeExpr = Utils.innerTypeExpression( inType );

		// The '<T>com( expr )' part
		var scopedExpression = new MethodCallExpression( new Name( comMethod.identifier() ),
				List.of( expr ), List.of( inTypeExpr ), expr.position() );
		scopedExpression.setMethodAnnotation( methodType );
		scopedExpression.setTypeAnnotation( outType );

		// The 'ch_AB' part
		var scope = new FieldAccessExpression( new Name( channelName ), expr.position() );
		scope.setTypeAnnotation( channelType );

		// The whole 'ch_AB.com( expr )' part
		var comExpression = new ScopedExpression( scope, scopedExpression, expr.position() );
		comExpression.setTypeAnnotation( outType );

		// Update the variable declaration
		initializer.dangerouslyUpdateValue( comExpression );
	}

	private static GroundReferenceType boxed( GroundDataTypeOrVoid type ) {
		if( type instanceof GroundPrimitiveDataType primitive ) {
			return primitive.boxedType();
		}
		return (GroundReferenceType) type;
	}
}
