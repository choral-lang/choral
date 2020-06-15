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

package org.choral.compiler.courtesyMethodSinthesiser;

import org.choral.ast.CompilationUnit;
import org.choral.ast.Name;
import org.choral.ast.Node;
import org.choral.ast.body.*;
import org.choral.ast.body.Class;
import org.choral.ast.expression.*;
import org.choral.ast.statement.ExpressionStatement;
import org.choral.ast.statement.NilStatement;
import org.choral.ast.statement.ReturnStatement;
import org.choral.ast.statement.Statement;
import org.choral.ast.type.FormalWorldParameter;
import org.choral.ast.type.TypeExpression;
import org.choral.ast.visitors.AbstractChoralVisitor;
import org.choral.compiler.unitNormaliser.UnitRepresentation;

import java.util.*;
import java.util.stream.Collectors;

public class CourtesyMethodsSynthesiser extends AbstractChoralVisitor< Node > {

	private CourtesyMethodsSynthesiser() {
	}

	private final List< ClassMethodDefinition > syntheticMethods = new ArrayList<>();
	private final List< ConstructorDefinition > syntheticConstructors = new ArrayList<>();
	private final List< InterfaceMethodDefinition > syntheticInterfaceMethods = new ArrayList<>();

	public static CompilationUnit visitCompilationUnit( CompilationUnit n ) {
		return new CompilationUnit(
				n.packageDeclaration(),
				n.imports(),
				n.interfaces().stream().map( CourtesyMethodsSynthesiser::visitInterface ).collect(
						Collectors.toList() ),
				n.classes().stream().map( CourtesyMethodsSynthesiser::visitClass ).collect(
						Collectors.toList() ),
				n.enums(),
				""
		);
	}

	private static Class visitClass( Class n ) {
		CourtesyMethodsSynthesiser v = new CourtesyMethodsSynthesiser();
		List< ClassMethodDefinition > methods = v.visitAndCollect( n.methods() );
		methods.addAll( v.syntheticMethods );
		List< ConstructorDefinition > constructors = v.visitAndCollect( n.constructors() );
		constructors.addAll( v.syntheticConstructors );
		return new Class( n.name(),
				n.worldParameters(), n.typeParameters(), n.extendsClass(), n.implementsInterfaces(),
				v.visitAndCollect( n.fields() ), methods, constructors, n.annotations(),
				n.modifiers(), n.position() );
	}

	private static Interface visitInterface( Interface n ) {
		CourtesyMethodsSynthesiser v = new CourtesyMethodsSynthesiser();
		List< InterfaceMethodDefinition > methods = v.visitAndCollect( n.methods() );
		methods.addAll( v.syntheticInterfaceMethods );
		return new Interface( n.name(), n.worldParameters(), n.typeParameters(),
				n.extendsInterfaces(), methods, n.annotations(), n.modifiers(), n.position() );
	}


	@Override
	public Field visit( Field n ) {
		return n;
	}

	private Set< Name > _visit( MethodSignature n ) {
		return n.parameters().stream()
				.filter( p -> p.type().name().equals( UnitRepresentation.UNIT ) )
				.map( FormalMethodParameter::name )
				.collect( Collectors.toSet() );
	}

	private Set< Name > _visit( ConstructorSignature n ) {
		return n.parameters().stream()
				.filter( p -> p.type().name().equals( UnitRepresentation.UNIT ) )
				.map( FormalMethodParameter::name )
				.collect( Collectors.toSet() );
	}

	@Override
	public ConstructorSignature visit( ConstructorSignature n ) {
		return n;
	}

	@Override
	public ClassMethodDefinition visit( ClassMethodDefinition n ) {
		Set< Name > unitParameters = _visit( n.signature() );
		if( !unitParameters.isEmpty() && !n.modifiers().contains( ClassMethodModifier.PRIVATE ) ) {
			MethodSignature sytheticMethodSignature = new MethodSignature(
					n.signature().name(),
					n.signature().typeParameters(),
					n.signature().parameters().stream() // we remove Unit parameters
							.filter( p -> !unitParameters.contains( p.name() ) )
							.collect( Collectors.toList() ),
					n.signature().returnType(),
					n.position()
			);
			List< Expression > parameterBypass = n.signature().parameters().stream()
					.filter( p -> !unitParameters.contains(
							p.name() ) ) // we keep only the non-Unit parameters
					.map( p -> new FieldAccessExpression( p.name() ) )
					.collect( Collectors.toList() );
			List< TypeExpression > typeParameters = n.signature().typeParameters().stream()
					.map( ftp -> new TypeExpression(
							ftp.name(),
							ftp.worldParameters().stream()
									.map( FormalWorldParameter::toWorldArgument )
									.collect( Collectors.toList() ),
							Collections.emptyList() )
					).collect( Collectors.toList() );
			syntheticMethods.add( new ClassMethodDefinition(
					sytheticMethodSignature,
					n.body().orElse( null ),
					n.annotations(),
					n.modifiers(),
					n.position()
			) );
			MethodCallExpression proxyMethod = new MethodCallExpression( n.signature().name(),
					parameterBypass, typeParameters );
			Statement proxyStatement = n.signature().returnType().name().identifier().equals(
					"void" ) ?
					new ExpressionStatement( proxyMethod, new NilStatement() )
					: new ReturnStatement( proxyMethod, new NilStatement() );
			return new ClassMethodDefinition(
					visit( n.signature() ),
					n.body().isPresent() ? proxyStatement : null,
					n.annotations(),
					n.modifiers(),
					n.position()
			);
		} else {
			return new ClassMethodDefinition(
					visit( n.signature() ),
					n.body().orElse( null ),
					n.annotations(),
					n.modifiers(),
					n.position()
			);
		}
	}

	@Override
	public InterfaceMethodDefinition visit( InterfaceMethodDefinition n ) {
		// lots of commented code needed for static and default methods (not supported yet)
		Set< Name > unitParameters = _visit( n.signature() );
		if( !unitParameters.isEmpty() ) {
			MethodSignature methodSignature = new MethodSignature(
					n.signature().name(),
					n.signature().typeParameters(),
					n.signature().parameters().stream() // we remove Unit parameters
							.filter( p -> unitParameters.contains( n.signature().name() ) )
							.collect( Collectors.toList() ),
					n.signature().returnType(),
					n.position()
			);
//			List< Expression > parameterBypass = n.signature().parameters().stream()
//					.filter( p -> !unitParameters.contains( p.name() ) ) // we keep only the non-Unit parameters
//					.map( p -> new FieldAccessExpression( p.name() ) )
//					.collect( Collectors.toList() );
//			List< TypeExpression > typeParameters = n.signature().typeParameters().stream()
//					.map( ftp -> new TypeExpression(
//							ftp.name(),
//							ftp.worldParameters().stream()
//									.map( FormalWorldParameter::toWorldArgument )
//									.collect( Collectors.toList() ),
//							Collections.emptyList() )
//					).collect( Collectors.toList() );
			syntheticInterfaceMethods.add( new InterfaceMethodDefinition(
					methodSignature,
//					n.body(),
					n.annotations(),
					n.modifiers(),
					n.position()
			) );
//			MethodCallExpression proxyMethod = new MethodCallExpression( n.signature().name(), parameterBypass, typeParameters );
//			Statement proxyStatement = n.signature().returnType().name().identifier().equals( "void" ) ?
//					new ExpressionStatement( proxyMethod, new NilStatement() )
//					: new ReturnStatement( proxyMethod, new NilStatement() );
			return new InterfaceMethodDefinition(
					visit( n.signature() ),
//					proxyStatement,
					n.annotations(),
					n.modifiers(),
					n.position()
			);
		} else {
			return new InterfaceMethodDefinition(
					visit( n.signature() ),
//					n.body(),
					n.annotations(),
					n.modifiers(),
					n.position()
			);
		}
	}


	@Override
	public MethodSignature visit( MethodSignature n ) {
//		Set< Name > unitParameters = _visit( n );
//		if ( !unitParameters.isEmpty() ) {
//			syntheticInterfaceMethods.add( new MethodSignature(
//					n.name(),
//					n.typeParameters(),
//					n.parameters().stream() // we remove Unit parameters
//							.filter( p -> unitParameters.contains( n.name() ) )
//							.collect( Collectors.toList() ),
//					n.returnType(),
//					n.position()
//			) );
//		}
		return n;
	}

	@Override
	public ConstructorDefinition visit( ConstructorDefinition n ) {
		Set< Name > unitParameters = _visit( n.signature() );
		if( !unitParameters.isEmpty() && !n.modifiers().contains( ConstructorModifier.PRIVATE ) ) {
			ConstructorSignature constructorSignature = new ConstructorSignature(
					n.signature().name(),
					n.signature().typeParameters(),
					n.signature().parameters().stream() // we remove Unit parameters
							.filter( p -> !unitParameters.contains( p.name() ) )
							.collect( Collectors.toList() ),
					n.position()
			);
			List< Expression > parameterBypass = n.signature().parameters().stream()
					.filter( p -> !unitParameters.contains( p.name() ) )
					.map( p -> // unitParameters.contains( p.name() ) ?
							//UnitRepresentation.UnitFD( new WorldArgument( new Name( "" ) ) )
							//				:
							new FieldAccessExpression( p.name() ) )
					.collect( Collectors.toList() );
			MethodCallExpression proxyConstructor =
					new MethodCallExpression( new Name( "this" ),
							parameterBypass,
							Collections.emptyList() ); // TODO: fix this, we need to have the type parameters of the class
			syntheticConstructors.add( new ConstructorDefinition(
					constructorSignature,
					n.body(),
					n.modifiers(),
					n.position()
			) );
			return new ConstructorDefinition(
					visit( n.signature() ),
					new ExpressionStatement( proxyConstructor, new NilStatement() ),
					n.modifiers(),
					n.position()
			);
		}
		return new ConstructorDefinition(
				visit( n.signature() ),
				n.body(),
				n.modifiers(),
				n.position()
		);
	}

	private < T extends Node > List< T > visitAndCollect( List< T > n ) {
		return n.stream().map( this::safeVisit ).filter( Objects::nonNull ).collect(
				Collectors.toList() );
	}

	@SuppressWarnings( "unchecked cast" )
	private < T extends Node > T safeVisit( T n ) {
		return (T) n.accept( this );
	}

}
