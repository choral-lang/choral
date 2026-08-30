package choral.compiler.typer;

import choral.ast.Node;
import choral.ast.body.ClassMethodDefinition;
import choral.ast.body.ConstructorDefinition;
import choral.ast.body.TemplateDeclaration;
import choral.ast.body.MethodDefinition;
import choral.ast.body.VariableDeclaration;
import choral.ast.expression.*;
import choral.ast.statement.Statement;
import choral.ast.type.TypeExpression;
import choral.exceptions.AstPositionedException;
import choral.exceptions.StaticVerificationException;
import choral.types.*;
import choral.types.Universe.PrimitiveTypeTag;
import choral.types.Universe.SpecialTypeTag;
import choral.utils.Formatting;

import java.util.List;
import java.util.stream.Collectors;

public final class TypeError {
	private TypeError() {
	}

	private static void raise( Node node, String message ) {
		throw new AstPositionedException(
				node.position(), new StaticVerificationException( message ) );
	}

	public static void cyclicInheritance(
			Node node, GroundClassOrInterface type, GroundClassOrInterface supertype
	) {
		raise( node,
				"Cyclic inheritance: '" + type + "' cannot extend '" + supertype + "'" );
	}

	public static void abstractMethodInConcreteClass( ClassMethodDefinition node ) {
		raise( node, "abstract method in non-abstract class" );
	}

	public static void parameterUsesVar( VariableDeclaration node ) {
		raise( node, "parameters cannot use the var keyword" );
	}

	public static void illegalVoidInstantiation( TypeExpression node ) {
		raise( node, "illegal type instantiation, expected 0 role and 0 type arguments" );
	}

	public static void invalidEnumRoles( Node node ) {
		raise( node, "enums must have exactly one role" );
	}

	public static void selectionMethodTypeParameterCount( Node node, int found ) {
		raise( node, "illegal selection method, expected 1 type parameter, found " + found );
	}

	public static void selectionMethodTypeParameterBound( Node node ) {
		raise( node,
				"illegal selection method, the type parameter must be bounded exactly by '"
						+ SpecialTypeTag.ENUM + "'" );
	}

	public static void selectionMethodParameterCount( Node node, int found ) {
		raise( node,
				"illegal selection method, expected 1 method parameter, found " + found );
	}

	public static void selectionMethodParameterOneOfType(
			Node node, GroundReferenceType expected, List< ? extends World > worlds,
			GroundDataType found
	) {
		raise( node,
				"illegal selection method, expected a method parameter of type '" + expected
						+ "' for some " + Formatting.joiningQuotedAndOxfordComma( worlds )
						+ ", found one of type '" + found + "'" );
	}

	public static void selectionMethodParameterType(
			Node node, GroundReferenceType expected, List< ? extends World > worlds,
			GroundDataType found
	) {
		raise( node,
				"illegal selection method, expected a method parameter of type '" + expected
						+ "' for some " + Formatting.joiningQuotedAndOxfordComma( worlds )
						+ ", found '" + found + "'" );
	}

	public static void selectionMethodVoidReturn(
			Node node, GroundReferenceType expected, List< ? extends World > worlds
	) {
		raise( node,
				"illegal selection method, expected return type '" + expected + "' for some "
						+ Formatting.joiningQuotedAndOxfordComma( worlds ) + ", found 'void'" );
	}

	public static void selectionMethodRolesNotDistinct( Node node ) {
		raise( node,
				"illegal selection method, roles of the method parameter and return type must be distinct" );
	}

	public static void publicTypeFileName(
			TemplateDeclaration node, String family, String name, String extension
	) {
		raise( node, family + " '" + name
				+ "' is public, should be declared in a file named '" + name + extension + "'" );
	}

	public static void recursiveConstructorInvocation( MethodCallExpression node ) {
		raise( node, "recursive constructor invocation" );
	}

	public static void abstractMethodHasBody( Statement node ) {
		raise( node, "abstract methods cannot have bodies" );
	}

	public static void concreteMethodMissingBody( MethodDefinition node ) {
		raise( node, "non-abstract methods must have bodies" );
	}

	public static void missingReturnStatement( Statement node ) {
		raise( node, "missing return statement" );
	}

	public static void missingDefaultConstructor(
			ConstructorDefinition node, GroundClass type
	) {
		raise( node, "there is no default constructor available in '" + type + "'" );
	}

	public static void unresolvedSuper(
			MethodCallExpression node, HigherClassOrInterface type
	) {
		raise( node,
				"cannot resolve 'super', class '" + type + "' does not extend any class" );
	}

	public static void unresolvedConstructor(
			Node node, GroundClass type, List< ? extends GroundDataType > arguments
	) {
		raise( node, "cannot resolve constructor '" + type + arguments.stream()
				.map( Object::toString ).collect( Formatting.joining( ",", "(", ")", "" ) ) + "'" );
	}

	public static void ambiguousConstructor(
			Node node, GroundClass type, List< ? extends Member.GroundCallable > candidates
	) {
		raise( node,
				"ambiguous constructor invocation, " + candidates.stream().map( x -> "'" + type
						+ x.signature().parameters().stream().map( y -> y.type().toString() )
								.collect( Formatting.joining( ",", "(", ")", "" ) )
						+ "'" )
						.collect( Collectors.collectingAndThen( Collectors.toList(),
								Formatting.joiningOxfordComma() ) ) );
	}

	public static void requiredDataTypeForVoid( Expression node ) {
		raise( node, "data type expected, found 'void'" );
	}

	public static void requiredInstance(
			Expression node, PrimitiveTypeTag required, GroundDataTypeOrVoid found
	) {
		raise( node,
				"required an instance of type '" + required + "', found '" + found + "'" );
	}

	public static void requiredInstance(
			TypeExpression node, SpecialTypeTag required, GroundDataTypeOrVoid found
	) {
		raise( node,
				"required an instance of type '" + required + "', found '" + found + "'" );
	}

	public static void invalidSwitchGuard( Expression node, GroundDataTypeOrVoid found ) {
		raise( node, "incompatible types, found '" + found + "', required an instance of '"
				+ PrimitiveTypeTag.CHAR + "', '" + PrimitiveTypeTag.BYTE + "', '"
				+ PrimitiveTypeTag.SHORT + "', '" + PrimitiveTypeTag.INT + "', '"
				+ SpecialTypeTag.BYTE + "', '" + SpecialTypeTag.SHORT + "', '"
				+ SpecialTypeTag.INTEGER + "', '" + SpecialTypeTag.STRING + "', or an enum type" );
	}

	public static void duplicateCase( Node node, String value ) {
		raise( node, "duplicate case '" + value + "'" );
	}

	public static void requiredLiteral( Node node, GroundDataTypeOrVoid type ) {
		raise( node, "required a literal of type '" + type + "', found a label" );
	}

	public static void requiredType(
			Node node, GroundDataTypeOrVoid required, GroundDataTypeOrVoid found
	) {
		raise( node, "required type '" + required + "', found '" + found + "'" );
	}

	public static void catchBindingUsesVar( VariableDeclaration node ) {
		raise( node, "catch bindings cannot use the var keyword" );
	}

	public static void missingReturnValue( Node node ) {
		raise( node, "missing return value" );
	}

	public static void valueReturnedFromVoid( Expression node ) {
		raise( node, "cannot return a value from a method with 'void' result type" );
	}

	public static void varMissingInitializer( VariableDeclaration node ) {
		raise( node, "var declarations require an initializer" );
	}

	public static void unreachableStatement( Statement node ) {
		raise( node, "unreachable statement" );
	}

	public static void requiredPrimitiveType( Expression node, GroundDataTypeOrVoid type ) {
		raise( node, "primitive type expected, '" + type + "' cannot be converted" );
	}

	public static void cannotApplyBinary(
			Expression node, BinaryExpression.Operator operator, GroundDataTypeOrVoid left,
			GroundDataTypeOrVoid right
	) {
		raise( node,
				"cannot apply '" + operator + "' to '" + left + "' and '" + right + "'" );
	}

	public static void cannotApplyNot( NotExpression node, GroundDataTypeOrVoid type ) {
		raise( node, "cannot apply '!' to '" + type + "'" );
	}

	public static void requiredAssignableTarget( AssignExpression node ) {
		raise( node, "expected assignable variable" );
	}

	public static void assignmentToFinalVariable( AssignExpression node, String name ) {
		raise( node, "cannot assign a value to final variable '" + name + "'" );
	}

	public static void unexpectedStaticTypeArgument( TypeExpression node ) {
		raise( node, "unexpected type argument in static member access" );
	}

	public static void abstractClassInstantiation(
			ClassInstantiationExpression node, GroundClass type
	) {
		raise( node, "'" + type + "' is abstract, cannot be instantiated" );
	}

	public static void unresolvedMethod(
			MethodCallExpression node, String name, List< ? extends GroundDataType > arguments,
			GroundDataTypeOrVoid receiver
	) {
		raise( node,
				"cannot resolve method '" + name
						+ arguments.stream().map( Object::toString )
								.collect( Formatting.joining( ",", "(", ")", "" ) )
						+ "' in '" + receiver + "'" );
	}

	public static void unresolvedMethodInVoid(
			MethodCallExpression node, String name, List< ? extends GroundDataType > arguments
	) {
		raise( node,
				"cannot resolve method '" + name + arguments.stream().map( Object::toString )
						.collect( Formatting.joining( ",", "(", ")", "" ) ) + "' in 'void'" );
	}

	public static void ambiguousMethod(
			MethodCallExpression node, List< ? extends Member.GroundCallable > candidates
	) {
		raise( node,
				"ambiguous method invocation, "
						+ candidates.stream().map( Member.GroundCallable::toString )
								.collect( Collectors.collectingAndThen( Collectors.toList(),
										Formatting.joiningOxfordComma() ) ) );
	}

	public static void thisBeforeConstructor( ThisExpression node ) {
		raise( node, "cannot reference 'this' before constructor has been called" );
	}

	public static void thisInStaticContext( ThisExpression node ) {
		raise( node,
				"non-static variable 'this' cannot be referenced from a static context" );
	}

	public static void superBeforeConstructor( SuperExpression node ) {
		raise( node,
				"cannot reference 'super' before supertype constructor has been called" );
	}

	public static void superInStaticContext( SuperExpression node ) {
		raise( node,
				"non-static variable 'super' cannot be referenced from a static context" );
	}

	public static void literalAtWrongRole(
			LiteralExpression< ? > node, List< ? extends World > roles
	) {
		raise( node,
				"Literal '" + node + "', can't be used in an expression at role '" + roles + "'" );
	}

	public static void requiredHigherReferenceType( TypeExpression node, Type type ) {
		raise( node, formattedAssertTypeMessage( type,
				"higher-kinded reference type expected, '%1$s' is %3$s" ) );
	}

	public static void requiredDataType( TypeExpression node, Type type ) {
		raise( node,
				formattedAssertTypeMessage( type, "data type expected, '%1$s' is %3$s" ) );
	}

	public static void requiredReferenceType( TypeExpression node, Type type ) {
		raise( node,
				formattedAssertTypeMessage( type, "reference type expected, '%1$s' is %3$s" ) );
	}

	public static void requiredClass( TypeExpression node, Type type ) {
		raise( node, formattedAssertTypeMessage( type, "class expected, '%1$s' is %3$s" ) );
	}

	public static void requiredInterface( TypeExpression node, Type type ) {
		raise( node,
				formattedAssertTypeMessage( type, "interface expected, '%1$s' is %3$s" ) );
	}

	public static void requiredEnum( TypeExpression node, Type type ) {
		raise( node, formattedAssertTypeMessage( type, "enum expected, '%1$s' is %3$s" ) );
	}

	private static String formattedAssertTypeMessage( Type type, String format ) {
		String description;
		if( type instanceof GroundEnum ) description = "an enum";
		else if( type instanceof GroundClass ) description = "a class";
		else if( type instanceof GroundInterface ) description = "an interface";
		else if( type instanceof GroundPrimitiveDataType ) description = "a primitive type";
		else if( type instanceof HigherEnum ) description = "a higher-kinded enum";
		else if( type instanceof HigherClass ) description = "a higher-kinded class";
		else if( type instanceof HigherInterface ) description = "a higher-kinded interface";
		else if( type instanceof HigherPrimitiveDataType )
			description = "a higher-kinded primitive type";
		else if( type instanceof HigherReferenceType )
			description = "a higher-kinded reference type";
		else if( type instanceof HigherDataType ) description = "a higher-kinded data type";
		else description = "a role";
		return String.format( format, type, type.kind(), description );
	}
}
