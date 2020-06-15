/*
 * [The "BSD license"]
 *  Copyright (c) 2014 Terence Parr
 *  Copyright (c) 2014 Sam Harwell
 *  Copyright (c) 2019-2020 Saverio Giallorenzo
 *  Copyright (c) 2019-2020 Fabrizio Montesi
 *  Copyright (c) 2019-2020 Marco Peressotti
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

grammar Choral;

/*
 * Productions from §3 (Lexical Structure)
 */

literal
	: BooleanLiteral
	| IntegerLiteral
	| FloatingPointLiteral
	| StringLiteral
	;

referenceType
	: Identifier worldArguments? typeArguments?
	;

typeParameter
	: Identifier worldParameters typeBound?
	;

worldParameter
	: Identifier
	;

worldArgument
	: Identifier
	;

worldArguments
	: AT LPAREN worldArgumentList RPAREN
	| AT worldArgument
	;

worldArgumentList
	: worldArgument ( COMMA worldArgument )*
	;

typeBound
	: EXTENDS referenceType additionalBound*
	;

additionalBound
	: AMPERSAND referenceType
	;

typeArguments
	: LANGLE typeArgumentList RANGLE
	;

typeArgumentList
	: referenceType ( COMMA referenceType )*
	;

expressionName
	: Identifier
	| ambiguousName DOT Identifier
	;

ambiguousName
	: Identifier
	| ambiguousName DOT Identifier
	;

/*
 * Productions from §7 (Packages)
 */

// PARSING ENTRY POINT
compilationUnit
	: headerDeclaration typeDeclaration*
	| EOF
	;

headerDeclaration :
	packageDeclaration?	importDeclaration*
	;

packageDeclaration:
	PACKAGE qualifiedName SEMI
	;

importDeclaration
	: IMPORT qualifiedName (DOT STAR)? SEMI
	;

qualifiedName
	: Identifier
	| qualifiedName DOT Identifier
	;

typeDeclaration
	: classDeclaration
	| interfaceDeclaration
	| enumDeclaration
	;

annotations
	: AT Identifier ( LPAREN annotationValues RPAREN )? annotations?
	;

annotationValues
 	: Identifier ASSIGN literal ( COMMA annotationValues )?
	;

/*
 * Productions from §8 (Classes)
 */

classDeclaration
	: annotations? classModifier* CLASS Identifier worldParameters typeParameters? superClass? superInterfaces? classBody
	;

classModifier
	:	'public'
	|	'protected'
	|	'private'
	|	'abstract'
	|	'static'
	|	'final'
	;


worldParameters
	: AT LPAREN worldParameterList RPAREN
	| AT worldParameter
	;

typeParameters
	: LANGLE typeParameterList RANGLE
	;

typeParameterList
	: typeParameter ( COMMA typeParameter )*
	;

worldParameterList
	: worldParameter ( COMMA worldParameter )*
	;

superInterfaces
	: IMPLEMENTS interfaceTypeList
	;

interfaceTypeList
	: referenceType ( COMMA referenceType )*
	;

superClass
	: EXTENDS referenceType
	;

classBody
	: LBRACE classBodyDeclaration* RBRACE
	;

classBodyDeclaration
	: classMemberDeclaration
	| constructorDeclaration
	;

classMemberDeclaration
	: fieldDeclaration
	| methodDeclaration
	;

fieldDeclaration
	: fieldModifier* referenceType Identifier+ SEMI
	;

fieldModifier
	:	'public'
	|	'protected'
	|	'private'
	|	'static'
	|	'final'
	;

methodDeclaration
	: annotations? methodModifier* methodHeader methodBody
	;

methodModifier
	:	'public'
	|	'protected'
	|	'private'
	|	'abstract'
	|	'static'
	|	'final'
	;

methodHeader
	: typeParameters? result Identifier LPAREN formalParameterList? RPAREN
	;

result
	: referenceType
	| VOID
	;

formalParameterList
	: formalParameters
	;

formalParameters
	: formalParameter ( COMMA formalParameter )*
	;

formalParameter
	: referenceType Identifier
	;

methodBody
	: block
	| SEMI
	;

constructorDeclaration
	: constructorModifier* constructorDeclarator constructorBody
	;

constructorModifier
	:	'public'
	|	'protected'
	|	'private'
	;

constructorDeclarator
	: typeParameters? Identifier LPAREN formalParameterList? RPAREN
	;

constructorBody
    : LBRACE  explicitConstructorInvocation? blockStatements? RBRACE
    ;

explicitConstructorInvocation
    :	typeArguments? THIS LPAREN argumentList? RPAREN SEMI
    |	typeArguments? SUPER LPAREN argumentList? RPAREN SEMI
    ;

/*
 * Productions from §9 (Interfaces), added enums
 */

interfaceDeclaration
	: annotations? interfaceModifier* INTERFACE Identifier worldParameters typeParameters? extendsInterfaces? interfaceBody
	;

interfaceModifier
	:	'public'
	|	'protected'
	|	'private'
	|	'abstract'
	|	'static'
	;

enumDeclaration
	: annotations? classModifier* ENUM Identifier AT worldParameter enumBody
	;

extendsInterfaces
	: EXTENDS interfaceTypeList
	;

interfaceBody
	: LBRACE interfaceMethodDeclaration* RBRACE
	;

enumBody
	: LBRACE enumConstantList? RBRACE
	;

enumConstantList
	:	enumConstant (COMMA enumConstant)*
	;

enumConstant
	:	Identifier
	;

interfaceMethodDeclaration
	: annotations? interfaceMethodModifier* methodHeader SEMI
	;

interfaceMethodModifier
	:	'public'
//	|	'private'//Introduced in Java 9
	|	'abstract'
//	|	'default'
//	|	'static'
	;

/*
 * Productions from §14 (Blocks and Statements)
 */

block
	: LBRACE blockStatements? RBRACE
	;

blockStatements
	: blockStatement+
	;

blockStatement
	: localVariableDeclaration SEMI
	| localVariableDeclarationAndAssignment SEMI
	| block
	| statement
	;

localVariableDeclaration
	: referenceType Identifier+
	;

localVariableDeclarationAndAssignment
	: referenceType Identifier assignmentOperator shortCircuitOrExpression chainedExpression?
	;

statement
	: basicStatement
	| ifThenStatement
	| ifThenElseStatement
	| switchStatement
	| tryCatchStatement
	;

basicStatement
	: emptyStatement
//	| selectStatement
	| expressionStatement
	| returnStatement
	;

emptyStatement
	: SEMI
	;

//selectStatement
//	: SELECT LPAREN enumCaseCreationExpression COMMA expression RPAREN SEMI
//	;

expressionStatement
	: statementExpression SEMI
	| expression chainedExpression SEMI
	;

statementExpression
	: assignment
	| trailingExpression
	;

trailingExpression
	: ( fieldAccess
			| methodInvocation
			| classInstanceCreationExpression
			| staticGenericAccess
	) ( DOT trailExpression )?
	;

trailExpression
	: ( fieldAccess_no_primary
			| methodInvocation
	) ( DOT trailExpression )?
	;

chainedExpression
	: fwd_chain chainedInvocation ( fwd_chain chainedInvocation )*
	;

chainedInvocation
	: chainedClassInstanceCreation
	| chainedStaticMethodInvocation
	| chainedMethodInvocation
	;

chainedMethodInvocation
	: Identifier ( DOT Identifier )* CHAIN_ACCESS typeArguments? Identifier
	| THIS ( DOT Identifier )* CHAIN_ACCESS typeArguments? Identifier
	| SUPER ( DOT Identifier )* CHAIN_ACCESS typeArguments? Identifier
	;

chainedStaticMethodInvocation
	: staticGenericAccess ( DOT Identifier )* CHAIN_ACCESS typeArguments? Identifier
	;

chainedClassInstanceCreation
	: staticGenericAccess CHAIN_ACCESS typeArguments? NEW
	;

methodInvocation
	: typeArguments? ( Identifier | SUPER ) LPAREN argumentList? RPAREN
	;

staticGenericAccess
	: Identifier worldArguments typeArguments?
	;

primary
	:	literal AT worldArgument
	|	NULL worldArguments
	|	THIS
    |   SUPER
	|	LPAREN expression RPAREN
	;

classInstanceCreationExpression
	: NEW methodArgs=typeArguments? Identifier worldArguments classArguments=typeArguments? LPAREN argumentList? RPAREN
	;

enumCaseCreationExpression
	: Identifier AT worldArgument DOT Identifier
	;

fieldAccess
	: primary DOT Identifier
	| ( expressionName DOT )? Identifier
	;

fieldAccess_no_primary
    : ( expressionName DOT )? Identifier
    ;

argumentList
	: expression ( COMMA expression )*
	| literal AT LBRACK worldArgumentList RBRACK
	;

ifThenStatement
	: IF LPAREN expression RPAREN block
	;

ifThenElseStatement
	: IF LPAREN expression RPAREN thenBlock=block ELSE elseBlock=block
	;

switchStatement
	: SWITCH LPAREN expression RPAREN switchBlock
	;

switchBlock
	: LBRACE switchCase+ RBRACE
	;

switchCase
	: CASE switchArgs RIGHT_ARROW block
	| DEFAULT RIGHT_ARROW block
	;

switchArgs
	: ( Identifier | literal AT worldArgument ) ( COMMA switchArgs )?
	;

tryCatchStatement
	: TRY block catchBlock+
	;

catchBlock
	: CATCH LPAREN formalParameter RPAREN block
	;

returnStatement
	: RETURN expression chainedExpression? SEMI
	| RETURN SEMI
	;

expression
	: shortCircuitOrExpression
	| assignment
	;

assignment
	: leftHandSide assignmentOperator expression
	;

leftHandSide
	: expressionName
	| fieldAccess
	;

assignmentOperator
	: ASSIGN
	| MUL_ASSIGN
	| DIV_ASSIGN
	| MOD_ASSIGN
	| ADD_ASSIGN
	| SUB_ASSIGN
	| AND_ASSIGN
	| OR_ASSIGN
	;

shortCircuitOrExpression
	: shortCircuitAndExpression
	| shortCircuitOrExpression OR shortCircuitAndExpression
	;

shortCircuitAndExpression
	: orExpression
	| shortCircuitAndExpression AND orExpression
	;

orExpression
	: andExpression
	| orExpression BITOR andExpression
	;

andExpression
	: equalityExpression
	| andExpression AMPERSAND equalityExpression
	;

equalityExpression
	: relationalExpression
	| equalityExpression op=EQUAL relationalExpression
	| equalityExpression op=NOTEQUAL relationalExpression
	;

relationalExpression
	: additiveExpression
	| relationalExpression op=RANGLE additiveExpression
	| relationalExpression op=GT_EQUAL additiveExpression
	| relationalExpression op=LANGLE additiveExpression
	| relationalExpression op=LT_EQUAL additiveExpression
	;

additiveExpression
	: multiplicativeExpression
	| additiveExpression op=ADD multiplicativeExpression
	| additiveExpression op=SUB multiplicativeExpression
	;

multiplicativeExpression
	: unaryExpression
	| multiplicativeExpression op=STAR unaryExpression
	| multiplicativeExpression op=DIV unaryExpression
	| multiplicativeExpression op=MOD unaryExpression
	;

unaryExpression
	: primary
	| statementExpression
	| op=NOT unaryExpression
	;

// LEXER

// §3.9 Keywords

CLASS       : 'class';
ELSE        : 'else';
SWITCH 		: 'switch';
CASE		: 'case';
DEFAULT		: 'default';
EXTENDS     : 'extends';
IF          : 'if';
IMPLEMENTS  : 'implements';
PACKAGE     : 'package';
IMPORT      : 'import';
INTERFACE   : 'interface';
ENUM 		: 'enum';
NEW         : 'new';
RETURN      : 'return';
SUPER       : 'super';
THIS        : 'this';
TRY			: 'try';
CATCH		: 'catch';
NULL		: 'null';
VOID 		: 'void';
AMPERSAND	: '&';


// §3.10.1 Integer Literals

IntegerLiteral
	: DecimalIntegerLiteral
//	| HexIntegerLiteral
//	| OctalIntegerLiteral
//	| BinaryIntegerLiteral
	;

fragment
DecimalIntegerLiteral
	: Sign? DecimalNumeral //IntegerTypeSuffix?
	;

//fragment
//HexIntegerLiteral
//	: HexNumeral IntegerTypeSuffix?
//	;
//
//fragment
//OctalIntegerLiteral
//	: OctalNumeral IntegerTypeSuffix?
//	;
//
//fragment
//BinaryIntegerLiteral
//	: BinaryNumeral IntegerTypeSuffix?
//	;

//fragment
//IntegerTypeSuffix
//	: [lL]
//	;

fragment
DecimalNumeral
	: '0'
	| NonZeroDigit (Digits? | Underscores Digits)
	;

fragment
Digits
	: Digit (DigitsAndUnderscores? Digit)?
	;

fragment
Digit
	: '0'
	| NonZeroDigit
	;

fragment
NonZeroDigit
	: [1-9]
	;

fragment
DigitsAndUnderscores
	: DigitOrUnderscore+
	;

fragment
DigitOrUnderscore
	: Digit
	| '_'
	;

fragment
Underscores
	: '_'+
	;

//fragment
//HexNumeral
//	: '0' [xX] HexDigits
//	;
//
//fragment
//HexDigits
//	: HexDigit (HexDigitsAndUnderscores? HexDigit)?
//	;
//
fragment // for encoded characters
HexDigit
	: [0-9a-fA-F]
	;
//
//fragment
//HexDigitsAndUnderscores
//	: HexDigitOrUnderscore+
//	;
//
//fragment
//HexDigitOrUnderscore
//	: HexDigit
//	| '_'
//	;
//
//fragment
//OctalNumeral
//	: '0' Underscores? OctalDigits
//	;
//
//fragment
//OctalDigits
//	: OctalDigit (OctalDigitsAndUnderscores? OctalDigit)?
//	;
//
fragment
OctalDigit
	: [0-7]
	;
//
//fragment
//OctalDigitsAndUnderscores
//	: OctalDigitOrUnderscore+
//	;
//
//fragment
//OctalDigitOrUnderscore
//	: OctalDigit
//	| '_'
//	;
//
//fragment
//BinaryNumeral
//	: '0' [bB] BinaryDigits
//	;
//
//fragment
//BinaryDigits
//	: BinaryDigit (BinaryDigitsAndUnderscores? BinaryDigit)?
//	;
//
//fragment
//BinaryDigit
//	: [01]
//	;
//
//fragment
//BinaryDigitsAndUnderscores
//	: BinaryDigitOrUnderscore+
//	;
//
//fragment
//BinaryDigitOrUnderscore
//	: BinaryDigit
//	| '_'
//	;

// §3.10.2 Floating-Point Literals

FloatingPointLiteral
	: DecimalFloatingPointLiteral
//	| HexadecimalFloatingPointLiteral
	;

fragment
DecimalFloatingPointLiteral
	: Digits '.' Digits? ExponentPart? FloatTypeSuffix?
	| '.' Digits ExponentPart? FloatTypeSuffix?
	| Digits ExponentPart FloatTypeSuffix?
	| Digits FloatTypeSuffix
	;

fragment
ExponentPart
	: ExponentIndicator SignedInteger
	;

fragment
ExponentIndicator
	: [eE]
	;

fragment
SignedInteger
	: Sign? Digits
	;

fragment
Sign
	: [+-]
	;

fragment
FloatTypeSuffix
	: [fFdD]
	;

//fragment
//HexadecimalFloatingPointLiteral
//	: HexSignificand BinaryExponent FloatTypeSuffix?
//	;
//
//fragment
//HexSignificand
//	: HexNumeral '.'?
//	| '0' [xX] HexDigits? '.' HexDigits
//	;
//
//fragment
//BinaryExponent
//	: BinaryExponentIndicator SignedInteger
//	;
//
//fragment
//BinaryExponentIndicator
//	: [pP]
//	;

// §3.10.3 Boolean Literals

BooleanLiteral
	: 'true'
	| 'false'
	;

// §3.10.4 Character Literals

CharacterLiteral
	: '\'' SingleCharacter '\''
	| '\'' EscapeSequence '\''
	;

fragment
SingleCharacter
	: ~['\\\r\n]
	;
// §3.10.5 String Literals
StringLiteral
	: '"' StringCharacters? '"'
	;
fragment
StringCharacters
	: StringCharacter+
	;
fragment
StringCharacter
	: ~["\\\r\n]
	| EscapeSequence
	;
// §3.10.6 Escape Sequences for Character and String Literals
fragment
EscapeSequence
	: '\\' [btnfr"'\\]
	| OctalEscape
	|   UnicodeEscape // This is not in the spec but prevents having to preprocess the input
	;

fragment
OctalEscape
	: '\\' OctalDigit
	| '\\' OctalDigit OctalDigit
	| '\\' ZeroToThree OctalDigit OctalDigit
	;

fragment
ZeroToThree
	: [0-3]
	;

// This is not in the spec but prevents having to preprocess the input
fragment
UnicodeEscape
	:   '\\' 'u'+  HexDigit HexDigit HexDigit HexDigit
	;

// §3.11 Separators

LPAREN : '(';
RPAREN : ')';
LBRACE : '{';
RBRACE : '}';
LBRACK : '[';
RBRACK : ']';
LANGLE : '<';
RANGLE : '>';
SEMI : ';';
COMMA : ',';
DOT : '.';
STAR : '*';

// §3.12 Operators

ASSIGN : '=';
GT_EQUAL : '>=';
LT_EQUAL : '<=';
NOT : '!';
COLON : ':';
EQUAL : '==';
NOTEQUAL : '!=';
AND : '&&';
OR : '||';
INC : '++';
DEC : '--';
ADD : '+';
SUB : '-';
DIV : '/';
BITOR : '|';
MOD : '%';

ADD_ASSIGN : '+=';
SUB_ASSIGN : '-=';
MUL_ASSIGN : '*=';
DIV_ASSIGN : '/=';
AND_ASSIGN : '&=';
OR_ASSIGN : '|=';
MOD_ASSIGN : '%=';

CHAIN_ACCESS : '::';
RIGHT_ARROW : '->';

fwd_chain : '>' '>';

// §3.8 Identifiers (must appear after all keywords in the grammar)

Identifier
	: JavaLetter JavaLetterOrDigit*
	;

fragment
JavaLetter
	: [a-zA-Z$_] // these are the "java letters" below 0x7F
	| // covers all characters above 0x7F which are not a surrogate
	~[\u0000-\u007F\uD800-\uDBFF]
	{Character.isJavaIdentifierStart(_input.LA(-1))}?
	| // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
	[\uD800-\uDBFF] [\uDC00-\uDFFF]
	{Character.isJavaIdentifierStart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
	;

fragment
JavaLetterOrDigit
	: [a-zA-Z0-9$_] // these are the "java letters or digits" below 0x7F
	| // covers all characters above 0x7F which are not a surrogate
	~[\u0000-\u007F\uD800-\uDBFF]
	{Character.isJavaIdentifierPart(_input.LA(-1))}?
	| // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
	[\uD800-\uDBFF] [\uDC00-\uDFFF]
	{Character.isJavaIdentifierPart(Character.toCodePoint((char)_input.LA(-2), (char)_input.LA(-1)))}?
	;

//
// Additional symbols not defined in the lexical specification
//

AT : '@';
ELLIPSIS : '...';

//
// Whitespace and comments
//

WS  :  [ \t\r\n\u000C]+ -> skip
	;

COMMENT
	:   '/*' .*? '*/' -> skip
	;

LINE_COMMENT
	:   '//' ~[\r\n]* -> skip
	;
