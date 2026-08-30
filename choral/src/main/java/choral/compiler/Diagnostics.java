package choral.compiler;

import choral.ast.Name;
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
import choral.exceptions.ChoralException;
import choral.exceptions.StaticVerificationException;
import choral.types.*;
import choral.types.Package;
import choral.types.Universe.PrimitiveTypeTag;
import choral.types.Universe.SpecialTypeTag;
import choral.utils.Formatting;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class Diagnostics {
	private Diagnostics() {
	}

	// METHOD DECLARATIONS

	public static AstPositionedException abstractMethodInConcreteClass(
			ClassMethodDefinition node
	) {
		return raise( node, "abstract method in non-abstract class" );
	}

	public static AstPositionedException abstractMethodHasBody( Statement node ) {
		return raise( node, "abstract methods cannot have bodies" );
	}

	public static AstPositionedException concreteMethodMissingBody( MethodDefinition node ) {
		return raise( node, "non-abstract methods must have bodies" );
	}

	public static StaticVerificationException methodClashesWithInheritedMethodReturnType(
			Member.HigherMethod method, Member.HigherMethod other
	) {
		return raise( "method '" + method + "' in '" + method.declarationContext()
				+ "' clashes with method '" + other + "' in '" + other.declarationContext()
				+ "', attempting to use incompatible return type" );
	}

	public static StaticVerificationException inheritedMethodErasureClashesWithDeclared(
			Member.HigherMethod declared, GroundClassOrInterface inheritor,
			Member.HigherMethod inherited
	) {
		return raise( "method '" + declared + "' in '" + inheritor + "' clashes with method '"
				+ inherited + "' in '" + inherited.declarationContext()
				+ "', both methods have the same erasure" );
	}

	public static StaticVerificationException defaultMethodsFoundDuplicate(
			GroundClassOrInterface type, Member.HigherMethod method, Member.HigherMethod other
	) {
		return raise( "Duplicate default methods inherited. '" + type + "' must override '"
				+ method + "'' from '" + method.declarationContext() + "' which is identical to '"
				+ other + "' from '" + other.declarationContext() + "'" );
	}

	public static StaticVerificationException concreteTypeMustImplementAbstractMethod(
			GroundClassOrInterface type, Member.HigherMethod method
	) {
		return raise(
				"'" + type + "' must either be declared as abstract or implement abstract method '"
						+ method + "' in '" + method.declarationContext() + "'" );
	}

	public static StaticVerificationException concreteTypeMustImplementAbstractMethod(
			Member.HigherMethod method
	) {
		return raise( "Implementation is not abstract and does not override abstract method '"
				+ method + "' in '" + method.declarationContext() + "'" );
	}

	public static StaticVerificationException methodOverridesFinalMethod(
			Member.HigherMethod child, GroundClassOrInterface type, Member.HigherMethod parent
	) {
		return raise( "method '" + child + "' in '" + type + "' cannot override final method '"
				+ parent + "' in '" + parent.declarationContext() + "'" );
	}

	public static StaticVerificationException methodOverridesStaticMethod(
			Member.HigherMethod child, GroundClassOrInterface type, Member.HigherMethod parent
	) {
		return raise( "instance method '" + child + "' in '" + type
				+ "' cannot override static method '" + parent + "' in '"
				+ parent.declarationContext() + "'" );
	}

	public static StaticVerificationException staticMethodOverridesInstanceMethod(
			Member.HigherMethod child, GroundClassOrInterface type, Member.HigherMethod parent
	) {
		return raise( "static method '" + child + "' in '" + type
				+ "' cannot override instance method '" + parent + "' in '"
				+ parent.declarationContext() + "'" );
	}

	public static StaticVerificationException methodOverrideHasWeakerAccess(
			Member.HigherMethod child, String type, Member.HigherMethod parent,
			String childAccess, String parentAccess
	) {
		return raise( "method '" + child + "' in '" + type + "' clashes with method '"
				+ parent + "' in '" + parent.declarationContext()
				+ "', attempting to assign weaker access privileges '" + childAccess + "' to '"
				+ parentAccess + "'" );
	}

	public static StaticVerificationException methodAlreadyDefined(
			Member.HigherMethod method, HigherClassOrInterface type
	) {
		return raise( "method '" + method + "' is already defined in '" + type + "'" );
	}

	public static StaticVerificationException methodsHaveSameErasure(
			Member.HigherMethod method, Member.HigherMethod other
	) {
		return raise( "method '" + method + "' clashes with '" + other
				+ "', both methods have the same erasure" );
	}

	// SELECTION METHOD DECLARATIONS
	public static AstPositionedException selectionMethodIllegalTypeParameterCount(
			Node node, int found
	) {
		return raise( node, "illegal selection method, expected 1 type parameter, found " + found );
	}

	public static AstPositionedException selectionMethodIllegalTypeParameterBound( Node node ) {
		return raise( node,
				"illegal selection method, the type parameter must be bounded exactly by '"
						+ SpecialTypeTag.ENUM + "'" );
	}

	public static AstPositionedException selectionMethodIllegalParameterCount(
			Node node, int found
	) {
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

	// PARAMETERS

	public static StaticVerificationException parameterAlreadyDefined( String identifier ) {
		return raise( "duplicate parameter '" + identifier + "'" );
	}

	// ROLE PARAMETERS

	public static ChoralException roleParameterAlreadyDefined(
			Optional< Node > sourceCode, String identifier
	) {
		return raise( sourceCode, "duplicate role parameter '" + identifier + "'" );
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

	public static StaticVerificationException variableAlreadyDefined( String identifier ) {
		return raise( "variable '" + identifier + "' already defined in the scope" );
	}

	// ENUMS

	public static AstPositionedException enumIllegalNumberOfRoles( Node node ) {
		return raise( node, "enums must have exactly one role" );
	}

	public static AstPositionedException enumRequired( TypeExpression node, Type type ) {
		return raise( node, formattedAssertTypeMessage( type, "enum expected, '%1$s' is %3$s" ) );
	}

	public static StaticVerificationException enumHasAbstractModifier() {
		return raise( "modifier 'abstract' not allowed for enums" );
	}

	public static StaticVerificationException enumCaseAlreadyDefined(
			String identifier, String typeLabel, HigherEnum type
	) {
		return raise( "duplicate case '" + identifier + "' in " + typeLabel + " '" + type + "'" );
	}

	public static StaticVerificationException enumCaseConflictsWithField(
			String identifier, String typeLabel, HigherEnum type
	) {
		return raise( "duplicate variable '" + identifier + "', " + typeLabel + " '" + type
				+ "' contains a field with the same identifier" );
	}

	public static StaticVerificationException enumFieldConflictsWithCase(
			String identifier, String typeLabel, HigherEnum type
	) {
		return raise( "duplicate variable '" + identifier + "', " + typeLabel + " '" + type
				+ "'  contains a case with the same identifier" );
	}

	// TYPE PARAMETERS

	public static StaticVerificationException typeParameterAlreadyDefined( String identifier ) {
		return raise( "duplicate type parameter '" + identifier + "'" );
	}

	public static StaticVerificationException typeParameterBoundHasWrongRoles(
			GroundReferenceType type, GroundReferenceType parameter
	) {
		return raise( "illegal bound, '" + type + "' and '" + parameter
				+ "' must have the same roles" );
	}

	public static StaticVerificationException typeParameterDuplicateBound(
			GroundReferenceType type
	) {
		return raise( "duplicate parameter bound, '" + type + "' is repeated" );
	}

	// TYPES

	public static StaticVerificationException typeArgumentNotWithinBounds(
			String typeArgument, String appliedTypeArgument, String bounds, String worlds
	) {
		return raise( "type argument '" + typeArgument + "' is not within bounds, '"
				+ appliedTypeArgument + "' must extend " + bounds + " for any role " + worlds );
	}

	public static StaticVerificationException typeArgumentsWrongCount(
			int expected, int found
	) {
		return raise( "illegal type instantiation: expected " + expected
				+ " type arguments but found " + found );
	}

	public static StaticVerificationException invalidSpecialType(
			String identifier, String expected, String found
	) {
		return raise( "Invalid special type '" + identifier + "', expected " + expected
				+ " found " + found );
	}

	public static StaticVerificationException typeHasNoPublicAccess( HigherClassOrInterface type ) {
		return raise( type.variety().labelSingular + " '" + type.identifier( true )
				+ "' has not public access" );
	}

	// VOID TYPE

	public static AstPositionedException voidTypeIllegalArguments( TypeExpression node ) {
		return raise( node, "illegal type instantiation, expected 0 role and 0 type arguments" );
	}

	// INHERITANCE

	public static StaticVerificationException inheritanceConflictingAncestors(
			GroundInterface first, GroundInterface second
	) {
		return raise(
				"illegal inheritance, cannot implement both '" + first + "' and " + second + "'" );
	}

	public static StaticVerificationException inheritanceRepeatedInterface( GroundInterface type ) {
		return raise( "illegal inheritance, '" + type + "' is repeated" );
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

	public static StaticVerificationException classExtendsFinal( GroundClass type ) {
		return raise( "illegal inheritance, cannot inherit from final '" + type + "'" );
	}

	public static StaticVerificationException classExtendsEnum( HigherClass enumType ) {
		return raise( "illegal inheritance, only enum types can inherit from '" + enumType + "'" );
	}

	public static StaticVerificationException classExtendsWrongNumberOfRoles(
			GroundClassOrInterface type, GroundClassOrInterface inheritor
	) {
		return raise( "illegal inheritance, '" + type + "' and '" + inheritor
				+ "' must have the same roles" );
	}

	// INTERFACES

	public static StaticVerificationException interfaceExpected(
			GroundReferenceType type, String description
	) {
		return raise( "interface expected, '" + type + "' is " + description );
	}

	// MODIFIERS

	public static StaticVerificationException modifiersIllegalCombination( String modifiers ) {
		return raise( "illegal combination of modifiers " + modifiers );
	}

	public static StaticVerificationException modifiersIllegalCombination(
			Modifier first, Modifier second
	) {
		return raise( "illegal combination of modifiers '" + first.label + "' and '"
				+ second.label + "'" );
	}

	public static StaticVerificationException modifiersNotAllowed(
			String prefix, String modifiers, String where
	) {
		return raise( prefix + modifiers + " not allowed " + where );
	}

	// CONSTRUCTORS

	public static AstPositionedException constructorInvocationIsRecursive(
			MethodCallExpression node
	) {
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

	public static StaticVerificationException constructorMissing( GroundClass type ) {
		return raise( "there is no default constructor available in '" + type + "'" );
	}

	public static StaticVerificationException constructorAlreadyDefined(
			Member.HigherConstructor constructor, HigherClass type
	) {
		return raise( "constructor '" + constructor + "' is already defined in '" + type + "'" );
	}

	public static StaticVerificationException constructorErasureClash(
			Member.HigherConstructor constructor, Member.HigherConstructor other
	) {
		return raise( "constructor '" + constructor + "' clashes with '" + other
				+ "', both constructors have the same erasure" );
	}

	// FIELDS

	public static StaticVerificationException fieldHasAbstractModifier() {
		return raise( "modifier 'abstract' not allowed for fields" );
	}

	public static StaticVerificationException fieldAlreadyDefined(
			String identifier, String typeLabel, HigherClassOrInterface type
	) {
		return raise( "duplicate variable '" + identifier + "' in " + typeLabel + " '" + type );
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

	public static AstPositionedException typeMismatchExpectedPrimitiveType(
			Expression node, GroundDataTypeOrVoid type
	) {
		return raise( node, "primitive type expected, '" + type + "' cannot be converted" );
	}

	public static AstPositionedException typeMismatchExpectedHigherKindedReferenceType(
			TypeExpression node, Type type
	) {
		return raise( node, formattedAssertTypeMessage( type,
				"higher-kinded reference type expected, '%1$s' is %3$s" ) );
	}

	public static AstPositionedException typeMismatchExpectedDataType(
			TypeExpression node, Type type
	) {
		return raise( node,
				formattedAssertTypeMessage( type, "data type expected, '%1$s' is %3$s" ) );
	}

	public static AstPositionedException typeMismatchExpectedReferenceType(
			TypeExpression node, Type type
	) {
		return raise( node,
				formattedAssertTypeMessage( type, "reference type expected, '%1$s' is %3$s" ) );
	}

	public static AstPositionedException typeMismatchExpectedClass(
			TypeExpression node, Type type
	) {
		return raise( node, formattedAssertTypeMessage( type, "class expected, '%1$s' is %3$s" ) );
	}

	public static AstPositionedException typeMismatchExpectedInterface(
			TypeExpression node, Type type
	) {
		return raise( node,
				formattedAssertTypeMessage( type, "interface expected, '%1$s' is %3$s" ) );
	}

	// SWITCH STATEMENTS

	public static AstPositionedException switchGuardTypeMismatch(
			Expression node, GroundDataTypeOrVoid found
	) {
		return raise( node, "incompatible types, found '" + found + "', required an instance of '"
				+ PrimitiveTypeTag.CHAR + "', '" + PrimitiveTypeTag.BYTE + "', '"
				+ PrimitiveTypeTag.SHORT + "', '" + PrimitiveTypeTag.INT + "', '"
				+ SpecialTypeTag.BYTE + "', '" + SpecialTypeTag.SHORT + "', '"
				+ SpecialTypeTag.INTEGER + "', '" + SpecialTypeTag.STRING + "', or an enum type" );
	}

	public static AstPositionedException switchStatementDuplicateCase( Node node, String value ) {
		return raise( node, "duplicate case '" + value + "'" );
	}

	public static AstPositionedException literalRequiredFoundLabel(
			Node node, GroundDataTypeOrVoid type
	) {
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
		return raise( node,
				"cannot apply '" + operator + "' to '" + left + "' and '" + right + "'" );
	}

	public static AstPositionedException unaryOperatorCannotApply(
			NotExpression node, GroundDataTypeOrVoid type
	) {
		return raise( node, "cannot apply '!' to '" + type + "'" );
	}

	// ASSIGNMENT

	public static AstPositionedException assignmentExpectedAssignableVariable(
			AssignExpression node
	) {
		return raise( node, "expected assignable variable" );
	}

	public static AstPositionedException assignmentToFinalVariable(
			AssignExpression node, String name
	) {
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

	public static StaticVerificationException instantiateWrongRoleCount(
			int expected, int found
	) {
		return raise( "illegal type instantiation: expected " + expected
				+ " role arguments but found " + found );
	}

	public static StaticVerificationException instantiateDuplicateRole(
			World role, HigherDataType type
	) {
		return raise( "illegal type instantiation: role '" + role
				+ "' must play exactly one role in '" + type + "'" );
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

	// SYMBOLS
	
	public static StaticVerificationException symbolAlreadyDefined( String name ) {
		return raise( "Duplicate declaration for '" + name + "'" );
	}

	public static StaticVerificationException symbolAlreadyDefined(
			String name, Package declarationPackage
	) {
		return raise( "Duplicate declaration for '" + name + "' in '" + declarationPackage + "'" );
	}

	public static StaticVerificationException symbolIsAmbiguous(
			String query, List< HigherClassOrInterface > candidates
	) {
		var candidatesList = candidates.stream()
				.map( x -> "'" + x.identifier( true ) + "'" )
				.collect( Collectors.collectingAndThen( Collectors.toList(),
						Formatting.joiningOxfordComma() ) );
		return raise(
				"reference to '" + query + "' is ambiguous, " + candidatesList + " are ambiguous" );
	}

	public static AstPositionedException symbolNotFound( Node place, String symbol ) {
		return raise( place, "cannot resolve symbol '" + symbol + "'" );
	}

	public static StaticVerificationException symbolNotFound( String symbol ) {
		return raise( "cannot resolve symbol '" + symbol + "'" );
	}

	public static AstPositionedException symbolNotFound( Name symbol ) {
		return raise( symbol, "cannot resolve symbol '" + symbol.identifier() + "'" );
	}

	// HELPERS

	private static StaticVerificationException raise( String message ) {
		return new StaticVerificationException( message );
	}

	private static AstPositionedException raise( Node node, String message ) {
		return new AstPositionedException(
				node.position(), new StaticVerificationException( message ) );
	}

	private static ChoralException raise( Optional< Node > node, String message ) {
		return node.isEmpty() ? raise( message ) : raise( node.get(), message );
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
