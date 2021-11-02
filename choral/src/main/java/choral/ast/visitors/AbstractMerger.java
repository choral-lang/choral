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

package choral.ast.visitors;

import choral.ast.CompilationUnit;
import choral.ast.ImportDeclaration;
import choral.ast.Name;
import choral.ast.body.*;
import choral.ast.body.Class;
import choral.ast.body.Enum;
import choral.ast.expression.*;
import choral.ast.statement.*;
import choral.ast.type.FormalTypeParameter;
import choral.ast.type.TypeExpression;

public class AbstractMerger< T > implements MergerInterface< T > {

	@Override
	public T merge( CompilationUnit n1, CompilationUnit n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( ImportDeclaration n1, ImportDeclaration n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( Class n1, Class n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( Enum n1, Enum n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( Interface n1, Interface n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( Statement n1, Statement n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( BlockStatement n1, BlockStatement n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( SelectStatement n1, SelectStatement n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( ScopedExpression n1, ScopedExpression n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( ExpressionStatement n1, ExpressionStatement n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( IfStatement n1, IfStatement n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( SwitchStatement n1, SwitchStatement n2 ) {
		throw new UnsupportedOperationException();
	}

	public T merge( SwitchArgument n1, SwitchArgument n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( TryCatchStatement n1, TryCatchStatement n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( NilStatement n1, NilStatement n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( ReturnStatement n1, ReturnStatement n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( Expression n1, Expression n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( AssignExpression n1, AssignExpression n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( BinaryExpression n1, BinaryExpression n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( ClassInstantiationExpression n1, ClassInstantiationExpression n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( EnumCaseInstantiationExpression n1, EnumCaseInstantiationExpression n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( EnclosedExpression n1, EnclosedExpression n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( FieldAccessExpression n1, FieldAccessExpression n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( StaticAccessExpression n1, StaticAccessExpression n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( MethodCallExpression n1, MethodCallExpression n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( Name n1, Name n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( NotExpression n1, NotExpression n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( ThisExpression n1, ThisExpression n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( NullExpression n1, NullExpression n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( VariableDeclarationStatement n1, VariableDeclarationStatement n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( LiteralExpression n1, LiteralExpression n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( CaseSignature n1, CaseSignature n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( Field n1, Field n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( FormalMethodParameter n1, FormalMethodParameter n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( ClassMethodDefinition n1, ClassMethodDefinition n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( MethodSignature n1, MethodSignature n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( VariableDeclaration n1, VariableDeclaration n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( TypeExpression n1, TypeExpression n2 ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T merge( FormalTypeParameter n1, FormalTypeParameter n2 ) {
		throw new UnsupportedOperationException();
	}

}
