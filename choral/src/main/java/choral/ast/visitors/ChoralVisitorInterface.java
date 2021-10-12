/*
 *   Copyright (C) 2019 by Saverio Giallorenzo <saverio.giallorenzo@gmail.com>
 *   Copyright (C) 2019 by Fabrizio Montesi <famontesi@gmail.com>
 *   Copyright (C) 2019 by Marco Peressotti <marco.peressotti@gmail.com>
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as
 *   published by the Free Software Foundation; either version 2 of the
 *   License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the
 *   Free Software Foundation, Inc.,
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package choral.ast.visitors;

import choral.ast.CompilationUnit;
import choral.ast.ImportDeclaration;
import choral.ast.Name;
import choral.ast.body.*;
import choral.ast.expression.*;
import choral.ast.statement.*;
import choral.ast.type.FormalTypeParameter;
import choral.ast.type.FormalWorldParameter;
import choral.ast.type.TypeExpression;
import choral.ast.type.WorldArgument;

public interface ChoralVisitorInterface< R > {

	R visit( CompilationUnit n );

	R visit( ImportDeclaration n );

	R visit( choral.ast.body.Class n );

	R visit( choral.ast.body.Enum n );

	R visit( Interface n );

	R visit( Statement n );

	R visit( BlockStatement n );

	R visit( SelectStatement n );

	R visit( ScopedExpression n );

	R visit( ExpressionStatement n );

	R visit( IfStatement n );

	R visit( SwitchStatement n );

	R visit( TryCatchStatement n );

	R visit( NilStatement n );

	R visit( ReturnStatement n );

	R visit( Expression n );

	R visit( AssignExpression n );

	R visit( BinaryExpression n );

	R visit( EnumCaseInstantiationExpression n );

	R visit( EnclosedExpression n );

	R visit( FieldAccessExpression n );

	R visit( StaticAccessExpression n );

	R visit( MethodCallExpression n );

	R visit( ClassInstantiationExpression n );

	R visit( Name n );

	R visit( NotExpression n );

	R visit( ThisExpression n );

	R visit( SuperExpression n );

	R visit( NullExpression n );

	R visit( VariableDeclarationStatement n );

	R visit( BlankExpression n );

	R visit( LiteralExpression.BooleanLiteralExpression n );

	R visit( LiteralExpression.DoubleLiteralExpression n );

	R visit( LiteralExpression.IntegerLiteralExpression n );

	R visit( LiteralExpression.StringLiteralExpression n );

	R visit( SwitchArgument< ? > n );

	R visit( CaseSignature n );

	R visit( Field n );

	R visit( FormalMethodParameter n );

	R visit( ClassMethodDefinition n );

	R visit( InterfaceMethodDefinition n );

	R visit( MethodSignature n );

	R visit( ConstructorDefinition n );

	R visit( ConstructorSignature n );

	R visit( VariableDeclaration n );

	R visit( TypeExpression n );

	R visit( WorldArgument n );

	R visit( FormalTypeParameter n );

	R visit( FormalWorldParameter n );

	R visit( Annotation n );
}
