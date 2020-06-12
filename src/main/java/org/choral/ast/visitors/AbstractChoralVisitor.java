/*
 * Copyright (C) 2019 by Saverio Giallorenzo <saverio.giallorenzo@gmail.com>
 * Copyright (C) 2019 by Fabrizio Montesi <famontesi@gmail.com>
 * Copyright (C) 2019 by Marco Peressotti <marco.peressotti@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.choral.ast.visitors;

import org.choral.ast.CompilationUnit;
import org.choral.ast.ImportDeclaration;
import org.choral.ast.Name;
import org.choral.ast.Node;
import org.choral.ast.body.Class;
import org.choral.ast.body.Enum;
import org.choral.ast.body.*;
import org.choral.ast.expression.*;
import org.choral.ast.statement.*;
import org.choral.ast.type.FormalTypeParameter;
import org.choral.ast.type.FormalWorldParameter;
import org.choral.ast.type.TypeExpression;
import org.choral.ast.type.WorldArgument;

public abstract class AbstractChoralVisitor< T > implements ChoralVisitorInterface< T > {

	protected AbstractChoralVisitor() {
	}

	@Override
	public T visit( CompilationUnit n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( ImportDeclaration n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( Class n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( Enum n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( Interface n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( Statement n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( BlockStatement n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( SelectStatement n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( ScopedExpression n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( ExpressionStatement n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( IfStatement n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( SwitchStatement n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( SwitchArgument< ? > n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( TryCatchStatement n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( NilStatement n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( ReturnStatement n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( Expression n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( AssignExpression n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( BinaryExpression n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( ClassInstantiationExpression n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( EnumCaseInstantiationExpression n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( EnclosedExpression n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( FieldAccessExpression n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( StaticAccessExpression n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( MethodCallExpression n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( Name n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( NotExpression n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( ThisExpression n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( SuperExpression n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( NullExpression n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( BlankExpression n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( VariableDeclarationStatement n ) {
		throw new UnsupportedOperationException();
	}

//	public T visit( LiteralExpression< ? > n ) {
//		throw new UnsupportedOperationException();
//	}

	@Override
	public T visit( LiteralExpression.BooleanLiteralExpression n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( LiteralExpression.DoubleLiteralExpression n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( LiteralExpression.IntegerLiteralExpression n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( LiteralExpression.StringLiteralExpression n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( CaseSignature n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( Field n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( FormalMethodParameter n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( ClassMethodDefinition n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( InterfaceMethodDefinition n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( MethodSignature n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( ConstructorDefinition n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( ConstructorSignature n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( VariableDeclaration n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( TypeExpression n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( WorldArgument n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( FormalWorldParameter n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( FormalTypeParameter n ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T visit( Annotation n ) {
		throw new UnsupportedOperationException();
	}

}
