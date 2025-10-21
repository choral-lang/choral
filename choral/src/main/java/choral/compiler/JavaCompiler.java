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
import choral.ast.body.ConstructorSignature;
import choral.ast.body.MethodSignature;
import choral.ast.body.RefType;
import choral.ast.expression.ClassInstantiationExpression;
import choral.ast.expression.EnumCaseInstantiationExpression;
import choral.ast.expression.LiteralExpression;
import choral.ast.statement.SwitchArgument;
import choral.ast.statement.SwitchStatement;
import choral.ast.statement.TryCatchStatement;
import choral.ast.type.FormalTypeParameter;
import choral.ast.type.TypeExpression;
import choral.ast.visitors.PrettyPrinterVisitor;
import choral.ast.visitors.templates.Utils;
import choral.compiler.SourceObject.JavaSourceObject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;

public class JavaCompiler extends PrettyPrinterVisitor {

	private static final String NEWLINE = "\n";
	private static final String _2NEWLINE = NEWLINE + NEWLINE;
	private static final String COMMA = ",";
	private static final String SPACED_COMMA = COMMA + " ";
	private static final String SEMICOLON = ";";
	private static final String AMPERSAND = " & ";
	private static final String EXTENDS = "extends";
	private static final String PACKAGE = "package";

	public static Collection< JavaSourceObject > compile( CompilationUnit n ) {
		List< JavaSourceObject > c = new LinkedList<>();
		JavaCompiler jc = new JavaCompiler();
		Path sourcePath = Paths.get( n.position().sourceFile() );
		String imports = jc.visitAndCollect( n.imports(), SEMICOLON + NEWLINE,
				SEMICOLON + _2NEWLINE );
		String packageDeclaration = n.packageDeclaration().isPresent() ? PACKAGE + " " + n.packageDeclaration().get() + SEMICOLON + _2NEWLINE : "";
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
			String template = "{" + NEWLINE +
					"$body" + NEWLINE +
					"}";
			m.put( "body", indent( visit( n.body() ) ) );
			return Utils.createVelocityTemplate( template )
					.render( m ) + getContinuation( n, "" );
		} else {
			return super.visit( n );
		}
	}

	@Override
	public String visit( SwitchStatement n ) {
		HashMap< String, Object > m = new HashMap<>();

		m.put( "guard", visit( n.guard() ) );
		String cases = n.cases().entrySet().stream().map(
				e -> {
					String caseSwitch, body;
					if( e.getKey().equals( SwitchArgument.SwitchArgumentMergeDefault.getInstance() )) {
						caseSwitch = "default -> ";
						body = "throw new RuntimeException( \"Received unexpected label from select operation\" );";
					} else {
						caseSwitch = visit( e.getKey() );
						body = visit( e.getValue() );
					}
					return caseSwitch + "{" + NEWLINE + indent( body ) + NEWLINE + "}";
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
		s.append( visitAndCollect( n.annotations(), " ", " " ) );
		s.append( n.name() );
		if( !n.upperBound().isEmpty() ) {
			s.append( ' ' ).append( EXTENDS ).append( ' ' ).append(
					visitAndCollect( n.upperBound(), AMPERSAND )
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
