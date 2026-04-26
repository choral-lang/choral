package choral.compiler.moveMeant;

import choral.ast.CompilationUnit;
import choral.ast.Name;
import choral.ast.body.VariableDeclaration;
import choral.ast.expression.AssignExpression;
import choral.ast.expression.Expression;
import choral.ast.expression.FieldAccessExpression;
import choral.ast.expression.MethodCallExpression;
import choral.ast.expression.ScopedExpression;
import choral.ast.type.TypeExpression;
import choral.exceptions.CommunicationInferenceException;
import choral.types.GroundClassOrInterface;
import choral.types.GroundDataType;
import choral.types.GroundDataTypeOrVoid;
import choral.types.GroundInterface;
import choral.types.GroundPrimitiveDataType;
import choral.types.GroundReferenceType;
import choral.types.HigherReferenceType;
import choral.types.Member;
import choral.types.World;
import choral.utils.Pair;

import java.util.List;
import java.util.Map;

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
		for( Map.Entry< Member.HigherCallable, List< VariableDeclaration > > entry :
				normalized.hoistedDefinitions().entrySet() ) {
			insertCommunications( entry.getKey(), entry.getValue() );
		}
		Utils.clearAllDependencies( normalized.compilationUnit() );
		return normalized.compilationUnit();
	}

	private void insertCommunications(
			Member.HigherCallable callable, List< VariableDeclaration > hoists ) {
		for( VariableDeclaration hoist : hoists ) {
			insertCommunication( callable, hoist );
		}
	}

	private void insertCommunication(
			Member.HigherCallable callable, VariableDeclaration hoist ) {
		AssignExpression initializer = hoist.initializer().orElseThrow( () ->
				inferenceError( hoist, "Hoisted variable has no initializer" ) );
		if( initializer.operator() != AssignExpression.Operator.ASSIGN ) {
			throw inferenceError( hoist,
					"Hoisted initializer uses unsupported operator " + initializer.operator() );
		}

		Expression value = initializer.value();
		GroundDataType receiverType = dataTypeOf(
				receiverTypeAnnotation( initializer, hoist ),
				hoist,
				"receiver" );
		GroundDataType messageType = messageTypeOf( value, hoist );
		World receiver = singleWorld( receiverType, hoist, "receiver" );
		World sender = singleWorld( messageType, hoist, "sender" );

		if( sender.equals( receiver ) ) return;

		Pair< Pair< String, GroundInterface >, Member.HigherMethod > comPair =
				Utils.findComMethod( receiver, sender, messageType, callable.channels() );
		if( comPair == null ) {
			throw inferenceError( hoist,
					"No viable communication method from " + sender.identifier()
							+ " to " + receiver.identifier()
							+ " for type " + messageType );
		}

		initializer.dangerouslyUpdateValue( createComExpression(
				value,
				messageType,
				comPair.left().left(),
				comPair.left().right(),
				hoist,
				comPair.right() ) );
	}

	private static GroundDataType messageTypeOf( Expression value, VariableDeclaration hoist ) {
		if( value instanceof MethodCallExpression methodCall
				&& methodCall.methodAnnotation().isPresent() ) {
			return dataTypeOf(
					methodCall.methodAnnotation().get().returnType(),
					hoist,
					"message" );
		}
		return dataTypeOf(
				value.typeAnnotation().orElseThrow( () ->
						inferenceError( hoist, "Hoisted value has no type annotation" ) ),
				hoist,
				"message" );
	}

	private static GroundDataTypeOrVoid receiverTypeAnnotation(
			AssignExpression initializer, VariableDeclaration hoist ) {
		return initializer.typeAnnotation().orElseThrow( () ->
				inferenceError( hoist, "Hoisted initializer has no receiver type annotation" ) );
	}

	private static GroundDataType dataTypeOf(
			GroundDataTypeOrVoid type, VariableDeclaration hoist, String label ) {
		if( type == null || type.isVoid() ) {
			throw inferenceError( hoist, "Hoisted " + label + " type is void or missing" );
		}
		if( type instanceof GroundPrimitiveDataType primitive ) {
			return primitive.boxedType();
		}
		if( type instanceof GroundDataType dataType ) {
			return dataType;
		}
		throw inferenceError( hoist,
				"Hoisted " + label + " type is not a data type: "
						+ type.getClass().getSimpleName() );
	}

	private static World singleWorld(
			GroundDataType type, VariableDeclaration hoist, String label ) {
		if( type.worldArguments().size() != 1 ) {
			throw inferenceError( hoist,
					"Hoisted " + label + " type has "
							+ type.worldArguments().size()
							+ " worlds, expected 1" );
		}
		return type.worldArguments().get( 0 );
	}

	private static Expression createComExpression(
			Expression value, GroundDataType messageType, String channelIdentifier,
			GroundInterface channelType, VariableDeclaration hoist,
			Member.HigherMethod comMethod ) {
		List< ? extends HigherReferenceType > methodTypeArgs =
				methodTypeArguments( messageType, hoist, comMethod );
		Member.GroundMethod groundComMethod = comMethod.applyTo( methodTypeArgs );
		GroundDataType returnType = dataTypeOf(
				groundComMethod.returnType(), hoist, "communication return" );
		List< TypeExpression > typeArguments =
				comMethod.typeParameters().isEmpty()
						? List.of()
						: List.of( Utils.innerTypeExpression( messageType ) );
		MethodCallExpression scopedExpression = new MethodCallExpression(
				new Name( comMethod.identifier() ),
				List.of( value ),
				typeArguments,
				value.position() );
		scopedExpression.setMethodAnnotation( groundComMethod );
		scopedExpression.setTypeAnnotation( returnType );
		FieldAccessExpression scope = new FieldAccessExpression(
				new Name( channelIdentifier ), value.position() );
		scope.setTypeAnnotation( channelType );
		ScopedExpression comExpression =
				new ScopedExpression( scope, scopedExpression, value.position() );
		comExpression.setTypeAnnotation( returnType );
		return comExpression;
	}

	private static List< ? extends HigherReferenceType > methodTypeArguments(
			GroundDataType messageType, VariableDeclaration hoist,
			Member.HigherMethod comMethod ) {
		if( comMethod.typeParameters().isEmpty() ) {
			return List.of();
		}
		if( comMethod.typeParameters().size() != 1 ) {
			throw inferenceError( hoist,
					"Communication method has "
							+ comMethod.typeParameters().size()
							+ " type parameters, expected 0 or 1" );
		}
		if( messageType instanceof GroundClassOrInterface referenceType ) {
			return List.of(
					referenceType.typeConstructor()
							.partiallyApplyTo( referenceType.typeArguments() ) );
		}
		if( messageType instanceof GroundReferenceType referenceType ) {
			return List.of( referenceType.typeConstructor() );
		}
		throw inferenceError( hoist,
				"Hoisted message type cannot be used as a communication type argument: "
						+ messageType );
	}

	private static CommunicationInferenceException inferenceError(
			VariableDeclaration hoist, String message ) {
		return new CommunicationInferenceException(
				message + " for hoist " + hoist.name()
						+ hoist.initializer()
								.map( initializer -> " initialized by " + initializer.value() )
								.orElse( "" ) );
	}
}
