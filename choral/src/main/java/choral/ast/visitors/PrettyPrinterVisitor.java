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
import choral.ast.visitors.templates.Utils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PrettyPrinterVisitor implements ChoralVisitorInterface< String > {

	private static final String NEWLINE = "\n";
	private static final String _2NEWLINE = NEWLINE + NEWLINE;
	private static final String COMMA = ",";
	private static final String SPACED_COMMA = COMMA + " ";
	private static final String SEMICOLON = ";";
	private static final String IMPLEMENTS = "implements";
	private static final String EXTENDS = "extends";
	private static final String AMPERSAND = " & ";
	private static final String IMPORT = "import";
	private static final String PACKAGE = "package";
	private static final String TAB = "\t";
	private static final String ANNOTATION = "@";
	private static final String ASSIGN = " = ";

	@Override
	public String visit( CompilationUnit n ) {
		return ( n.packageDeclaration().isPresent() ? PACKAGE + " " + n.packageDeclaration().get() + SEMICOLON + _2NEWLINE : "" )
				+ visitAndCollect( n.imports(), SEMICOLON + NEWLINE, SEMICOLON + _2NEWLINE )
				+ visitAndCollect( n.interfaces(), NEWLINE, NEWLINE )
				+ visitAndCollect( n.enums(), NEWLINE, NEWLINE )
				+ visitAndCollect( n.classes(), NEWLINE, NEWLINE );
	}

	@Override
	public String visit( ImportDeclaration n ) {
		return IMPORT + " " + n.name();
	}

	protected static < E extends java.lang.Enum< E > > String visitModifiers(
			EnumSet< E > modifiers
	) {
		if( modifiers.size() == 0 ) {
			return "";
		} else {
			return modifiers.stream().map( Object::toString ).collect(
					Collectors.joining( " " ) ) + " ";
		}
	}

	@Override
	public String visit( Interface n ) {
		HashMap< String, Object > m = new HashMap<>();
		m.put( "interface", "interface" );
		m.put( "type", visitTypeDeclaration( n ) );
		m.put( "extends", n.extendsInterfaces().isEmpty() ? "" : m.put( "extends",
				" " + IMPLEMENTS + " " + visitAndCollect( n.extendsInterfaces(), COMMA ) ) );
		m.put( "methods", indent( visitAndCollect( n.methods(), SEMICOLON + NEWLINE,
				SEMICOLON + _2NEWLINE ) ) );
		m.put( "modifiers", visitModifiers( n.modifiers() ) );
		m.put( "annotations", visitAndCollect( n.annotations(), NEWLINE, NEWLINE ) );

		String template = "${annotations}$modifiers$interface $type$extends {" + NEWLINE +
				"#if( $!{methods.trim()} != '')$methods" + NEWLINE + "#end" +
				"}" + NEWLINE;
		return Utils.createVelocityTemplate( template ).render( m );
	}

	@Override
	public String visit( Class n ) {
		HashMap< String, Object > m = new HashMap<>();
		m.put( "class", "class" );
		m.put( "type", visitTypeDeclaration( n ) );
		m.put( "extends", ifPresent( n.superClass().orElse( null ) ).applyOrElse(
				t -> " " + EXTENDS + " " + visit( t ), String::new ) );
		m.put( IMPLEMENTS,
				n.implementsInterfaces().isEmpty() ? "" : " " + IMPLEMENTS + " " + visitAndCollect(
						n.implementsInterfaces(), COMMA ) );
		m.put( "fields",
				indent( visitAndCollect( n.fields(), SEMICOLON + NEWLINE, SEMICOLON + NEWLINE ) ) );
		m.put( "methods", indent( visitAndCollect( n.methods(), _2NEWLINE, _2NEWLINE ) ) );
		m.put( "constructors",
				indent( visitAndCollect( n.constructors(), _2NEWLINE, _2NEWLINE ) ) );
		m.put( "modifiers", visitModifiers( n.modifiers() ) );
		m.put( "annotations", visitAndCollect( n.annotations(), NEWLINE, NEWLINE ) );

		String template = "${annotations}$modifiers$class $type$extends$implements {" + NEWLINE +
				"#if( $!{fields.trim()} != '' )$fields" + _2NEWLINE + "#end" +
				"#if( $!{constructors.trim()} != '' )$constructors" + _2NEWLINE + "#end" +
				"#if( $!{methods.trim()} != '')$methods" + _2NEWLINE + "#end" +
				"}" + NEWLINE;
		return Utils.createVelocityTemplate( template ).render( m );
	}

	@Override
	public String visit( Enum n ) {
		HashMap< String, Object > m = new HashMap<>();
		m.put( "enum", "enum" );
		m.put( "type", visitTypeDeclaration( n ) );
		m.put( "cases", indent( visitAndCollect( n.cases(), COMMA + NEWLINE, NEWLINE ) ) );
		m.put( "modifiers", visitModifiers( n.modifiers() ) );
		m.put( "annotations", visitAndCollect( n.annotations(), NEWLINE, NEWLINE ) );
		String template = "${annotations}$modifiers$enum $type {" + NEWLINE +
				"#if( $!{cases.trim()} != '' )$cases" + NEWLINE + "#end" +
				"}" + NEWLINE;
		return Utils.createVelocityTemplate( template ).render( m );
	}

	@Override
	public String visit( EnumConstant n ) {
		return visitAndCollect( n.annotations(), NEWLINE, NEWLINE ) + n.name();
	}


	protected String visitTypeDeclaration( RefType n ) {
		StringBuilder s = new StringBuilder();
		s.append( n.name() );
		if( !n.worldParameters().isEmpty() ) {
			s.append( '@' );
			if( n.worldParameters().size() > 1 ) {
				s.append( '(' );
			}
			s.append( visitAndCollect( n.worldParameters(), SPACED_COMMA ) );
			if( n.worldParameters().size() > 1 ) {
				s.append( ')' );
			}
		}
		if( !n.typeParameters().isEmpty() ) {
			s.append( '<' );
			s.append( visitAndCollect( n.typeParameters(), SPACED_COMMA ) );
			s.append( '>' );
		}
		return s.toString();
	}


	@Override
	public String visit( ExpressionStatement n ) {
		return visit( n.expression() ) + getContinuation( n, SEMICOLON );
	}

	@Override
	public String visit( IfStatement n ) {
		HashMap< String, Object > m = new HashMap<>();
		StringBuilder template = new StringBuilder();
		template.append( "if( $condition ){" + NEWLINE +
				"$ifBranch" + NEWLINE +
				"}" );
		m.put( "condition", visit( n.condition() ) );
		m.put( "ifBranch", indent( visit( n.ifBranch() ) ) );
		if( !( n.elseBranch() instanceof NilStatement ) ) {
			m.put( "elseBranch", indent( visit( n.elseBranch() ) ) );
			template.append( " else { " + NEWLINE +
					"$elseBranch" + NEWLINE +
					"}" );
		}
		return Utils.createVelocityTemplate( template.toString() )
				.render( m ) + getContinuation( n, "" );
	}

	@Override
	public String visit( SwitchStatement n ) {
		HashMap< String, Object > m = new HashMap<>();
		String template = "switch( $guard ){" + NEWLINE +
				"$cases" + NEWLINE +
				"}";
		m.put( "guard", visit( n.guard() ) );
		String cases = n.cases().entrySet().stream().map(
				e -> {
					String caseArgument, body;
					caseArgument = visit( e.getKey() );
					body = indent( visit( e.getValue() ) );
					return caseArgument + "{" + NEWLINE + body + NEWLINE + "}";
				}
		).collect( Collectors.joining( NEWLINE ) );
		m.put( "cases", indent( cases ) );

		return Utils.createVelocityTemplate( template )
				.render( m ) + getContinuation( n, "" );
	}

	@Override
	public String visit( TryCatchStatement n ) {
		HashMap< String, Object > m = new HashMap<>();
		String template = "try {" + NEWLINE +
				"$body" + NEWLINE +
				"}" + NEWLINE + "$catches";
		m.put( "body", indent( visit( n.body() ) ) );
		String catches = n.catches().stream().map( e ->
				"catch ( " + visit( e.left(), " " ) + " ) { " + NEWLINE +
						indent( visit( e.right() ) ) + NEWLINE + "}"
		).collect( Collectors.joining( NEWLINE ) );
		m.put( "catches", catches );

		return Utils.createVelocityTemplate( template )
				.render( m ) + getContinuation( n, "" );
	}

	@Override
	public String visit( NilStatement n ) {
		return "";
	}

	@Override
	public String visit( ReturnStatement n ) {
		if( n.returnExpression() == null ) {
			return "return" + SEMICOLON + getContinuation( n, "" );
		} else {
			return "return " + visit( n.returnExpression() ) + SEMICOLON + getContinuation( n, "" );
		}
	}

	@Override
	public String visit( Expression n ) {
		return n.accept( this );
	}

	@Override
	public String visit( AssignExpression n ) {
		return visit( n.target() ) + " " + n.operator().symbol() + " " + visit( n.value() );
	}

	@Override
	public String visit( BinaryExpression n ) {
		return visit( n.left() ) + " " + n.operator().symbol() + " " + visit( n.right() );
	}

	@Override
	public String visit( ClassInstantiationExpression n ) {
		return
				"new " + ( n.typeArguments().isEmpty() ? "" : "< " + visitAndCollect(
						n.typeArguments(), SPACED_COMMA ) + " >" ) + visit( n.typeExpression() ) +
						( n.arguments().isEmpty() ? "()" : "( " + visitAndCollect( n.arguments(),
								SPACED_COMMA ) + " )" );
	}

	@Override
	public String visit( EnumCaseInstantiationExpression n ) {
		return n.name() + "@" + n.world().name().identifier() + "." + n._case();
	}

	@Override
	public String visit( EnclosedExpression n ) {
		return "( " + visit( n.nestedExpression() ) + " )";
	}

	@Override
	public String visit( FieldAccessExpression n ) {
		return visit( n.name() );
	}

	@Override
	public String visit( StaticAccessExpression n ) {
		return visit( n.typeExpression() );
	}

	@Override
	public String visit( MethodCallExpression n ) {
		return
				( n.typeArguments().isEmpty() ? "" : "< " + visitAndCollect( n.typeArguments(),
						SPACED_COMMA ) + " >" )
						+ visit( n.name() )
						+ ( n.arguments().isEmpty() ? "()" : "( " + visitAndCollect( n.arguments(),
						SPACED_COMMA ) + " )" );
	}

	@Override
	public String visit( Name n ) {
		return n.identifier();
	}

	@Override
	public String visit( NotExpression n ) {
		return "!" + visit( n.expression() );
	}

	@Override
	public String visit( ThisExpression n ) {
		return "this";
	}

	@Override
	public String visit( SuperExpression n ) {
		return "super";
	}

	@Override
	public String visit( NullExpression n ) {
		return "null";
	}

	@Override
	public String visit( BlankExpression n ) {
		return "";
	}

	@Override
	public String visit( VariableDeclarationStatement n ) {
		// Visit manually to preserve structure from original code
		String type = visit( n.variables().get( 0 ).type() );
		String variables = n.variables().stream().map(
				v -> visit( v.name() ) + v.initializer().map(
						e -> ASSIGN + visit( e.value() ) ).orElse( "" )
		).collect( Collectors.joining( SPACED_COMMA ) );
		return type + " " + variables + getContinuation( n, SEMICOLON );
	}

	@Override
	public String visit( LiteralExpression.BooleanLiteralExpression n ) {
		return n.content().toString() + ifPresent( n.world() ).applyOrElse(
				w -> "@" + w.name().identifier(), String::new );
	}

	@Override
	public String visit( LiteralExpression.DoubleLiteralExpression n ) {
		return n.content().toString() + ifPresent( n.world() ).applyOrElse(
				w -> "@" + w.name().identifier(), String::new );
	}

	@Override
	public String visit( LiteralExpression.IntegerLiteralExpression n ) {
		return n.content().toString() + ifPresent( n.world() ).applyOrElse(
				w -> "@" + w.name().identifier(), String::new );
	}

	@Override
	public String visit( LiteralExpression.StringLiteralExpression n ) {
		return n.content().toString() + ifPresent( n.world() ).applyOrElse(
				w -> "@" + w.name().identifier(), String::new );
	}

	@Override
	public String visit( SwitchArgument< ? > n ) {
		if( n instanceof SwitchArgument.SwitchArgumentDefault || n instanceof SwitchArgument.SwitchArgumentMergeDefault ) {
			return "default -> ";
		} else {
			return "case " + (
					n instanceof SwitchArgument.SwitchArgumentLabel ?
							( (SwitchArgument.SwitchArgumentLabel) n ).argument().identifier()
							: ( (SwitchArgument.SwitchArgumentLiteral) n ).argument().content().toString()
			) + " -> ";
		}
	}

	@Override
	public String visit( CaseSignature n ) {
		return "case " + n.name() + "( " + visitAndCollect( n.parameters(), SPACED_COMMA ) + " )";
	}

	@Override
	public String visit( Field n ) {
		HashMap< String, Object > m = new HashMap<>();
		m.put( "annotations", visitAndCollect( n.annotations(), NEWLINE, NEWLINE ) );
		m.put( "modifiers", visitModifiers( n.modifiers() ) );
		m.put( "type", visit( n.typeExpression() ) );
		m.put( "name", n.name() );

		String template = "${annotations}$modifiers$type $name";
		return Utils.createVelocityTemplate( template ).render( m );
	}

	@Override
	public String visit( FormalMethodParameter n ) {
		return visitAndCollect( n.annotations(), " ", " " ) + visit( n.type() ) + " " + n.name();
	}

	@Override
	public String visit( Statement n ) {
		return n.accept( this );
	}

	@Override
	public String visit( BlockStatement n ) {
		String s = "";
		if( !( n.enclosedStatement() instanceof NilStatement ) ) {
			s = "{" + NEWLINE + indent( visit( n.enclosedStatement() ) ) + NEWLINE + "}" + NEWLINE;
		}
		if( !( n.continuation() instanceof NilStatement ) ) {
			s += visit( n.continuation() );
		}
		return s;
	}

	@Override
	public String visit( ScopedExpression n ) {
		return visit( n.scope() ) + "." + visit( n.scopedExpression() );
	}

	@Override
	public String visit( ClassMethodDefinition n ) {
		HashMap< String, Object > m = new HashMap<>();
		m.put( "modifiers", visitModifiers( n.modifiers() ) );
		m.put( "signature", visit( n.signature() ) );
		m.put( "annotations", visitAndCollect( n.annotations(), NEWLINE, NEWLINE ) );

		String template;
		if( n.body().isPresent() ) {
			m.put( "body", indent( visit( n.body().get() ) ) );
			template = "${annotations}$modifiers$signature {" + NEWLINE +
					"$body" + NEWLINE +
					"}";
		} else {
			template = "${annotations}$modifiers$signature" + SEMICOLON;
		}
		return Utils.createVelocityTemplate( template ).render( m );
	}

	@Override
	public String visit( InterfaceMethodDefinition n ) {
		HashMap< String, Object > m = new HashMap<>();
		m.put( "modifiers", visitModifiers( n.modifiers() ) );
		m.put( "signature", visit( n.signature() ) );
//        m.put("body", indent(visit(n.body())));
		m.put( "annotations", visitAndCollect( n.annotations(), NEWLINE, NEWLINE ) );

		String template = "${annotations}$modifiers$signature";
		return Utils.createVelocityTemplate( template ).render( m );
	}


	@Override
	public String visit( MethodSignature n ) {
		return
				( n.typeParameters().isEmpty() ? "" : "< " + visitAndCollect( n.typeParameters(),
						SPACED_COMMA ) + " > " )
						+ visit( n.returnType() )
						+ " " + n.name()
						+ ( n.parameters().isEmpty() ? "()" : "( " + visitAndCollect(
						n.parameters(), SPACED_COMMA ) + " )" );
	}

	@Override
	public String visit( ConstructorDefinition n ) {
		HashMap< String, Object > m = new HashMap<>();
		m.put( "modifiers", visitModifiers( n.modifiers() ) );
		m.put( "annotations", visitAndCollect( n.annotations(), NEWLINE, NEWLINE ) );
		m.put( "signature", visit( n.signature() ) );
		m.put( "body", indent( visit( n.body() ) ) );

		String template = "${annotations}$modifiers$signature {" + NEWLINE + "$body" + NEWLINE + "}";
		return Utils.createVelocityTemplate( template ).render( m );
	}

	@Override
	public String visit( ConstructorSignature n ) {
		return ( n.typeParameters().isEmpty() ? "" : "< " + visitAndCollect( n.typeParameters(),
				SPACED_COMMA ) + " > " ) +
				n.name() +
				( n.parameters().isEmpty() ? "()" : "( " + visitAndCollect( n.parameters(),
						SPACED_COMMA ) + " )" );
	}

	@Override
	public String visit( VariableDeclaration n ) {
		return visit( n, NEWLINE );
	}

	public String visit( VariableDeclaration n, String separator ) {
		return visitAndCollect( n.annotations(), separator, separator ) + visit(
				n.type() ) + " " + visit( n.name() );
	}

	@Override
	public String visit( TypeExpression n ) {
		return n.name() +
				( n.worldArguments().isEmpty() ? "" : " @ ( " + visitAndCollect( n.worldArguments(),
						SPACED_COMMA ) + " )" ) +
				( n.typeArguments().isEmpty() ? "" : " < " + visitAndCollect( n.typeArguments(),
						SPACED_COMMA ) + " >" );
	}

	@Override
	public String visit( WorldArgument n ) {
		return n.name().toString();
	}

	@Override
	public String visit( FormalWorldParameter n ) {
		return n.name().toString();
	}

	@Override
	public String visit( Annotation n ) {
		return ANNOTATION
				+ n.getName().identifier()
				+ ( n.getValues().isEmpty() ? ""
				: "( " + n.getValues().entrySet().stream()
				.map( e -> e.getKey().identifier() + " " + AssignExpression.Operator.ASSIGN.symbol() + " " + visit(
						e.getValue() ) )
				.collect( Collectors.joining( SPACED_COMMA ) ) + " )"
		);
	}

	@Override
	public String visit( FormalTypeParameter n ) {
		StringBuilder s = new StringBuilder();
		s.append( visitAndCollect( n.annotations(), " ", " " ) );
		s.append( n.name() );
		if( !n.worldParameters().isEmpty() ) {
			s.append( " @ ( " );
			s.append( visitAndCollect( n.worldParameters(), SPACED_COMMA ) );
			s.append( " )" );
		}
		if( !n.upperBound().isEmpty() ) {
			s.append( ' ' ).append( EXTENDS ).append( ' ' ).append(
					n.upperBound().stream().map( this::visit ).collect(
							Collectors.joining( AMPERSAND ) )
			);
		}
		return s.toString();
	}

	// - - - - - - UTILITY - - - - - -

	public String visit( FormalTypeParameter n, String inheritance ) {
		StringBuilder s = new StringBuilder();
//		s.append( visit( n ) );
		if( !n.upperBound().isEmpty() ) {
			s.append( " " ).append( inheritance ).append( " " );
			s.append( visitAndCollect( n.upperBound(), AMPERSAND ) );
		}
		return s.toString();
	}

	protected final < T extends Node > String visitAndCollect( List< T > list, String delimiter ) {
		return list.stream().filter( Objects::nonNull ).map( e -> e.accept( this ) ).collect(
				Collectors.joining( delimiter ) );
	}

	protected final < T extends Node > String visitAndCollect(
			List< T > list, String delimiter, String closure
	) {
		return visitAndCollect( list, delimiter ) + ( list.isEmpty() ? "" : closure );
	}

	protected final String indent( String s ) {
		return Stream.of( s.split( NEWLINE ) ).map( l -> TAB + l ).collect(
				Collectors.joining( NEWLINE ) );
	}

	protected final String getContinuation( Statement s, String closure ) {
		return closure + ifPresent( s.continuation() ).applyOrElse(
				( e ) -> NEWLINE + visit( e ),
				String::new
		);
	}

	protected final < T > IfPresent< T > ifPresent( T o ) {
		return new IfPresent<>( o );
	}

	private static class IfPresent< T > {

		private final Optional< T > o;

		IfPresent( T o ) {
			if( o == null ) {
				this.o = Optional.empty();
			} else {
				this.o = Optional.of( o );
			}
		}

		< R > void apply( Function< T, R > f ) {
			if( f == null ) {
				throw new RuntimeException( "Application function must be not null" );
			}
			o.map( f );
		}


		< R > R applyOrElse( Function< T, R > f, Supplier< R > e ) {
			if( f == null || e == null ) {
				throw new RuntimeException(
						"Both application and alternative functions must be not null" );
			}
			return o.map( f ).orElseGet( e );
		}

	}

}
