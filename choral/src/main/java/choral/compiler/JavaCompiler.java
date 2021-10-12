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

package choral.compiler;

import choral.ast.CompilationUnit;
import choral.ast.body.*;
import choral.ast.expression.*;
import choral.ast.statement.*;
import choral.ast.type.FormalTypeParameter;
import choral.ast.type.TypeExpression;
import choral.ast.visitors.PrettyPrinterVisitor;
import choral.ast.visitors.templates.Utils;
import choral.compiler.SourceObject.JavaSourceObject;
import choral.compiler.soloist.StatementsProjector;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class JavaCompiler extends PrettyPrinterVisitor {

	private static final String NEWLINE = "\n";
	private static final String _2NEWLINE = NEWLINE + NEWLINE;
	private static final String COMMA = ",";
	private static final String SPACED_COMMA = COMMA + " ";
	private static final String SEMICOLON = ";";
	private static final String IMPLEMENTS = "implements";
	private static final String EXTENDS = "extends";
	private static final String PACKAGE = "package";
	private static final String TAB = "\t";

	public static Collection< JavaSourceObject > compile( CompilationUnit n ) {
		List< JavaSourceObject > c = new LinkedList<>();
		JavaCompiler jc = new JavaCompiler();
		Path sourcePath = Paths.get( n.position().sourceFile() );
		String imports = jc.visitAndCollect( n.imports(), SEMICOLON + NEWLINE,
				SEMICOLON + _2NEWLINE );
		String packageDeclaration = n.packageDeclaration().length() > 0 ? PACKAGE + " " + n.packageDeclaration() + SEMICOLON + NEWLINE : "";
		n.interfaces().forEach( x -> c.add(
				new JavaSourceObject( packageDeclaration + imports + jc.visit( x ),
						JavaSourceObject.combineName( n.packageDeclaration(),
								x.name().identifier() ),
						sourcePath.resolveSibling(
								x.name().identifier() + JavaSourceObject.FILE_EXTENSION ).toString() ) ) );
		n.enums().forEach( x -> c.add(
				new JavaSourceObject( packageDeclaration + imports + jc.visit( x ),
						JavaSourceObject.combineName( n.packageDeclaration(),
								x.name().identifier() ),
						sourcePath.resolveSibling(
								x.name().identifier() + JavaSourceObject.FILE_EXTENSION ).toString() ) ) );
		n.classes().forEach( x -> c.add(
				new JavaSourceObject( packageDeclaration + imports + jc.visit( x ),
						JavaSourceObject.combineName( n.packageDeclaration(),
								x.name().identifier() ),
						sourcePath.resolveSibling(
								x.name().identifier() + JavaSourceObject.FILE_EXTENSION ).toString() ) ) );
		return c;
	}


	@Override
	public String visit( CompilationUnit n ) {
		throw new UnsupportedOperationException(
				"The Java compiler must be called only via the 'compile' method" );
	}

	@Override
	protected String visitTypeDeclaration( RefType n ) {
		StringBuilder s = new StringBuilder();
		s.append( n.name() );
		if( !n.typeParameters().isEmpty() ) {
			s.append( "< " );
			s.append( visitAndCollect( n.typeParameters(), SPACED_COMMA ) );
			s.append( " >" );
		}
		return s.toString();
	}

	@Override
	public String visit( TryCatchStatement n ) {
		if( n.catches().isEmpty() ) {
			String body = visit( n.body() );
			if( body.trim().length() < 1 ) {
				return "";
			}
			HashMap< String, Object > m = new HashMap<>();
			StringBuilder template = new StringBuilder();
			template.append( "{" + NEWLINE +
					"$body" + NEWLINE +
					"}" );
			m.put( "body", indent( visit( n.body() ) ) );
			return Utils.createVelocityTemplate( template.toString() )
					.render( m ) + getContinuation( n, "" );
		} else {
			return super.visit( n );
		}
	}


//	private String compileCase( CaseSignature c, String extendsEnum ) {
//		HashMap< String, Object > m = new HashMap<>();
//		m.put( "name", c.name() );
//		m.put( "super", extendsEnum );
//		m.put( "fields", indent( c.parameters().stream().map( p ->
//										"public final " + visit( p.type() ) + " " + p.name()
//						).collect( Collectors.joining( SEMICOLON + NEWLINE ) ) )
//		);
//		m.put( "constructorBody", indent( c.parameters().stream().map( p ->
//										TAB + "this." + p.name() + " = " + p.name()
//						).collect( Collectors.joining( SEMICOLON + NEWLINE ) ) )
//		);
//
//		String template =
//						"public static final class $name extends $super {" + NEWLINE +
//										"#if( $!{fields.trim()} != '' )$fields" + SEMICOLON + NEWLINE + "#end" +
//										TAB + "public " + c.name() +
//										( c.parameters().isEmpty() ? "()" : "( " + visitAndCollect( c.parameters(), SPACED_COMMA ) + " )" ) + "{" +
//										"#if( $!{constructorBody.trim()} != '' )" + _2NEWLINE + "$constructorBody" + SEMICOLON + NEWLINE + TAB + "#end" +
//										"}" + NEWLINE + "}";
//		return Utils.createVelocityTemplate( template ).render( m );
//	}

	@Override
	public String visit( SwitchStatement n ) {
		HashMap< String, Object > m = new HashMap<>();

		m.put( "guard", visit( n.guard() ) );
		String cases = n.cases().entrySet().stream().map(
				e -> {
					String caseSwitch, body;
					if( e.getKey().equals( StatementsProjector.SELECT_DEFAULT ) ) {
						caseSwitch = "default -> ";
						body = "throw new RuntimeException( \"Received unexpected label from select operation\" );";
					} else {
						caseSwitch = visit( e.getKey() );
						body = visit( e.getValue() );
					}
					return caseSwitch + "{" + NEWLINE + indent( body ) + NEWLINE + "}";
//							+ indent( containsReturn( e.getValue() ) ?
//							""
//							: ( body.length() > 2 ? NEWLINE : "" ) + "break;" // body.length() > 2 accounts for empty case-bodies
//					);
				}
		).collect( Collectors.joining( NEWLINE ) );
		m.put( "cases", indent( cases ) );

		String template = "switch( $guard ){" + NEWLINE +
				"$cases" + NEWLINE +
				"}";
		return Utils.createVelocityTemplate( template )
				.render( m ) + getContinuation( n, "" );
	}

	@Override
	public String visit( ClassInstantiationExpression n ) {
		return
				"new " + ( n.typeArguments().isEmpty() ? "" : "< " + visitAndCollect(
						n.typeArguments(), SPACED_COMMA ) + " >" )
						+ visit( n.typeExpression() )
						+ ( n.arguments().isEmpty() ? "()" : "( " + visitAndCollect( n.arguments(),
						SPACED_COMMA ) + " )" );
	}

	@Override
	public String visit( EnumCaseInstantiationExpression n ) {
		return n.name().identifier() + "." + n._case();
	}

	@Override
	public String visit( SelectStatement n ) {
		return visit( n.channelExpression() ) + ".select( " + visit(
				n.enumConstructor() ) + " )" + getContinuation( n, SEMICOLON );
//		return visit( n.channelExpression() ) + ".com( " + visit( n.enumConstructor() ) + " )" + getContinuation( n, SEMICOLON );
	}

	@Override
	public String visit( MethodSignature n ) {
		return
				( n.typeParameters().isEmpty() ? "" : "< " + visitAndCollect( n.typeParameters(),
						SPACED_COMMA ) + " > " )
						+ visit( n.returnType() )
						+ " " + n.name() +
						( n.parameters().isEmpty() ? "()" : "( " + visitAndCollect( n.parameters(),
								SPACED_COMMA ) + " )" );
	}

	@Override
	public String visit( ConstructorSignature n ) {
		return
				( n.typeParameters().isEmpty() ? "" : "< " + visitAndCollect( n.typeParameters(),
						SPACED_COMMA ) + " > " ) +
						n.name() +
						( n.parameters().isEmpty() ? "()" : "( " + visitAndCollect( n.parameters(),
								SPACED_COMMA ) + " )" );
	}

	@Override
	public String visit( TypeExpression n ) {
		return n.name() +
				( n.typeArguments().isEmpty() ? "" : " < " + visitAndCollect( n.typeArguments(),
						SPACED_COMMA ) + " >" );
	}

	@Override
	public String visit( FormalTypeParameter n ) {
		StringBuilder s = new StringBuilder();
		s.append( n.name() );
		if( !n.upperBound().isEmpty() ) {
			s.append( ' ' ).append( EXTENDS ).append( ' ' ).append(
					n.upperBound().stream().map( this::visit ).collect(
							Collectors.joining( SPACED_COMMA ) )
			);
		}
		return s.toString();
	}

	@Override
	public String visit( LiteralExpression.BooleanLiteralExpression n ) {
		return n.content().toString();
	}

	@Override
	public String visit( LiteralExpression.DoubleLiteralExpression n ) {
		return n.content().toString();
	}

	@Override
	public String visit( LiteralExpression.IntegerLiteralExpression n ) {
		return n.content().toString();
	}

	@Override
	public String visit( LiteralExpression.StringLiteralExpression n ) {
		return n.content();
	}
}
