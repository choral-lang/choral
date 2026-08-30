package choral.compiler;

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

public final class Diagnostics {
	private Diagnostics() {
	}

	// METHOD DECLARATIONS

	public static AstPositionedException abstractMethodInConcreteClass( ClassMethodDefinition node ) {
		return raise( node, "abstract method in non-abstract class" );
	}
	
	public static AstPositionedException abstractMethodHasBody( Statement node ) {
		return raise( node, "abstract methods cannot have bodies" );
	}
	
	public static AstPositionedException concreteMethodMissingBody( MethodDefinition node ) {
		return raise( node, "non-abstract methods must have bodies" );
	}

	// SELECTION METHOD DECLARATIONS
	public static AstPositionedException selectionMethodIllegalTypeParameterCount( Node node, int found ) {
		return raise( node, "illegal selection method, expected 1 type parameter, found " + found );
	}

	public static AstPositionedException selectionMethodIllegalTypeParameterBound( Node node ) {
		return raise( node,
				"illegal selection method, the type parameter must be bounded exactly by '"
						+ SpecialTypeTag.ENUM + "'" );
	}

	public static AstPositionedException selectionMethodIllegalParameterCount( Node node, int found ) {
		return raise( node,
				"illegal selection method, expected 1 method parameter, found " + found );
	}

	public static AstPositionedException selectionMethodIllegalParameterType(
			Node node, GroundReferenceType expected, List< ? extends World > worlds,
			GroundDataType found
	) {
		return raise( node,
				"illegal selection method, expected a method parameter of type '" + expected
						+ "' for some " + Formatting.joiningQuotedAndOxfordComma( worlds )
						+ ", found '" + found + "'" );
	}

	public static AstPositionedException selectionMethodVoidReturn(
			Node node, GroundReferenceType expected, List< ? extends World > worlds
	) {
		return raise( node,
				"illegal selection method, expected return type '" + expected + "' for some "
						+ Formatting.joiningQuotedAndOxfordComma( worlds ) + ", found 'void'" );
	}

	public static AstPositionedException selectionMethodRolesNotDistinct( Node node ) {
		return raise( node,
				"illegal selection method, roles of the method parameter and return type must be distinct" );
	}

	// VARIABLE DECLARATIONS

	public static AstPositionedException varUsedAsParameter( VariableDeclaration node ) {
		return raise( node, "parameters cannot use the var keyword" );
	}
	
	public static AstPositionedException varUsedAsCatchBinding( VariableDeclaration node ) {
		return raise( node, "catch bindings cannot use the var keyword" );
	}
	
	public static AstPositionedException varMissingInitializer( VariableDeclaration node ) {
		return raise( node, "var declarations require an initializer" );
	}

	// ENUMS
	
	public static AstPositionedException enumIllegalNumberOfRoles( Node node ) {
		return raise( node, "enums must have exactly one role" );
	}
	
	public static AstPositionedException enumRequired( TypeExpression node, Type type ) {
		return raise( node, formattedAssertTypeMessage( type, "enum expected, '%1$s' is %3$s" ) );
	}
		
	// VOID TYPE

	public static AstPositionedException voidTypeIllegalArguments( TypeExpression node ) {
		return raise( node, "illegal type instantiation, expected 0 role and 0 type arguments" );
	}

	// CLASSES

	public static AstPositionedException classExtendingItsSubclass(
			Node node, GroundClassOrInterface type, GroundClassOrInterface supertype
	) {
		return raise( node,
				"Cyclic inheritance: '" + type + "' cannot extend '" + supertype + "'" );
	}
	public static AstPositionedException publicClassWrongFileName(
			TemplateDeclaration node, String family, String name, String extension
	) {
		return raise( node, family + " '" + name
				+ "' is public, should be declared in a file named '" + name + extension + "'" );
	}

	// CONSTRUCTORS

	public static AstPositionedException constructorInvocationIsRecursive( MethodCallExpression node ) {
		return raise( node, "recursive constructor invocation" );
	}
	
	public static AstPositionedException constructorMissing(
			ConstructorDefinition node, GroundClass type
	) {
		return raise( node, "there is no default constructor available in '" + type + "'" );
	}
	
	public static AstPositionedException constructorInvocationCannotBeResolved(
			Node node, GroundClass type, List< ? extends GroundDataType > arguments
	) {
		return raise( node, "cannot resolve constructor '" + type + arguments.stream()
				.map( Object::toString ).collect( Formatting.joining( ",", "(", ")", "" ) ) + "'" );
	}

	public static AstPositionedException constructorInvocationIsAmbiguous(
			Node node, GroundClass type, List< ? extends Member.GroundCallable > candidates
	) {
		return raise( node,
				"ambiguous constructor invocation, " + candidates.stream().map( x -> "'" + type
						+ x.signature().parameters().stream().map( y -> y.type().toString() )
								.collect( Formatting.joining( ",", "(", ")", "" ) )
						+ "'" )
						.collect( Collectors.collectingAndThen( Collectors.toList(),
								Formatting.joiningOxfordComma() ) ) );
	}
	
	public static AstPositionedException superConstructorWithoutParentClass(
			MethodCallExpression node, HigherClassOrInterface type
	) {
		return raise( node,
				"cannot resolve 'super', class '" + type + "' does not extend any class" );
	}

	// TYPE MISMATCH

	public static AstPositionedException typeMismatchExpectedDataTypeFoundVoid( Expression node ) {
		return raise( node, "data type expected, found 'void'" );
	}

	public static AstPositionedException typeMismatch(
			Expression node, PrimitiveTypeTag expected, GroundDataTypeOrVoid found
	) {
		return raise( node, "reuired type '" + expected + "', found '" + found + "'" );
	}

	public static AstPositionedException typeMismatch(
			TypeExpression node, SpecialTypeTag expected, GroundDataTypeOrVoid found
	) {
		return raise( node, "required type '" + expected + "', found '" + found + "'" );
	}
	
	public static AstPositionedException typeMismatch(
			Node node, GroundDataTypeOrVoid required, GroundDataTypeOrVoid found
	) {
		return raise( node, "required type '" + required + "', found '" + found + "'" );
	}
	
	public static AstPositionedException typeMismatchExpectedPrimitiveType( Expression node, GroundDataTypeOrVoid type ) {
		return raise( node, "primitive type expected, '" + type + "' cannot be converted" );
	}
	
	public static AstPositionedException typeMismatchExpectedHigherKindedReferenceType( TypeExpression node, Type type ) {
		return raise( node, formattedAssertTypeMessage( type, "higher-kinded reference type expected, '%1$s' is %3$s" ) );
	}

	public static AstPositionedException typeMismatchExpectedDataType( TypeExpression node, Type type ) {
		return raise( node, formattedAssertTypeMessage( type, "data type expected, '%1$s' is %3$s" ) );
	}

	public static AstPositionedException typeMismatchExpectedReferenceType( TypeExpression node, Type type ) {
		return raise( node, formattedAssertTypeMessage( type, "reference type expected, '%1$s' is %3$s" ) );
	}

	public static AstPositionedException typeMismatchExpectedClass( TypeExpression node, Type type ) {
		return raise( node, formattedAssertTypeMessage( type, "class expected, '%1$s' is %3$s" ) );
	}

	public static AstPositionedException typeMismatchExpectedInterface( TypeExpression node, Type type ) {
		return raise( node, formattedAssertTypeMessage( type, "interface expected, '%1$s' is %3$s" ) );
	}

	// SWITCH STATEMENTS

	public static AstPositionedException switchGuardTypeMismatch( Expression node, GroundDataTypeOrVoid found ) {
		return raise( node, "incompatible types, found '" + found + "', required an instance of '"
				+ PrimitiveTypeTag.CHAR + "', '" + PrimitiveTypeTag.BYTE + "', '"
				+ PrimitiveTypeTag.SHORT + "', '" + PrimitiveTypeTag.INT + "', '"
				+ SpecialTypeTag.BYTE + "', '" + SpecialTypeTag.SHORT + "', '"
				+ SpecialTypeTag.INTEGER + "', '" + SpecialTypeTag.STRING + "', or an enum type" );
	}

	public static AstPositionedException switchStatementDuplicateCase( Node node, String value ) {
		return raise( node, "duplicate case '" + value + "'" );
	}

	public static AstPositionedException literalRequiredFoundLabel( Node node, GroundDataTypeOrVoid type ) {
		return raise( node, "required a literal of type '" + type + "', found a label" );
	}

	// CONTROL FLOW

	public static AstPositionedException returnValueMissing( Node node ) {
		return raise( node, "missing return value" );
	}

	public static AstPositionedException returnValueFromVoid( Expression node ) {
		return raise( node, "cannot return a value from a method with 'void' result type" );
	}

	public static AstPositionedException controlFlowUnreachableStatement( Statement node ) {
		return raise( node, "unreachable statement" );
	}
	
	public static AstPositionedException returnStatementMissing( Statement node ) {
		return raise( node, "missing return statement" );
	}

	// OPERATORS

	public static AstPositionedException binaryOperatorCannotApply(
			Expression node, BinaryExpression.Operator operator, GroundDataTypeOrVoid left,
			GroundDataTypeOrVoid right
	) {
		return raise( node, "cannot apply '" + operator + "' to '" + left + "' and '" + right + "'" );
	}

	public static AstPositionedException unaryOperatorCannotApply( NotExpression node, GroundDataTypeOrVoid type ) {
		return raise( node, "cannot apply '!' to '" + type + "'" );
	}

	// ASSIGNMENT

	public static AstPositionedException assignmentExpectedAssignableVariable( AssignExpression node ) {
		return raise( node, "expected assignable variable" );
	}

	public static AstPositionedException assignmentToFinalVariable( AssignExpression node, String name ) {
		return raise( node, "cannot assign a value to final variable '" + name + "'" );
	}

	// STATIC MEMBERS

	public static AstPositionedException staticMemberUnexpectedTypeArgument( TypeExpression node ) {
		return raise( node, "unexpected type argument in static member access" );
	}

	// INSTANTIATION

	public static AstPositionedException instantiateAbstractClass(
			ClassInstantiationExpression node, GroundClass type
	) {
		return raise( node, "'" + type + "' is abstract, cannot be instantiated" );
	}

	// METHOD CALLS

	public static AstPositionedException methodUnresolved(
			MethodCallExpression node, String name, List< ? extends GroundDataType > arguments,
			GroundDataTypeOrVoid receiver
	) {
		return raise( node,
				"cannot resolve method '" + name
						+ arguments.stream().map( Object::toString )
								.collect( Formatting.joining( ",", "(", ")", "" ) )
						+ "' in '" + receiver + "'" );
	}

	public static AstPositionedException methodUnresolvedInVoid(
			MethodCallExpression node, String name, List< ? extends GroundDataType > arguments
	) {
		return raise( node,
				"cannot resolve method '" + name + arguments.stream().map( Object::toString )
						.collect( Formatting.joining( ",", "(", ")", "" ) ) + "' in 'void'" );
	}

	public static AstPositionedException methodAmbiguousInvocation(
			MethodCallExpression node, List< ? extends Member.GroundCallable > candidates
	) {
		return raise( node,
				"ambiguous method invocation, "
						+ candidates.stream().map( Member.GroundCallable::toString )
								.collect( Collectors.collectingAndThen( Collectors.toList(),
										Formatting.joiningOxfordComma() ) ) );
	}

	// THIS

	public static AstPositionedException thisBeforeConstructor( ThisExpression node ) {
		return raise( node, "cannot reference 'this' before constructor has been called" );
	}

	public static AstPositionedException thisInStaticContext( ThisExpression node ) {
		return raise( node,
				"non-static variable 'this' cannot be referenced from a static context" );
	}

	// SUPER

	public static AstPositionedException superBeforeConstructor( SuperExpression node ) {
		return raise( node,
				"cannot reference 'super' before supertype constructor has been called" );
	}

	public static AstPositionedException superInStaticContext( SuperExpression node ) {
		return raise( node,
				"non-static variable 'super' cannot be referenced from a static context" );
	}

	// LITERALS

	public static AstPositionedException literalAtWrongRole(
			LiteralExpression< ? > node, List< ? extends World > roles
	) {
		return raise( node,
				"Literal '" + node + "', can't be used in an expression at role '" + roles + "'" );
	}

	// HELPERS

	private static AstPositionedException raise( Node node, String message ) {
		return new AstPositionedException(
				node.position(), new StaticVerificationException( message ) );
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
