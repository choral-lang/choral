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
import choral.ast.visitors.ChoralVisitorInterface;
import choral.types.HigherDataType;
import choral.utils.Pair;
import com.google.common.collect.Streams;

import java.util.*;
import java.util.stream.Collectors;

public class DependencyVisitor implements ChoralVisitorInterface< Void > {

	private final Map< String, AvailableTemplate > availableTemplates;
	private final WorldArgument w;
	// name, world index
	private final Set< Pair< String, Integer > > projectableTemplates;
	// name, world index
	private final Set< Pair< String, Integer > > visitedTemplates;

	public DependencyVisitor( List< CompilationUnit > CUs, WorldArgument w ) {
		this.w = w;
		availableTemplates = new HashMap<>();
		projectableTemplates = new HashSet<>();
		visitedTemplates = new HashSet<>();
		CUs.forEach( cu -> {
			cu.interfaces().forEach( i -> availableTemplates.put( i.name().identifier(),
					new AvailableTemplate( i, cu ) ) );
			cu.classes().forEach( c -> availableTemplates.put( c.name().identifier(),
					new AvailableTemplate( c, cu ) ) );
			cu.enums().forEach( e -> availableTemplates.put( e.name().identifier(),
					new AvailableTemplate( e, cu ) ) );
		} );
	}

	private DependencyVisitor(
			Map< String, AvailableTemplate > availableTemplates,
			Set< Pair< String, Integer > > projectableTemplates,
			Set< Pair< String, Integer > > visitedTemplates,
			WorldArgument w
	) {
		this.w = w;
		this.availableTemplates = availableTemplates;
		this.projectableTemplates = projectableTemplates;
		this.visitedTemplates = visitedTemplates;
	}

	public Set< ProjectableTemplate > collectTemplates( String template ) {
		return collectProjectableTemplates( template ).stream()
				.map( p -> {
					AvailableTemplate a = availableTemplates.get( p.left() );
					WorldArgument w = getWorldFromIndex( a.node(), p.right() );
					return new ProjectableTemplate(
							a.compilationUnit().packageDeclaration(),
							a.compilationUnit().imports(),
							a.node(),
							w
					);
				} ).collect( Collectors.toSet() );
	}

	// name, world index
	private Set< Pair< String, Integer > > collectProjectableTemplates( String template ) {
		if( availableTemplates.containsKey( template ) ) {
			// first we visit the main template
			Node n = availableTemplates.get( template ).node();
			projectableTemplates.add( new Pair<>( template, getIndexFromWorld( n, this.w ) ) );
			if( n instanceof Interface ) {
				visit( (Interface) n );
			} else if( n instanceof Class ) {
				visit( (Class) n );
			} else {
				visit( (Enum) n );
			}
			// then we visit all the projectable templates collected from the main template
			visitProjectableTemplates();
			// before returning we keep only those projectable templates that are available
			return projectableTemplates.stream().filter(
					p -> availableTemplates.containsKey( p.left() ) ).collect( Collectors.toSet() );
		} else {
			throw new SoloistProjectorException( "Template '" + template + "' not found" );
		}
	}

	private void visitProjectableTemplates() {
		// we mark as visited those projectable templates that are not available
		projectableTemplates.stream()
				.filter( p -> !visitedTemplates.contains(
						p ) ) // we optimise the check to execute only on those not visited yet
				.forEach( p -> {
					if( !availableTemplates.containsKey( p.left() ) ) visitedTemplates.add( p );
				} );
		// we collect the remaining projectable templates
		new HashSet<>(
				projectableTemplates ) // we take a snapshot of the current projectable templates
				// to interate over them and update the same data structure in the loop
				.stream()
				.filter( p -> !visitedTemplates.contains( p ) )
				.forEach( p -> new DependencyVisitor(
								availableTemplates,
								projectableTemplates,
								visitedTemplates,
								getWorldFromIndex( availableTemplates.get( p.left() ).node(), p.right() )
						).collectProjectableTemplates( p.left() )
				);
		// if we have fewer visited templates than projectable, we re-iterate
		if( visitedTemplates.size() < projectableTemplates.size() ) {
			visitProjectableTemplates();
		}
	}

	@Override
	public Void visit( CompilationUnit n ) {
		throw new SoloistProjectorException(
				"DependencyVisitor should not visit a compilation unit" );
	}

	@Override
	public Void visit( ImportDeclaration n ) {
		throw new SoloistProjectorException(
				"DependencyVisitor should not visit an import declaration" );
	}

	@Override
	public Void visit( Class n ) {
		// we add this class to be projected under world w
		if( toWorldArguments( n.worldParameters() ).contains( w ) ) {
			// we add dependencies from type parameters
			// C@( A, B )< D@( W1, W2 ) extends E@( W2, W1 ) >, here we take all projections ( W2, W1 of E )
			n.typeParameters().forEach( this::visit );
			// we add dependencies from extended classes
			// C@( A, B ) extends D@( B, A )
			n.superClass().ifPresent( this::visit );
			// we add dependencies from implemented interfaces
			n.implementsInterfaces().forEach( this::visit ); // C@( A, B ) implements D@( B, A )
			// finally we visit the fields, constructors, and methods
			n.fields().forEach( this::visit );
			n.constructors().forEach( this::visit );
			n.methods().forEach( this::visit );
			visitedTemplates.add( new Pair<>(
					n.name().identifier(),
					toWorldArguments( n.worldParameters() ).indexOf( w )
			) );
		}
		return null;
	}

	@Override
	public Void visit( Enum n ) {
		if( toWorldArguments( n.worldParameters() ).contains( w ) ) {
			visitedTemplates.add( new Pair<>(
					n.name().identifier(),
					toWorldArguments( n.worldParameters() ).indexOf( w )
			) );
		}
		return null;
	}

	@Override
	public Void visit( EnumConstant n ) {
		return null;
	}

	@Override
	public Void visit( Interface n ) {
		if( toWorldArguments( n.worldParameters() ).contains( w ) ) {
			// we add dependencies from type parameters
			// C@( A, B )< D@( W1, W2 ) extends E@( W2, W1 ) >, here we take all projections ( W2, W1 of E )
			n.typeParameters().forEach( this::visit );
			// we add dependencies from extended interfaces
			// C@( A, B ) extends D@( B, A )
			n.extendsInterfaces().forEach( this::visit );
			// finally we visit the constructors and the methods
			n.methods().forEach( this::visit );
			visitedTemplates.add( new Pair<>(
					n.name().identifier(),
					toWorldArguments( n.worldParameters() ).indexOf( w )
			) );
		}
		return null;
	}

	@Override
	public Void visit( Statement n ) {
		n.accept( this );
		if( n.continuation() != null ) {
			visit( n.continuation() );
		}
		return null;
	}

	@Override
	public Void visit( BlockStatement n ) {
		visit( n.enclosedStatement() );
		return null;
	}

	@Override
	public Void visit( ScopedExpression n ) {
		visit( n.scope() );
		visit( n.scopedExpression() );
		return null;
	}

	@Override
	public Void visit( ExpressionStatement n ) {
		visit( n.expression() );
		return null;
	}

	@Override
	public Void visit( IfStatement n ) {
		visit( n.condition() );
		visit( n.ifBranch() );
		visit( n.elseBranch() );
		return null;
	}

	@Override
	public Void visit( SwitchStatement n ) {
		visit( n.guard() );
		n.cases().forEach( ( c, s ) -> visit( s ) );
		return null;
	}

	@Override
	public Void visit( TryCatchStatement n ) {
		visit( n.body() );
		n.catches().forEach( p -> {
			if( p.left().type().worldArguments().contains( this.w ) ) {
				visit( p.left().type() );
				visit( p.right() );
			}
		} );
		return null;
	}

	@Override
	public Void visit( NilStatement n ) {
		return null;
	}

	@Override
	public Void visit( ReturnStatement n ) {
		visit( n.returnExpression() );
		return null;
	}

	@Override
	public Void visit( Expression n ) {
		n.accept( this );
		return null;
	}

	@Override
	public Void visit( AssignExpression n ) {
		visit( n.target() );
		visit( n.value() );
		return null;
	}

	@Override
	public Void visit( BinaryExpression n ) {
		visit( n.left() );
		visit( n.right() );
		return null;
	}

	@Override
	public Void visit( ClassInstantiationExpression n ) {
		if( n.typeExpression().worldArguments().contains( this.w ) ) {
			projectableTemplates.add( new Pair<>( n.typeExpression().name().identifier(),
					n.typeExpression().worldArguments().indexOf( this.w ) ) );
			n.typeArguments().forEach( this::visit );
		}
		// we project the arguments anyway because this.w might participate in those
		n.arguments().forEach( this::visit );
		return null;
	}

	@Override
	public Void visit( EnumCaseInstantiationExpression n ) {
		if( n.world().equals( w ) ) {
			projectableTemplates.add( new Pair<>(
					n.name().identifier(),
					0 // we just have one world
			) );
		}
		return null;
	}

	@Override
	public Void visit( EnclosedExpression n ) {
		n.nestedExpression().accept( this );
		return null;
	}

	@Override
	public Void visit( FieldAccessExpression n ) {
		return null;
	}

	@Override
	public Void visit( StaticAccessExpression n ) {
		visit( n.typeExpression() );
		return null;
	}

	@Override
	public Void visit( MethodCallExpression n ) {
		n.typeArguments().forEach( this::visit );
		n.arguments().forEach( this::visit );
		return null;
	}

	@Override
	public Void visit( Name n ) {
		throw new SoloistProjectorException( "DependencyVisitor should not visit a name" );
	}

	@Override
	public Void visit( NotExpression n ) {
		n.expression().accept( this );
		return null;
	}

	@Override
	public Void visit( ThisExpression n ) {
		return null;
	}

	@Override
	public Void visit( SuperExpression n ) {
		return null;
	}

	@Override
	public Void visit( NullExpression n ) {
		return null;
	}

	@Override
	public Void visit( VariableDeclarationStatement n ) {
		n.variables().forEach( this::visit );
		return null;
	}

	@Override
	public Void visit( BlankExpression n ) {
		return null;
	}

	@Override
	public Void visit( LiteralExpression.BooleanLiteralExpression n ) {
		return null;
	}

	@Override
	public Void visit( LiteralExpression.DoubleLiteralExpression n ) {
		return null;
	}

	@Override
	public Void visit( LiteralExpression.IntegerLiteralExpression n ) {
		return null;
	}

	@Override
	public Void visit( LiteralExpression.StringLiteralExpression n ) {
		return null;
	}

	@Override
	public Void visit( SwitchArgument< ? > n ) {
		return null;
	}

	@Override
	public Void visit( CaseSignature n ) {
		throw new SoloistProjectorException(
				"DependencyVisitor should not visit a case signature" );
	}

	@Override
	public Void visit( Field n ) {
		visit( n.typeExpression() );
		return null;
	}

	@Override
	public Void visit( FormalMethodParameter n ) {
		visit( n.type() );
		return null;
	}

	@Override
	public Void visit( ClassMethodDefinition n ) {
		visit( n.signature() );
		n.body().ifPresent( this::visit );
		return null;
	}

	@Override
	public Void visit( InterfaceMethodDefinition n ) {
		visit( n.signature() );
//		visit( n.body() );
		return null;
	}

	@Override
	public Void visit( MethodSignature n ) {
		visit( n.returnType() );
		n.parameters().forEach( this::visit );
		n.typeParameters().forEach( this::visit );
		return null;
	}

	@Override
	public Void visit( ConstructorDefinition n ) {
		visit( n.signature() );
		visit( n.body() );
		return null;
	}

	@Override
	public Void visit( ConstructorSignature n ) {
		n.parameters().forEach( this::visit );
		return null;
	}

	@Override
	public Void visit( VariableDeclaration n ) {
		visit( n.type() );
		n.initializer().ifPresent( this::visit );
		return null;
	}

	@Override
	public Void visit( TypeExpression n ) {
		if( n.worldArguments().contains( this.w ) ) { // this is of kind *
			this.projectableTemplates.add(
					new Pair<>( n.name().identifier(), n.worldArguments().indexOf( this.w ) ) );
		}
		if( n.worldArguments().size() == 0 && !n.name().identifier().equals(
				"void" ) ) { // this is of kind @ => ...
			HigherDataType dataType = (HigherDataType) n.typeAnnotation().get();
			dataType.worldParameters().forEach( w -> {
				this.projectableTemplates.add(
						new Pair<>( n.name().identifier(), dataType.worldParameters().indexOf( w ) )
				);
			} );
		}
		n.typeArguments().forEach( this::visit );
		return null;
	}

	@Override
	public Void visit( WorldArgument n ) {
		throw new SoloistProjectorException(
				"DependencyVisitor should not visit a formal world argument" );
	}

	@Override
	public Void visit( FormalTypeParameter n ) {
		n.worldParameters().forEach( w ->
				n.upperBound().forEach( t ->
						new DependencyVisitor(
								this.availableTemplates,
								this.projectableTemplates,
								this.visitedTemplates,
								w.toWorldArgument()
						).visit( t )
				)
		);
		return null;
	}

	@Override
	public Void visit( FormalWorldParameter n ) {
		throw new SoloistProjectorException(
				"DependencyVisitor should not visit a formal world parameter" );
	}

	@Override
	public Void visit( Annotation n ) {
		return null;
	}

	private List< WorldArgument > toWorldArguments( List< FormalWorldParameter > w ) {
		return w.stream().map( FormalWorldParameter::toWorldArgument ).collect(
				Collectors.toList() );
	}

	private WorldArgument getWorldFromIndex( Node n, Integer i ) {
		String name = "";
		try {
			if( n instanceof Class ) {
				name = ( (Class) n ).name().identifier();
				return ( (Class) n ).worldParameters().get( i ).toWorldArgument();
			} else if( n instanceof Interface ) {
				name = ( (Interface) n ).name().identifier();
				return ( (Interface) n ).worldParameters().get( i ).toWorldArgument();
			} else {
				name = ( (Enum) n ).name().identifier();
				return ( (Enum) n ).worldParameters().get( i ).toWorldArgument();
			}
		} catch( IndexOutOfBoundsException e ) {
			// the world does not belong to the node
		}
		throw new SoloistProjectorException(
				"World '" + w.name().identifier() + "' not present in " + name );
	}

	private Integer getIndexFromWorld( Node n, WorldArgument w ) {
		String name = "";
		try {
			if( n instanceof Class ) {
				name = ( (Class) n ).name().identifier();
				return toWorldArguments( ( (Class) n ).worldParameters() ).indexOf( w );
			} else if( n instanceof Interface ) {
				name = ( (Interface) n ).name().identifier();
				return toWorldArguments( ( (Interface) n ).worldParameters() ).indexOf( w );
			} else {
				name = ( (Enum) n ).name().identifier();
				return toWorldArguments( ( (Enum) n ).worldParameters() ).indexOf( w );
			}
		} catch( IndexOutOfBoundsException e ) {
			// the world does not belong to the node
			throw new SoloistProjectorException(
					"World '" + w.name().identifier() + "' not present in " + name );
		}
	}

	public static List< WorldArgument > getWorldArguments(
			List< CompilationUnit > compilationUnits, String template
	) {
		List< WorldArgument > worlds =
				Streams.concat(
								compilationUnits.stream().map( CompilationUnit::interfaces ).flatMap(
										List::stream ),
								compilationUnits.stream().map( CompilationUnit::classes ).flatMap(
										List::stream ),
								compilationUnits.stream().map( CompilationUnit::enums ).flatMap(
										List::stream )
						).filter( t -> t.name().identifier().equals( template ) )
						.flatMap( t -> t.worldParameters().stream().map(
								FormalWorldParameter::toWorldArgument ) )
						.collect( Collectors.toList() );
		if( worlds.size() > 0 ) {
			return worlds;
		} else {
			throw new SoloistProjectorException( "Template '" + template + "' not found" );
		}
	}

	private static class AvailableTemplate {
		private final Node node;
		private final CompilationUnit compilationUnit;

		public AvailableTemplate( Node node, CompilationUnit compilationUnit ) {
			this.node = node;
			this.compilationUnit = compilationUnit;
		}

		public Node node() {
			return node;
		}

		public CompilationUnit compilationUnit() {
			return compilationUnit;
		}
	}

}
