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
import choral.ast.Node;
import choral.ast.body.Class;
import choral.ast.body.Enum;
import choral.ast.body.*;
import choral.ast.expression.*;
import choral.ast.statement.*;
import choral.ast.type.FormalTypeParameter;
import choral.ast.type.FormalWorldParameter;
import choral.ast.type.TypeExpression;
import choral.ast.type.WorldArgument;
import choral.utils.Pair;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChoralVisitor implements ChoralVisitorInterface< Node > {

	@Override
	public Node visit( CompilationUnit n ) {
		List< ImportDeclaration > importDeclarations = new ArrayList<>();
		List< Interface > interfaces = new ArrayList<>();
		List< Class > classes = new ArrayList<>();
		List< Enum > enums = new ArrayList<>();
		for( ImportDeclaration i : n.imports() ) {
			importDeclarations.add( (ImportDeclaration) visit( i ) );
		}
		for( Interface i : n.interfaces() ) {
			interfaces.add( (Interface) visit( i ) );
		}
		for( Class c : n.classes() ) {
			classes.add( (Class) visit( c ) );
		}
		for( Enum e : n.enums() ) {
			enums.add( (Enum) visit( e ) );
		}
		return new CompilationUnit( n.packageDeclaration(), importDeclarations, interfaces, classes,
				enums, n.position().sourceFile() );
	}

	@Override
	public Node visit( ImportDeclaration n ) {
		return new ImportDeclaration(
				n.name(),
				n.position()
		);
	}

	@Override
	public Node visit( ExpressionStatement n ) {
		return new ExpressionStatement(
				safeVisit( n.expression() ),
				safeVisit( n.continuation() ),
				n.position()
		);
	}

	@Override
	public Node visit( IfStatement n ) {
		return new IfStatement(
				safeVisit( n.condition() ),
				safeVisit( n.ifBranch() ),
				safeVisit( n.elseBranch() ),
				safeVisit( n.continuation() ),
				n.position()
		);
	}

	@Override
	public Node visit( SwitchStatement n ) {
		return new SwitchStatement(
				safeVisit( n.guard() ),
				n.cases().entrySet().stream().map( e ->
						new AbstractMap.SimpleEntry< SwitchArgument< ? >, Statement >(
								safeVisit( e.getKey() ), safeVisit( e.getValue() )
						) {
						} ).collect( Collectors.toMap( Map.Entry::getKey, Map.Entry::getValue )
				),
				safeVisit( n.continuation() ),
				n.position()
		);
	}

	@Override
	public Node visit( SwitchArgument< ? > n ) {
		if( n instanceof SwitchArgument.SwitchArgumentLiteral ) {
			return new SwitchArgument.SwitchArgumentLiteral(
					( (SwitchArgument.SwitchArgumentLiteral) n ).argument() );
		} else {
			if( n instanceof SwitchArgument.SwitchArgumentLabel ) {
				return new SwitchArgument.SwitchArgumentLabel(
						( (SwitchArgument.SwitchArgumentLabel) n ).argument() );
			} else {
				// the default case
				return n;
			}
		}
	}

	@Override
	public Node visit( TryCatchStatement n ) {
		return new TryCatchStatement(
				safeVisit( n.body() ),
				n.catches().stream().map( c ->
						new Pair<>( safeVisit( c.left() ), safeVisit( c.right() ) )
				).collect( Collectors.toList() ), safeVisit( n.continuation() ),
				n.position()
		);
	}

	@Override
	public Node visit( NilStatement n ) {
		return n;
	}

	@Override
	public Node visit( ReturnStatement n ) {
		return new ReturnStatement(
				safeVisit( n.returnExpression() ),
				safeVisit( n.continuation() ),
				n.position()
		);
	}

	@Override
	public Node visit( Expression n ) {
		return n.accept( this );
	}

	@Override
	public Node visit( AssignExpression n ) {
		return new AssignExpression(
				safeVisit( n.value() ),
				safeVisit( n.target() ),
				n.operator(),
				n.position()
		);
	}

	@Override
	public Node visit( BinaryExpression n ) {
		return new BinaryExpression(
				safeVisit( n.left() ),
				safeVisit( n.right() ),
				n.operator(),
				n.position()
		);
	}

	@Override
	public Node visit( ClassInstantiationExpression n ) {
		return new ClassInstantiationExpression(
				safeVisit( n.typeExpression() ),
				visitAndCollect( n.arguments() ),
				visitAndCollect( n.typeArguments() ),
				n.position()
		);
	}

	@Override
	public Node visit( EnumCaseInstantiationExpression n ) {
		return new EnumCaseInstantiationExpression(
				safeVisit( n.name() ),
				n._case(),
				safeVisit( n.world() ),
				n.position()
		);
	}

	@Override
	public Node visit( EnclosedExpression n ) {
		return new EnclosedExpression(
				safeVisit( n.nestedExpression() ),
				n.position()
		);
	}

	@Override
	public Node visit( FieldAccessExpression n ) {
		return new FieldAccessExpression(
				safeVisit( n.name() ),
				n.position()
		);
	}

	@Override
	public Node visit( StaticAccessExpression n ) {
		return new StaticAccessExpression(
				safeVisit( n.typeExpression() ),
				n.position()
		);
	}

	@Override
	public Node visit( MethodCallExpression n ) {
		return new MethodCallExpression(
				safeVisit( n.name() ),
				visitAndCollect( n.arguments() ),
				visitAndCollect( n.typeArguments() ),
				n.position()
		);
	}

	@Override
	public Node visit( Name n ) {
		return new Name(
				n.identifier(),
				n.position()
		);
	}

	@Override
	public Node visit( NotExpression n ) {
		return new NotExpression(
				safeVisit( n.expression() ),
				n.position()
		);
	}

	@Override
	public Node visit( ThisExpression n ) {
		return n;
	}

	@Override
	public Node visit( SuperExpression n ) {
		return n;
	}

	@Override
	public Node visit( NullExpression n ) {
		return n;
	}

	@Override
	public Node visit( VariableDeclarationStatement n ) {
		return new VariableDeclarationStatement(
				visitAndCollect( n.variables() ),
				safeVisit( n.continuation() ),
				n.position()
		);
	}

	@Override
	public Node visit( BlankExpression n ) {
		return n;
	}

	public Node visit( LiteralExpression< ? > n ) {
		return n;
	}

	@Override
	public Node visit( LiteralExpression.BooleanLiteralExpression n ) {
		return n;
	}

	@Override
	public Node visit( LiteralExpression.DoubleLiteralExpression n ) {
		return n;
	}

	@Override
	public Node visit( LiteralExpression.IntegerLiteralExpression n ) {
		return n;
	}

	@Override
	public Node visit( LiteralExpression.StringLiteralExpression n ) {
		return n;
	}

	@Override
	public Node visit( CaseSignature n ) {
		return new CaseSignature(
				n.name(),
				visitAndCollect( n.parameters() ),
				n.position()
		);
	}

	@Override
	public Node visit( Class n ) {
		return new Class(
				n.name(),
				visitAndCollect( n.worldParameters() ),
				visitAndCollect( n.typeParameters() ),
				n.superClass().orElse( null ),
				visitAndCollect( n.implementsInterfaces() ),
				visitAndCollect( n.fields() ),
				visitAndCollect( n.methods() ),
				visitAndCollect( n.constructors() ),
				visitAndCollect( n.annotations() ),
				n.modifiers(),
				n.position()
		);
	}

	@Override
	public Node visit( Enum n ) {
		return new Enum(
				n.name(),
				visitAndCollect( n.worldParameters() ).get( 0 ),
				visitAndCollect( n.cases() ),
				visitAndCollect( n.annotations() ),
				n.modifiers(),
				n.position()
		);
	}

	@Override
	public Node visit( Field n ) {
		return new Field(
				n.name(),
				safeVisit( n.typeExpression() ),
				n.modifiers(),
				n.position()
		);
	}

	@Override
	public Node visit( FormalMethodParameter n ) {
		return new FormalMethodParameter(
				n.name(),
				safeVisit( n.type() ),
				n.position()
		);
	}

	@Override
	public Node visit( Interface n ) {
		return new Interface(
				n.name(),
				visitAndCollect( n.worldParameters() ),
				visitAndCollect( n.typeParameters() ),
				visitAndCollect( n.extendsInterfaces() ),
				visitAndCollect( n.methods() ),
				visitAndCollect( n.annotations() ),
				n.modifiers(),
				n.position()
		);
	}

	@Override
	public Node visit( Statement n ) {
		return n.accept( this ).copyPosition( n );
	}

	@Override
	public Node visit( BlockStatement n ) {
		return new BlockStatement(
				safeVisit( n.enclosedStatement() ),
				safeVisit( n.continuation() ),
				n.position()
		);
	}

	@Override
	public Node visit( SelectStatement n ) {
		return new SelectStatement(
				safeVisit( n.enumConstructor() ),
				safeVisit( n.channelExpression() ),
				safeVisit( n.continuation() ),
				n.position()
		);
	}

	@Override
	public Node visit( ScopedExpression n ) {
		return new ScopedExpression(
				safeVisit( n.scope() ),
				safeVisit( n.scopedExpression() ),
				n.position()
		);
	}

	@Override
	public Node visit( ClassMethodDefinition n ) {
		return new ClassMethodDefinition(
				safeVisit( n.signature() ),
				n.body().map( this::safeVisit ).orElse( null ),
				visitAndCollect( n.annotations() ),
				n.modifiers(),
				n.position()
		);
	}

	@Override
	public Node visit( InterfaceMethodDefinition n ) {
		return new InterfaceMethodDefinition(
				safeVisit( n.signature() ),
//                safeVisit(n.body()),
				visitAndCollect( n.annotations() ),
				n.modifiers(),
				n.position()
		);
	}

	@Override
	public Node visit( MethodSignature n ) {
		return new MethodSignature(
				n.name(),
				visitAndCollect( n.typeParameters() ),
				visitAndCollect( n.parameters() ),
				safeVisit( n.returnType() ),
				n.position()
		);
	}

	@Override
	public Node visit( ConstructorDefinition n ) {
		return new ConstructorDefinition(
				safeVisit( n.signature() ),
				n.explicitConstructorInvocation().map( this::safeVisit ).orElse( null ),
				safeVisit( n.blockStatements() ),
				visitAndCollect( n.annotations() ),
				n.modifiers(),
				n.position()
		);
	}

	@Override
	public Node visit( ConstructorSignature n ) {
		return new ConstructorSignature(
				n.name(),
				visitAndCollect( n.typeParameters() ),
				visitAndCollect( n.parameters() ),
				n.position()
		);
	}

	@Override
	public Node visit( VariableDeclaration n ) {
		return new VariableDeclaration(
				safeVisit( n.name() ),
				safeVisit( n.type() ),
				safeVisit( n.initializer().get() ),
				n.position()
		);
	}

	@Override
	public Node visit( TypeExpression n ) {
		return new TypeExpression(
				n.name(),
				visitAndCollect( n.worldArguments() ),
				visitAndCollect( n.typeArguments() ),
				n.position()
		);
	}

	@Override
	public Node visit( WorldArgument n ) {
		return new WorldArgument( n.name(),
				n.position()
		);
	}

	@Override
	public Node visit( FormalWorldParameter n ) {
		return new FormalWorldParameter( n.name(),
				n.position()
		);
	}

	@Override
	public Node visit( Annotation n ) {
		return new Annotation( n.getName(),
				n.getValues(),
				n.position()
		);
	}

	@Override
	public Node visit( FormalTypeParameter n ) {
		return new FormalTypeParameter(
				n.name(),
				visitAndCollect( n.worldParameters() ),
				visitAndCollect( n.upperBound() ),
				n.position()
		);
	}

	// - - - - - - - - - UTILITY - - - - - - - - - - -

	@SuppressWarnings( "unchecked cast" )
	private < T extends Node > List< T > visitAndCollect( List< T > n ) {
		return n.stream().map( e -> (T) e.accept( this ) ).collect( Collectors.toList() );
	}

	@SuppressWarnings( "unchecked cast" )
	private < T extends Node > T safeVisit( T n ) {
		return n == null ? null : (T) n.accept( this );
	}

}
