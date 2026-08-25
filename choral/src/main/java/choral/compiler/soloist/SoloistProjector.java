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

package choral.compiler.soloist;

import choral.ast.Name;
import choral.ast.body.Class;
import choral.ast.body.ClassMethodDefinition;
import choral.ast.body.Enum;
import choral.ast.body.Interface;
import choral.ast.body.InterfaceMethodDefinition;
import choral.ast.body.MethodDefinition;
import choral.ast.statement.Statement;
import choral.ast.type.FormalWorldParameter;
import choral.ast.type.WorldArgument;
import choral.ast.visitors.ChoralVisitor;
import choral.ast.visitors.PrettyPrinterVisitor;
import choral.compiler.merge.MergeException;
import choral.compiler.merge.StatementsMerger;
import choral.exceptions.AstPositionedException;
import choral.exceptions.ChoralException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SoloistProjector extends ChoralVisitor {

	private final WorldArgument w;

	public SoloistProjector( WorldArgument w ) {
		this.w = w;
	}

//	@Override
//	public CompilationUnit visit ( CompilationUnit n ) {
//		List< ImportDeclaration > importDeclarations = new ArrayList<>();
//		List< Interface > interfaces = new ArrayList<>();
//		List< Class > classes = new ArrayList<>();
//		List< Enum > enums = new ArrayList<>();
//		for ( ImportDeclaration i : n.imports() ) {
//			importDeclarations.add( ( ImportDeclaration ) visit( i ) );
//		}
//		for ( Interface i : n.interfaces() ) {
//			interfaces.add( visit( i ) );
//		}
//		for ( Enum e : n.enums() ) {
//			enums.add( visit( e ) );
//		}
//		for ( Class c : n.classes() ) {
//			classes.add( visit( c ) );
//		}
//		return new CompilationUnit( importDeclarations, interfaces, classes, enums );
//	}

	@Override
	public Interface visit( Interface n ) {
		Utils.warnIfWorldNotPresent( n.worldParameters(), w, n );
		return new Interface(
				new Name( Utils.getProjectionName( n.name().identifier(), w,
						n.worldParameters().stream()
								.map( FormalWorldParameter::toWorldArgument )
								.collect( Collectors.toList() ) ) ),
				Collections.emptyList(),
				TypesProjector.visitAndCollect( w, n.typeParameters() ),
				TypesProjector.visitAndCollect( w, n.extendsInterfaces() ),
				deduplicateMethods( BodyProjector.visitAndCollect( w, n.methods() ) ),
				BodyProjector.visitAndCollect( w, n.annotations() ),
				n.modifiers(),
				n.position()
		);
	}

	@Override
	public Enum visit( Enum n ) {
		Utils.warnIfWorldNotPresent( n.worldParameters(), w, n );
		return new Enum(
				new Name( n.name().identifier() ),
				n.worldParameters().get( 0 ),
				new ArrayList<>( n.cases() ),
				new ArrayList<>( n.annotations() ),
				EnumSet.copyOf( n.modifiers() ),
				n.position()
		);
	}

	@Override
	public Class visit( Class n ) {
		Utils.warnIfWorldNotPresent( n.worldParameters(), w, n );
		Name name = new Name( Utils.getProjectionName( n.name().identifier(), w,
				n.worldParameters().stream()
						.map( FormalWorldParameter::toWorldArgument )
						.collect( Collectors.toList() ) ) );
		return new Class(
				name,
				Collections.emptyList(),
				TypesProjector.visitAndCollect( w, n.typeParameters() ),
				n.superClass().isPresent() ?
						TypesProjector.visit( w, n.superClass().get() ).get( 0 ) // this is always 1
						: null,
				TypesProjector.visitAndCollect( w, n.implementsInterfaces() ),
				BodyProjector.visitAndCollect( w, n.fields() ), // create
				deduplicateMethods( BodyProjector.visitAndCollect( w, n.methods() ) ),
				BodyProjector.visitAndCollect( w, n.constructors() ),
				BodyProjector.visitAndCollect( w, n.annotations() ),
				n.modifiers(),
				n.position()
		);
	}

	private < T extends MethodDefinition > List< T > deduplicateMethods( List< T > methods ) {
		Map< String, T > methodsBySignature = new LinkedHashMap<>();
		PrettyPrinterVisitor printer = new PrettyPrinterVisitor();
		for( T method : methods ) {
			String parameterTypes = method.signature().parameters().stream()
					.map( parameter -> printer.visit( parameter.type() ) )
					.collect( Collectors.joining( ",", "(", ")" ) );
			String signature = method.signature().name().identifier() + parameterTypes;
			T previous = methodsBySignature.get( signature );
			if( previous == null ) {
				methodsBySignature.put( signature, method );
				continue;
			}

			Optional< Statement > previousBody = projectedBody( previous );
			Optional< Statement > currentBody = projectedBody( method );
			if( previousBody.isEmpty() && currentBody.isEmpty() ) {
				continue;
			}
			try {
				if( previousBody.isEmpty() || currentBody.isEmpty() ) {
					throw new MergeException(
							"one projected method has no body", previous, method );
				}
				Statement mergedBody = StatementsMerger.merge(
						List.of( previousBody.get(), currentBody.get() ) );
				methodsBySignature.put( signature, withBody( previous, mergedBody ) );
			} catch( MergeException e ) {
				String message = "Two methods at role '" + w
						+ "' both have projected signature '" + signature
						+ "' but their projected bodies cannot be merged: " + e.getMessage()
						+ "\n\n"
						+ "--- first projected body ---\n"
						+ previousBody.map( printer::visit ).orElse( "<no body>" ) + "\n\n"
						+ "--- second projected body ---\n"
						+ currentBody.map( printer::visit ).orElse( "<no body>" );
				throw new AstPositionedException(
						method.position(), new ChoralException( message ) );
			}
		}
		return new ArrayList<>( methodsBySignature.values() );
	}


	@SuppressWarnings( "unchecked" )
	private < T extends MethodDefinition > T withBody( T method, Statement body ) {
		if( method instanceof ClassMethodDefinition classMethod ) {
			return (T) new ClassMethodDefinition(
					classMethod.signature(), body, classMethod.annotations(),
					classMethod.modifiers(), classMethod.position() );
		}
		if( method instanceof InterfaceMethodDefinition interfaceMethod ) {
			return (T) new InterfaceMethodDefinition(
					interfaceMethod.signature(), body, interfaceMethod.annotations(),
					interfaceMethod.modifiers(), interfaceMethod.position() );
		}
		throw new SoloistProjectorException( "Unsupported projected method definition" );
	}

	private Optional< Statement > projectedBody( MethodDefinition method ) {
		if( method instanceof ClassMethodDefinition classMethod ) {
			return classMethod.body();
		}
		if( method instanceof InterfaceMethodDefinition interfaceMethod ) {
			return interfaceMethod.body();
		}
		throw new SoloistProjectorException( "Unsupported projected method definition" );
	}

}
