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

package choral.compiler.amend;

import choral.ast.*;
import choral.ast.body.*;
import choral.ast.expression.*;
import choral.ast.statement.*;
import choral.ast.type.FormalTypeParameter;
import choral.ast.type.FormalWorldParameter;
import choral.ast.type.TypeExpression;
import choral.ast.type.WorldArgument;
import choral.ast.visitors.AbstractChoralVisitor;
import choral.exceptions.AstPositionedException;
import choral.exceptions.StaticVerificationException;
import choral.types.*;
import choral.types.Member.HigherMethod;
import choral.types.Package; // to avoid ambigous reference to "Package"
import choral.types.Universe.PrimitiveTypeTag;
import choral.types.Universe.SpecialTypeTag;
import choral.utils.Formatting;
import choral.utils.Pair;
import com.google.common.base.Strings;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Similar to Typer, except RelaxedTyper allows location mismatch between variables.
 * RelaxedTyper allows expressions such as "int@A a = b;" where b is an int@B.
 */
public class RelaxedTyper {

	private enum Phase {
		TYPE_SYMBOL_DECLARATIONS,
		HIERARCHY,
		BOUND_CHECKS,
		MEMBER_DECLARATIONS,
		MEMBER_DEFINITIONS,
		MEMBER_GLOBAL_CHECKS,
	}

	public static Collection< CompilationUnit > annotate(
			Collection< CompilationUnit > sourceUnits, 
			Collection< CompilationUnit > headerUnits
	) {
		TaskQueue taskQueue = new TaskQueue();
		Universe universe = new Universe();
		Visitor headerVisitor = new HeaderVisitor( taskQueue, universe );
		headerUnits.forEach( cu -> taskQueue.enqueue( Phase.TYPE_SYMBOL_DECLARATIONS,
				() -> headerVisitor.visit( cu ) ) );
		Visitor sourceVisitor = new SourceVisitor( taskQueue, universe );
		sourceUnits.forEach( cu -> taskQueue.enqueue( Phase.TYPE_SYMBOL_DECLARATIONS,
				() -> sourceVisitor.visit( cu ) ) );
		taskQueue.process();
		return sourceUnits;
	}

	private abstract static class Visitor {

		private static final String SELECTION_METHOD_ANNOTATION = "SelectionMethod";
		private static final String SUPER_SELECTION_METHOD_ANNOTATION = "TypeSelectionMethod";

		private final TaskQueue taskQueue;
		private final Universe universe;

		public Visitor( TaskQueue taskQueue, Universe universe ) {
			this.taskQueue = taskQueue;
			this.universe = universe;
		}

		protected TaskQueue taskQueue() {
			return taskQueue;
		}

		protected Universe universe() {
			return universe;
		}

		protected void visit( choral.ast.CompilationUnit n ) {
			Package pkg = universe.rootPackage();
			if( n.packageDeclaration().isPresent() ) {
				String[] path = n.packageDeclaration().get().split( "\\." );
				for( String s : path ) {
					pkg = pkg.declarePackage( s );
				}
			}
			CompilationUnitScope scope = new CompilationUnitScope( pkg, n.imports() );
			for( choral.ast.body.Class x : n.classes() ) {
				checkPrimaryTemplate( x, n.primaryType(), "class" );
				visitClass( scope, pkg, x );
			}
			for( choral.ast.body.Enum x : n.enums() ) {
				checkPrimaryTemplate( x, n.primaryType(), "enum" );
				visitEnum( scope, pkg, x );
			}
			for( choral.ast.body.Interface x : n.interfaces() ) {
				checkPrimaryTemplate( x, n.primaryType(), "interface" );
				visitInterface( scope, pkg, x );
			}
			visitImportDeclarations( scope, n.imports() );
		}

		protected void visitImportDeclarations( Scope scope, List< ImportDeclaration > ns ) {
		}

		protected abstract void checkPrimaryTemplate(
				TemplateDeclaration n, String primaryType, String family
		);

		private void checkExtendsForCycles(
				GroundClassOrInterface type, GroundClassOrInterface supertype
		) {
			if( !supertype.isInheritanceFinalised() ) {
				TaskQueue.Task hs = taskQueue.hierarchyConstructionTasks.get(
						supertype.typeConstructor() );
				hs.prepare(); // no-op unless waiting
				hs.run();     // no-op unless ready
				if( hs.status() != TaskQueue.Task.Status.FINISHED ) {
					throw new StaticVerificationException(
							"Cyclic inheritance: '" + type + "' cannot extend '" + supertype + "'" );
				}
			}
		}

		private void visitClass(
				CompilationUnitScope declarationScope, Package pkg, choral.ast.body.Class n
		) {
			EnumSet< Modifier > modifiers = EnumSet.noneOf( Modifier.class );
			for( ClassModifier m : n.modifiers() ) {
				modifiers.add( Modifier.valueOf( m.name() ) );
			}
			HigherClass t = new HigherClass(
					pkg,
					modifiers,
					n.name().identifier(),
					visitWorldParameters( n.worldParameters() ),
					visitTypeParameters( n.typeParameters() ),
					n
			);
			n.setTypeAnnotation( t ); // annotate AST
			ClassOrInterfaceStaticScope classOrInterfaceStaticScope = declarationScope.getScope(
					t );
			TaskQueue.Task ht = new TaskQueue.Task( Phase.HIERARCHY, () -> {
				if( n.superClass().isEmpty() ) {
					t.innerType().setExtendedClass(); // default
					if( t.innerType().extendedClass().isPresent() ) {
						checkExtendsForCycles( t.innerType(), t.innerType().extendedClass().get() );
					}
				} else {
					try {
						GroundClass s = visitGroundClassExpression(
								classOrInterfaceStaticScope.getScope(),
								n.superClass().get(), true );
						t.innerType().setExtendedClass( s );
						checkExtendsForCycles( t.innerType(), s );
					} catch( StaticVerificationException e ) {
						throw new AstPositionedException( n.superClass().get().position(), e );
					}
				}
				for( choral.ast.type.TypeExpression x : n.implementsInterfaces() ) {
					try {
						GroundInterface s = visitGroundInterfaceExpression(
								classOrInterfaceStaticScope.getScope(),
								x, true );
						t.innerType().addExtendedInterface( s );
						checkExtendsForCycles( t.innerType(), s );
					} catch( StaticVerificationException e ) {
						throw new AstPositionedException( x.position(), e );
					}
				}
				t.innerType().finaliseInheritance();
				visitTypeParametersBound( classOrInterfaceStaticScope.getScope(),
						n.typeParameters(),
						true );
				taskQueue.hierarchyConstructionTasks.remove( t );
			} );
			taskQueue.hierarchyConstructionTasks.put( t, ht );
			taskQueue.enqueue( ht );
			taskQueue.enqueue( Phase.MEMBER_DECLARATIONS, () -> {
				for( Field nm : n.fields() ) {
					EnumSet< Modifier > ms = EnumSet.noneOf( Modifier.class );
					for( FieldModifier x : nm.modifiers() ) {
						ms.add( Modifier.valueOf( x.name() ) );
					}
					Member.Field tm = new Member.Field(
							t.innerType(),
							nm.name().identifier(),
							ms,
							visitGroundDataTypeExpression(
									( ms.contains( Modifier.STATIC ) )
											? classOrInterfaceStaticScope
											: classOrInterfaceStaticScope.getScope(),
									nm.typeExpression(), false ) );
					nm.setTypeAnnotation( tm );
					tm.setSourceCode( nm );
					t.innerType().addField( tm );
				}
				for( ClassMethodDefinition nm : n.methods() ) {
					EnumSet< Modifier > ms = EnumSet.noneOf( Modifier.class );
					for( ClassMethodModifier x : nm.modifiers() ) {
						ms.add( Modifier.valueOf( x.name() ) );
					}
					if( ms.contains( Modifier.ABSTRACT ) && !t.isAbstract() ) {
						throw StaticVerificationException.of(
								"abstract method in non-abstract class", nm.position() );
					}
					List< HigherTypeParameter > typeParams = visitTypeParameters(
							nm.signature().typeParameters() );
					Member.HigherMethod tm = new Member.HigherMethod(
							t.innerType(),
							nm.signature().name().identifier(),
							ms,
							typeParams );
					nm.setTypeAnnotation( tm );
					tm.setSourceCode( nm );
					CallableScope callableScope = ( ms.contains( Modifier.STATIC ) )
							? classOrInterfaceStaticScope.getScope( tm )
							: classOrInterfaceStaticScope.getScope().getScope( tm );
					visitTypeParametersBound( callableScope, nm.signature().typeParameters(),
							true );
					for( FormalMethodParameter x : nm.signature().parameters() ) {
						tm.innerCallable().signature().addParameter( x.name().identifier(),
								visitGroundDataTypeExpression( callableScope, x.type(),
										true ) );
					}
					TypeExpression r = nm.signature().returnType();
					if( r.name().identifier().equals( "void" ) ) {
						if( !( r.worldArguments().isEmpty() && r.typeArguments().isEmpty() ) ) {
							// void should not be in a type expression
							throw new AstPositionedException( r.position(),
									new StaticVerificationException(
											"illegal type instantiation, expected 0 role and 0 type arguments" ) );
						}
						tm.innerCallable().setReturnType( universe.voidType() );
					} else {
						tm.innerCallable().setReturnType(
								visitGroundDataTypeExpression( callableScope,
										nm.signature().returnType(), false ) );
					}
					checkIfSelectionMethod( tm, nm.annotations() );
					checkIfTypeSelectionMethod( tm, nm.annotations() );
					try {
						tm.innerCallable().finalise();
						t.innerType().addMethod( tm );
					} catch( StaticVerificationException e ) {
						throw new AstPositionedException( nm.position(), e );
					}
					taskQueue.enqueue( Phase.MEMBER_DEFINITIONS, () -> {
						try {
							visitMethodBody( callableScope.getScope(), tm,
									nm.body().orElse( null ) );
						} catch( StaticVerificationException e ) {
							throw new AstPositionedException( nm.position(), e );
						}
					} );
				}
				Map< Member.HigherConstructor, Member.HigherConstructor > constructorDependencies =
						new HashMap<>( n.constructors().size() );
				Map< Member.HigherConstructor, Position > explicitConstructorInvocations =
						new HashMap<>( n.constructors().size() );
				for( ConstructorDefinition nm : n.constructors() ) {
					EnumSet< Modifier > ms = EnumSet.noneOf( Modifier.class );
					for( ConstructorModifier m : nm.modifiers() ) {
						ms.add( Modifier.valueOf( m.name() ) );
					}
					List< HigherTypeParameter > typeParams = visitTypeParameters(
							nm.signature().typeParameters() );
					Member.HigherConstructor tm = new Member.HigherConstructor(
							t.innerType(),
							ms,
							typeParams );
					nm.setTypeAnnotation( tm );
					tm.setSourceCode( nm );
					CallableScope callableScope = classOrInterfaceStaticScope.getScope().getScope(
							tm );
					visitTypeParametersBound( callableScope, nm.signature().typeParameters(),
							false );
					for( FormalMethodParameter x : nm.signature().parameters() ) {
						tm.innerCallable().signature().addParameter( x.name().identifier(),
								visitGroundDataTypeExpression( callableScope, x.type(),
										false ) );
					}
					try {
						tm.innerCallable().finalise();
						t.innerType().addConstructor( tm );
					} catch( StaticVerificationException e ) {
						throw new AstPositionedException( nm.position(), e );
					}
					taskQueue.enqueue( Phase.MEMBER_DEFINITIONS,
							() -> visitConstructorBody( callableScope.getScope(), tm, nm,
									constructorDependencies, explicitConstructorInvocations ) );
				}
				taskQueue.enqueue( Phase.MEMBER_GLOBAL_CHECKS,
						() -> checkConstructorsDependencies( constructorDependencies,
								explicitConstructorInvocations ) );
				taskQueue.enqueue( new MemberTask( Phase.MEMBER_DECLARATIONS, t, () -> {
					try {
						t.innerType().finaliseInterface();
					} catch( StaticVerificationException e ) {
						throw new AstPositionedException( n.position(), e );
					}
				} ) );
			} );
		}

		private void visitEnum(
				CompilationUnitScope declarationScope, Package pkg, choral.ast.body.Enum n
		) {
			EnumSet< Modifier > modifiers = EnumSet.noneOf( Modifier.class );
			for( ClassModifier m : n.modifiers() ) {
				modifiers.add( Modifier.valueOf( m.name() ) );
			}
			String name = n.name().identifier();
			if( n.worldParameters().size() != 1 ) {
				throw new AstPositionedException(
						( n.worldParameters().isEmpty() )
								? n.position()
								: n.worldParameters().get( 1 ).position(),
						new StaticVerificationException( "enums must have exactly one role" ) );
			}
			HigherEnum t = new HigherEnum(
					pkg,
					modifiers,
					name,
					visitWorldParameter( n.worldParameters().get( 0 ) ),
					n
			);
			n.setTypeAnnotation( t ); // annotate AST
			TaskQueue.Task ht = new TaskQueue.Task( Phase.HIERARCHY, () -> {
				t.innerType().setExtendedClass(); // default
				checkExtendsForCycles( t.innerType(), t.innerType().extendedClass().get() );
				t.innerType().finaliseInheritance();
				taskQueue.hierarchyConstructionTasks.remove( t );
				if( !t.innerType().isInstantiationChecked() ) {
					t.innerType().checkInstantiation();
				}
			} );
			taskQueue.hierarchyConstructionTasks.put( t, ht );
			taskQueue.enqueue( ht );
			taskQueue.enqueue( Phase.MEMBER_DECLARATIONS, () -> {
				for( EnumConstant c : n.cases() ) {
					try {
						t.innerType().addCase( c.name().identifier() );
					} catch( StaticVerificationException e ) {
						throw new AstPositionedException( c.position(), e );
					}
				}
				taskQueue.enqueue( new MemberTask( Phase.MEMBER_DECLARATIONS, t, () -> {
					try {
						t.innerType().finaliseInterface();
					} catch( StaticVerificationException e ) {
						throw new AstPositionedException( n.position(), e );
					}
				} ) );
			} );
		}

		private void visitInterface(
				CompilationUnitScope declarationScope, Package
				pkg, choral.ast.body.Interface n
		) {
			EnumSet< Modifier > modifiers = EnumSet.noneOf( Modifier.class );
			for( InterfaceModifier m : n.modifiers() ) {
				modifiers.add( Modifier.valueOf( m.name() ) );
			}
			String name = n.name().identifier();
			HigherInterface t = new HigherInterface(
					pkg,
					modifiers,
					name,
					visitWorldParameters( n.worldParameters() ),
					visitTypeParameters( n.typeParameters() ),
					n
			);
			ClassOrInterfaceStaticScope classOrInterfaceStaticScope = declarationScope.getScope(
					t );
			n.setTypeAnnotation( t ); // annotate AST
			TaskQueue.Task h = new TaskQueue.Task( Phase.HIERARCHY, () -> {
				for( choral.ast.type.TypeExpression x : n.extendsInterfaces() ) {
					try {
						GroundInterface s = visitGroundInterfaceExpression(
								classOrInterfaceStaticScope.getScope(),
								x, true );
						t.innerType().addExtendedInterface( s );
						checkExtendsForCycles( t.innerType(), s );
					} catch( StaticVerificationException e ) {
						throw new AstPositionedException( x.position(), e );
					}
				}
				t.innerType().finaliseInheritance();
				taskQueue.hierarchyConstructionTasks.remove( t );
				visitTypeParametersBound( classOrInterfaceStaticScope.getScope(),
						n.typeParameters(), true );
			} );
			taskQueue.hierarchyConstructionTasks.put( t, h );
			taskQueue.enqueue( h );
			taskQueue.enqueue( Phase.MEMBER_DECLARATIONS, () -> {
				for( InterfaceMethodDefinition nm : n.methods() ) {
					EnumSet< Modifier > ms = EnumSet.noneOf( Modifier.class );
					for( InterfaceMethodModifier x : nm.modifiers() ) {
						ms.add( Modifier.valueOf( x.name() ) );
					}
					t.addImplicitMethodModifiers( ms );
					List< HigherTypeParameter > typeParams = visitTypeParameters(
							nm.signature().typeParameters() );
					Member.HigherMethod tm = new Member.HigherMethod(
							t.innerType(),
							nm.signature().name().identifier(),
							ms,
							typeParams );
					nm.setTypeAnnotation( tm );
					tm.setSourceCode( nm );
					CallableScope methodScope = ( ms.contains( Modifier.STATIC ) )
							? classOrInterfaceStaticScope.getScope( tm )
							: classOrInterfaceStaticScope.getScope().getScope( tm );
					visitTypeParametersBound( methodScope, nm.signature().typeParameters(),
							true );
					for( FormalMethodParameter x : nm.signature().parameters() ) {
						tm.innerCallable().signature().addParameter( x.name().identifier(),
								visitGroundDataTypeExpression( methodScope, x.type(), false ) );
					}
					TypeExpression r = nm.signature().returnType();
					if( r.name().identifier().equals( "void" ) ) {
						if( !( r.worldArguments().isEmpty() && r.typeArguments().isEmpty() ) ) {
							// void should not be in a type expression
							throw new AstPositionedException( r.position(),
									new StaticVerificationException(
											"illegal type instantiation, expected 0 role and 0 type arguments" ) );
						}
						tm.innerCallable().setReturnType( universe.voidType() );
					} else {
						tm.innerCallable().setReturnType(
								visitGroundDataTypeExpression( methodScope,
										nm.signature().returnType(), false ) );
					}
					checkIfSelectionMethod( tm, nm.annotations() );
					checkIfTypeSelectionMethod( tm, nm.annotations() );
					tm.innerCallable().finalise();
					t.innerType().addMethod( tm );
				}
				taskQueue.enqueue( new MemberTask( Phase.MEMBER_DECLARATIONS, t, () -> {
					try {
						t.innerType().finaliseInterface();
					} catch( StaticVerificationException e ) {
						throw new AstPositionedException( n.position(), e );
					}
				} ) );
			} );
		}

		protected abstract void checkConstructorsDependencies(
				Map< Member.HigherConstructor, Member.HigherConstructor > dependencies,
				Map< Member.HigherConstructor, Position > positions
		);

		private void checkIfSelectionMethod(
				Member.HigherMethod tm, List< Annotation > annotations
		) {
			for( Annotation x : annotations ) {
				if( x.getName().identifier().equals( SELECTION_METHOD_ANNOTATION ) ) {
					if( tm.typeParameters().size() != 1 ) {
						throw new AstPositionedException( x.position(),
								new StaticVerificationException(
										"illegal selection method, expected 1 type parameter, found "
												+ tm.typeParameters().size() ) );
					}
					HigherTypeParameter tp = tm.typeParameters().get( 0 );
					if( tp.innerType().upperClass().specialTypeTag() != SpecialTypeTag.ENUM
							|| tp.innerType().upperInterfaces().findAny().isPresent() ) {
						throw new AstPositionedException( x.position(),
								new StaticVerificationException(
										"illegal selection method, the type parameter must be bounded exactly by '"
												+ SpecialTypeTag.ENUM + "'" ) );
					}
					if( tm.arity() != 1 ) {
						throw new AstPositionedException( x.position(),
								new StaticVerificationException(
										"illegal selection method, expected 1 method parameter, found "
												+ tm.innerCallable().signature().arity() ) );
					}
					GroundDataType mp = tm.innerCallable().signature().parameters()
							.get( 0 ).type();
					if( mp.worldArguments().size() != 1 || !mp.isEquivalentTo(
							tp.applyTo( mp.worldArguments() ) ) ) {
						List< World > ws = World.freshWorlds( universe(), 1, "X" );
						throw new AstPositionedException( x.position(),
								new StaticVerificationException(
										"illegal selection method, expected a method parameter of type '"
												+ tp.applyTo( ws )
												+ "' for some "
												+ Formatting.joiningQuotedAndOxfordComma( ws )
												+ ", found one of type '" + mp
												+ "'" ) );
					}
					if( tm.innerCallable().returnType().isVoid() ) {
						List< World > ws = World.freshWorlds( universe(), 1, "X" );
						throw new AstPositionedException( x.position(),
								new StaticVerificationException(
										"illegal selection method, expected return type '"
												+ tp.applyTo( ws )
												+ "' for some "
												+ Formatting.joiningQuotedAndOxfordComma( ws )
												+ ", found 'void'" ) );
					}
					GroundDataType tr = (GroundDataType) tm.innerCallable().returnType();
					if( tr.worldArguments().size() != 1 || !tr.isEquivalentTo(
							tp.applyTo( tr.worldArguments() ) ) ) {
						List< World > ws = World.freshWorlds( universe(), 1, "X" );
						throw new AstPositionedException( x.position(),
								new StaticVerificationException(
										"illegal selection method, expected a method parameter of type '"
												+ tp.applyTo( ws )
												+ "' for some "
												+ Formatting.joiningQuotedAndOxfordComma( ws )
												+ ", found '" + mp + "'" ) );
					}
					if( mp.worldArguments().equals( tr.worldArguments() ) ) {
						throw new AstPositionedException( x.position(),
								new StaticVerificationException(
										"illegal selection method, roles of the method parameter and return type must be distinct" ) );
					}

					tm.setSelectionMethod();
					break;
				}
			}
		}

		private void checkIfTypeSelectionMethod( Member.HigherMethod tm,
				List< Annotation > annotations ) {
			for( Annotation x : annotations ) {
				if( x.getName().identifier().equals( SUPER_SELECTION_METHOD_ANNOTATION ) ) {
					// TODO: Proper typechecks.
					tm.setTypeSelectionMethod();
					break;
				}
			}
		}

		protected abstract void visitMethodBody(
				CallableBodyScope bodyScope,
				Member.HigherMethod callable,
				Statement body
		);

		protected abstract void visitConstructorBody(
				CallableBodyScope bodyScope, Member.HigherConstructor callable,
				ConstructorDefinition n,
				Map< Member.HigherConstructor, Member.HigherConstructor > dependencies,
				Map< Member.HigherConstructor, Position > positions
		);

		private List< World > visitWorldParameters
				( List< FormalWorldParameter > worldParameters ) {
			List< World > result = new ArrayList<>( worldParameters.size() );
			for( FormalWorldParameter m : worldParameters ) {
				result.add( visitWorldParameter( m ) );
			}
			return result;
		}

		private World visitWorldParameter( FormalWorldParameter m ) {
			return annotate( m, new World( universe, m.name().identifier(), m ) );
		}

		private List< HigherTypeParameter > visitTypeParameters(
				List< FormalTypeParameter > typeParameters
		) {
			List< HigherTypeParameter > result = new ArrayList<>( typeParameters.size() );
			for( FormalTypeParameter m : typeParameters ) {
				HigherTypeParameter t = new HigherTypeParameter( universe,
						m.name().identifier(),
						visitWorldParameters( m.worldParameters() ),
						m );
				result.add( t );
				m.setTypeAnnotation( t ); // annotate AST
			}
			return result;
		}

		private void visitTypeParametersBound(
				TypeParameterDeclarationScope declarationScope,
				List< FormalTypeParameter > typeParameters, boolean delayBoundChecks
		) {
			for( FormalTypeParameter n : typeParameters ) {
				HigherTypeParameter p = n.typeAnnotation().get();
				TypeParameterScope scope = declarationScope.getScope( p );
				for( TypeExpression m : n.upperBound() ) {
					p.innerType().addUpperBound(
							visitGroundReferenceTypeExpression( scope, m, delayBoundChecks ) );
				}
				p.innerType().finaliseBound();
				taskQueue.enqueue( new MemberTask( Phase.MEMBER_DECLARATIONS, p, () -> {
					try {
						p.innerType().finaliseInterface();
					} catch( StaticVerificationException e ) {
						throw new AstPositionedException( n.position(), e );
					}
				} ) );
			}
		}

		private Type visitTypeExpression(
				Scope scope, TypeExpression n, boolean delayBoundChecks
		) {
			HigherDataType type = scope.assertLookupDataType( n.name().identifier() );
			List< World > worldArgs = n.worldArguments().stream()
					.map( x -> scope.lookupWorldParameter( x.name().identifier() ).orElseThrow(
							() -> new AstPositionedException( x.position(),
									new UnresolvedSymbolException( x.name().identifier() ) ) ) )
					.collect( Collectors.toList() );
			List< HigherReferenceType > typeArgs = n.typeArguments().stream()
					.map( x -> visitHigherReferenceTypeExpression( scope, x, delayBoundChecks ) )
					.collect( Collectors.toList() );
			try {
				if( worldArgs.isEmpty() ) {
					// partial application, higher kinded
					return annotate( n, type.partiallyApplyTo( typeArgs ) );
				} else {
					GroundDataType g = type.applyTo( worldArgs, typeArgs );
					if( !g.isInstantiationChecked() ) {
						if( delayBoundChecks ) {
							taskQueue.enqueue( Phase.BOUND_CHECKS, () -> {
								try {
									g.checkInstantiation();
								} catch( StaticVerificationException e ) {
									throw new AstPositionedException( n.position(), e );
								}
							} );
						} else {
							try {
								g.checkInstantiation();
							} catch( StaticVerificationException e ) {
								throw new AstPositionedException( n.position(), e );
							}
						}
					}
					return annotate( n, g );
				}
			} catch( StaticVerificationException e ) {
				System.out.println(n.name());
				throw new AstPositionedException( n.position(), e );
			}
		}

		protected HigherReferenceType visitHigherReferenceTypeExpression(
				Scope scope, TypeExpression n, boolean delayBoundChecks
		) {
			try {
				return assertHigherReferenceType(
						visitTypeExpression( scope, n, delayBoundChecks ) );
			} catch( StaticVerificationException e ) {
				throw new AstPositionedException( n.position(), e );
			}
		}

		protected GroundDataType visitGroundDataTypeExpression(
				Scope scope, TypeExpression n, boolean delayBoundChecks
		) {
			try {
				return assertGroundDataType(
						visitTypeExpression( scope, n, delayBoundChecks ) );
			} catch( StaticVerificationException e ) {
				throw new AstPositionedException( n.position(), e );
			}
		}

		protected GroundReferenceType visitGroundReferenceTypeExpression(
				Scope scope, TypeExpression n, boolean delayBoundChecks
		) {
			try {
				return assertGroundReferenceType(
						visitTypeExpression( scope, n, delayBoundChecks ) );
			} catch( StaticVerificationException e ) {
				throw new AstPositionedException( n.position(), e );
			}
		}

		protected GroundInterface visitGroundInterfaceExpression(
				Scope scope, TypeExpression n, boolean delayBoundChecks
		) {
			try {
				return assertGroundInterface(
						visitTypeExpression( scope, n, delayBoundChecks ) );
			} catch( StaticVerificationException e ) {
				throw new AstPositionedException( n.position(), e );
			}
		}

		protected GroundClass visitGroundClassExpression(
				Scope scope, TypeExpression n, boolean delayBoundChecks
		) {
			try {
				return assertGroundClass( visitTypeExpression( scope, n, delayBoundChecks ) );
			} catch( StaticVerificationException e ) {
				throw new AstPositionedException( n.position(), e );
			}
		}

		private static String formattedAssertTypeMessage( Type type, String format ) {
			String m;
			if( type instanceof GroundEnum ) {
				m = "an enum";
			} else if( type instanceof GroundClass ) {
				m = "a class";
			} else if( type instanceof GroundInterface ) {
				m = "an interface";
			} else if( type instanceof GroundPrimitiveDataType ) {
				m = "a primitive type";
			} else if( type instanceof HigherEnum ) {
				m = "a higher-kinded enum";
			} else if( type instanceof HigherClass ) {
				m = "a higher-kinded class";
			} else if( type instanceof HigherInterface ) {
				m = "a higher-kinded interface";
			} else if( type instanceof HigherPrimitiveDataType ) {
				m = "a higher-kinded primitive type";
			} else if( type instanceof HigherReferenceType ) {
				m = "a higher-kinded reference type";
			} else if( type instanceof HigherDataType ) {
				m = "a higher-kinded data type";
			} else {
				m = "a role";
			}
			return String.format( format, type, type.kind(), m );
		}

		private static HigherReferenceType assertHigherReferenceType( Type t ) {
			if( t instanceof HigherReferenceType ) {
				return (HigherReferenceType) t;
			} else {
				throw new StaticVerificationException( formattedAssertTypeMessage( t,
						"higher-kinded reference type expected, '%1$s' is %3$s" ) );
			}
		}

		private static GroundDataType assertGroundDataType( Type t ) {
			if( t instanceof GroundDataType ) {
				return (GroundDataType) t;
			} else {
				throw new StaticVerificationException(
						formattedAssertTypeMessage( t, "data type expected, '%1$s' is %3$s" ) );
			}
		}

		private static GroundReferenceType assertGroundReferenceType( Type t ) {
			if( t instanceof GroundReferenceType ) {
				return (GroundReferenceType) t;
			} else {
				throw new StaticVerificationException( formattedAssertTypeMessage( t,
						"reference type expected, '%1$s' is %3$s" ) );
			}
		}

		private static GroundClass assertGroundClass( Type t ) {
			if( t instanceof GroundClass ) {
				return (GroundClass) t;
			} else {
				throw new StaticVerificationException(
						formattedAssertTypeMessage( t, "class expected, '%1$s' is %3$s" ) );
			}
		}

		private static GroundInterface assertGroundInterface( Type t ) {
			if( t instanceof GroundInterface ) {
				return (GroundInterface) t;
			} else {
				throw new StaticVerificationException(
						formattedAssertTypeMessage( t, "interface expected, '%1$s' is %3$s" ) );
			}
		}

		private static GroundEnum assertGroundEnum( Type t ) {
			if( t instanceof GroundEnum ) {
				return (GroundEnum) t;
			} else {
				throw new StaticVerificationException(
						formattedAssertTypeMessage( t, "enum expected, '%1$s' is %3$s" ) );
			}
		}

		< N extends WithTypeAnnotation< ? super T >, T > T annotate( N n, T t ) {
			n.setTypeAnnotation( t );
			return t;
		}

		private static class MemberTask
				extends TaskQueue.Task {

			private final HigherReferenceType type;

			public MemberTask( Phase phase, HigherReferenceType type, Runnable task ) {
				super( phase, task );
				this.type = type;
			}

			@Override
			public int compareTo( TaskQueue.Task o ) {
				int i = super.compareTo( o );
				if( i == 0 && o instanceof MemberTask ) {
					MemberTask m = (MemberTask) o;
					if( m.type.isStrictSubtypeOf( this.type ) ) {
						i = 1;
					} else if( this.type.isStrictSubtypeOf( m.type ) ) {
						i = -1;
					}
				}
				return i;
			}

			boolean dependenciesReady = false;

			@Override
			protected boolean isReady() {
				if( !dependenciesReady ) {
					if( type instanceof HigherTypeParameter ) {
						dependenciesReady = ( (HigherTypeParameter) type ).innerType()
								.upperBound().allMatch( GroundReferenceType::isInterfaceFinalised );
					} else {
						dependenciesReady = ( (HigherClassOrInterface) type ).innerType()
								.extendedClassesOrInterfaces()
								.allMatch( GroundReferenceType::isInterfaceFinalised );
					}
				}
				return super.isReady() && dependenciesReady;
			}
		}

	}

	private static class HeaderVisitor extends Visitor {
		public HeaderVisitor( TaskQueue taskQueue, Universe universe ) {
			super( taskQueue, universe );
		}

		@Override
		protected void checkPrimaryTemplate(
				TemplateDeclaration n, String primaryType, String family
		) {
			/* we are more permissive with header files, this may change in the future */
		}

		@Override
		protected void checkConstructorsDependencies(
				Map< Member.HigherConstructor, Member.HigherConstructor > dependencies,
				Map< Member.HigherConstructor, Position > positions
		) {
			/* header files may specify constructor bodies but are ignored */
		}

		@Override
		protected void visitMethodBody(
				CallableBodyScope bodyScope, Member.HigherMethod callable, Statement body
		) {
			/* header files may specify method bodies but are ignored */
		}

		@Override
		protected void visitConstructorBody(
				CallableBodyScope bodyScope, Member.HigherConstructor callable,
				ConstructorDefinition n,
				Map< Member.HigherConstructor, Member.HigherConstructor > dependencies,
				Map< Member.HigherConstructor, Position > positions
		) {
			/* header files may specify constructor bodies but are ignored */
		}

	}

	private static class SourceVisitor
			extends Visitor {
		public SourceVisitor( TaskQueue taskQueue, Universe universe ) {
			super( taskQueue, universe );
		}

		@Override
		protected void visitImportDeclarations( Scope scope, List< ImportDeclaration > imports ) {
			taskQueue().enqueue( Phase.HIERARCHY, () -> {
				for( ImportDeclaration n : imports ) {
					if( !n.isOnDemand() ) {
						n.setTypeAnnotation( scope.assertLookupClassOrInterface( n.name() ) );
					}
				}
			} );
		}

		@Override
		protected void checkPrimaryTemplate(
				TemplateDeclaration n, String primaryType, String family
		) {
			if( n.isPublic() && !n.name().identifier().equals( primaryType ) ) {
				throw new AstPositionedException( n.position(),
						new StaticVerificationException( family
								+ " '" + n.name().identifier()
								+ "' is public, should be declared in a file named '"
								+ n.name().identifier()
								+ choral.compiler.SourceObject.ChoralSourceObject.FILE_EXTENSION + "'" ) );
			}

		}

		@Override
		protected void checkConstructorsDependencies(
				Map< Member.HigherConstructor, Member.HigherConstructor > dependencies,
				Map< Member.HigherConstructor, Position > positions
		) {
			Queue< Member.HigherConstructor > q = new LinkedList<>( dependencies.keySet() );
			List< Member.HigherConstructor > v = new ArrayList<>( dependencies.keySet() );
			while( !q.isEmpty() ) {
				v.clear();
				Member.HigherConstructor c = q.peek();
				while( c != null ) {
					if( v.contains( c ) ) {
						throw new AstPositionedException( positions.get( c ),
								new StaticVerificationException(
										"recursive constructor invocation" ) );
					}
					v.add( c );
					q.remove( c );
					c = dependencies.get( c );
				}
			}
		}

		@Override
		protected void visitMethodBody(
				CallableBodyScope bodyScope, HigherMethod callable, Statement body
		) {
			if( callable.isAbstract() ) {
				if( body != null ) {
					throw new AstPositionedException( body.position(),
							new StaticVerificationException(
									"abstract methods cannot have bodies" ) );
				}
				// nothing to check :)
			} else {
				if( body == null ) {
					throw new StaticVerificationException(
							"non-abstract methods must have bodies" );
				} else {
					boolean returnChecked;
					
					callable.addChannel(bodyScope.getChannels()); // find all available channels

					returnChecked = new Check( bodyScope,
						callable.innerCallable().returnType(), callable )
						.visit( body );
					if( !callable.innerCallable().returnType().isVoid() && !returnChecked ) {
						throw new AstPositionedException( body.position(),
								new StaticVerificationException( "missing return statement" ) );
					}	
				}
			}
		}

		@Override
		protected void visitConstructorBody(
				CallableBodyScope scope,
				Member.HigherConstructor callable,
				ConstructorDefinition d,
				final Map< Member.HigherConstructor, Member.HigherConstructor > dependencies,
				final Map< Member.HigherConstructor, Position > positions
		) {
			if( d.explicitConstructorInvocation().isEmpty() ) {
				GroundClass t = scope.lookupSuper();
				if( t != null && t.constructors()
						.filter( x -> x.isAccessibleFrom( scope.lookupThis() ) )
						.noneMatch( x -> x.typeParameters().size() == 0 && x.arity() == 0 )
				) {
					throw new AstPositionedException( d.position(),
							new StaticVerificationException(
									"there is no default constructor available in '" + t + "'" ) );
				}
			} else {
				MethodCallExpression n = d.explicitConstructorInvocation().get();
				GroundClass t = ( "this".equals( n.name().identifier() ) )
						? scope.lookupThis()
						: scope.lookupSuper();
				if( t == null ) {
					throw new AstPositionedException( n.position(),
							new StaticVerificationException(
									"cannot resolve 'super', class '"
											+ scope.lookupThis().typeConstructor()
											+ "' does not extend any class" ) );
				}
				List< ? extends HigherReferenceType > typeArgs = n.typeArguments().stream()
						.map( x -> visitHigherReferenceTypeExpression( scope, x, false ) )
						.collect( Collectors.toList() );
				List< ? extends GroundDataType > args = n.arguments().stream()
						.map( x -> assertNotVoid( synth( scope, x, true ), x.position() ) )
						.collect( Collectors.toList() );
				List< ? extends Member.GroundCallable > ms = findMostSpecificCallable(
						typeArgs,
						args,
						t.constructors().filter( x -> x.isAccessibleFrom( scope.lookupThis() ) )
				);
				if( ms.isEmpty() ) {
					throw new AstPositionedException( n.position(),
							new StaticVerificationException( "cannot resolve constructor '" + t +
									args.stream().map( Object::toString ).collect( Formatting
											.joining( ",", "(", ")", "" ) )
									+ "'" ) );
				} else if( ms.size() > 1 ) {
					throw new AstPositionedException( n.position(),
							new StaticVerificationException( "ambiguous constructor invocation, " +
									ms.stream().map( x -> "'" + t +
													x.signature().parameters().stream()
															.map( y -> y.type().toString() )
															.collect( Formatting.joining( ",", "(", ")",
																	"" ) ) + "'" )
											.collect( Collectors.collectingAndThen(
													Collectors.toList(),
													Formatting.joiningOxfordComma() ) ) ) );
				}
				Member.GroundConstructor selected = (Member.GroundConstructor) ms.get( 0 );
				// n.setMethodAnnotation( selected );
				dependencies.put( callable, selected.higherCallable() );
				positions.put( callable, n.position() );
			}
			new Check( scope, universe().voidType() ).visit( d.blockStatements() );
		}

		private List< ? extends Member.GroundCallable > findMostSpecificCallable(
				List< ? extends HigherReferenceType > typeArgs,
				List< ? extends GroundDataType > args,
				Stream< ? extends Member.HigherCallable > callables
		) {
			List< ? extends Member.HigherCallable > cs = callables.filter( x ->
					x.typeParameters().size() == typeArgs.size() && x.arity() == args.size()
			).collect( Collectors.toList() );
			// find most specific w/o unboxing
//	/*DEBUG*/
//			System.out.println( "=================================================" );
//			System.out.println( args.stream().map( Type::toString ).collect(
//					Formatting.joining( ",", "(", ")", "()" ) ) );
//			System.out.println( "candidates: " + cs.size() );
			List< Member.GroundCallable > ms = new ArrayList<>( cs.size() );
			List< Member.GroundCallable > rs = new ArrayList<>( cs.size() );
			int phase = 1; /* phase 1 w/o boxing conversions, phase 2 boxing conversions*/
			do {
				ms.clear();
				for( Member.HigherCallable x : cs ) {
//					System.out.println( "------------------------------------------------" );
					Member.GroundCallable c;
					try {
						c = x.applyTo( typeArgs );
					} catch( StaticVerificationException e ) {
//						System.out.println( "type parameter compatible: false" );
						continue;
					}
//					System.out.println( "type parameter compatible: true" );
					List< ? extends Signature.Parameter > cparams = c.signature().parameters();
//					System.out.println( "checking: " + c );
					// check compatibility of actual parameters and formal ones.
					boolean incompatible = false;
					for( int i = 0; i < cparams.size(); i++ ) {
						GroundDataType a = args.get( i );
						GroundDataType p = cparams.get( i ).type();
//						System.out.printf( "'%s' isSubtypeOf '%s': %s\n", a,p, a.isSubtypeOf( p ) );
						if( phase == 1 ) {
							incompatible = !a.isSubtypeOf_relaxed( p );
						} else {
							if( p.isPrimitive() ) {
								if( a instanceof GroundClass && ( (GroundClass) a ).isBoxedType() ) {
									a = ( (GroundClass) a ).unboxedType();
								}
							} else {
								if( a.isPrimitive() ) {
									a = ( (GroundPrimitiveDataType) a ).boxedType();
								}
							}
//							System.out.printf( "'%s' isAssignableTo '%s': %s\n", a,p,a.isAssignableTo( p ) );
							incompatible = !a.isAssignableTo_relaxed( p );
						}
						if( incompatible ) {
							break;
						}
					}
//					System.out.println( "actual parameter compatible: " + !incompatible );
					if( incompatible ) {
						continue;
					}
					// check if most specific w/o unboxing
					boolean mostSpecific = true;
					for( Member.GroundCallable m : ms ) {
						List< ? extends Signature.Parameter > mparams = m.signature().parameters();
						boolean mcsub = true;
						boolean cmsub = true;
						for( int i = 0; i < cparams.size(); i++ ) {
							GroundDataType cp = cparams.get( i ).type();
							GroundDataType mp = mparams.get( i ).type();
							mcsub &= mp.isSubtypeOf_relaxed( cp );
							cmsub &= cp.isSubtypeOf_relaxed( mp );
						}
						if( mcsub ) {
//							System.out.println( "subsumed by most specific candidate " + m );
							mostSpecific = false;
						} else if( cmsub ) {
//							System.out.println( "subsumes most specific candidate " + m );
							rs.add( m );
						}
					}
					ms.removeAll( rs );
					rs.clear();
//					System.out.println( "most specific candidate: " + mostSpecific );
					if( mostSpecific ) {
						ms.add( c );
					}
				}
//				System.out.println( "------------------------------------------------" );
//				System.out.println( "end of phase: " + phase );
//				System.out.println( "found: " + ms.size() );
				phase++;
			} while( ms.isEmpty() && phase < 3 );
//			System.out.println( "=================================================" );
			return ms;
		}

		GroundDataTypeOrVoid synth( 
			VariableDeclarationScope scope, 
			Expression n, 
			HigherMethod method,
			Statement statement
		) {
			return new Synth( scope, method, statement ).visit( n );
		}

		// only kept because used in visitConstructorBody
		GroundDataTypeOrVoid synth(
				VariableDeclarationScope scope, 
				Expression n, 
				boolean explicitConstructorArg
		) {
			return new Synth( scope, explicitConstructorArg, null, null ).visit( n );
		}

		GroundDataTypeOrVoid synth(
				VariableDeclarationScope scope, 
				Expression n, 
				boolean explicitConstructorArg, 
				HigherMethod method,
				Statement statement
		) {
			return new Synth( scope, explicitConstructorArg, method, statement ).visit( n );
		}

		GroundDataTypeOrVoid synth( 
			VariableDeclarationScope scope, 
			Expression n, 
			List< ? extends World > homeWorlds,
			HigherMethod method,
			Statement statement
		) {
			return new Synth( scope, homeWorlds, method, statement ).visit( n );
		}

		GroundDataTypeOrVoid synth(
				VariableDeclarationScope scope, 
				Expression n, 
				boolean explicitConstructorArg, 
				List< ? extends World > homeWorlds,
				HigherMethod method,
				Statement statement 
		) {
			return new Synth( scope, explicitConstructorArg, homeWorlds, method, statement ).visit( n );
		}

		GroundDataType assertNotVoid( GroundDataTypeOrVoid t, Position position ) {
			if( t.isVoid() ) {
				throw new AstPositionedException( position,
						new StaticVerificationException( "data type expected, found 'void'" ) );
			} else {
				return (GroundDataType) t;
			}
		}

		/**
		 * A relaxed version of Check. Doesn't throw exceptions on data location mismatch.
		 */
		private final class Check extends AbstractChoralVisitor< Boolean > {
			
			private VariableDeclarationScope scope;
			private final GroundDataTypeOrVoid expected;
			/** The enclosing method. */
			private HigherMethod enclosingMethod = null;

			public Check( VariableDeclarationScope scope, GroundDataTypeOrVoid expected ) {
				this.scope = scope;
				this.expected = expected;
			}

			public Check( VariableDeclarationScope scope, GroundDataTypeOrVoid expected, HigherMethod method ) {
				this.scope = scope;
				this.expected = expected;
				this.enclosingMethod = method;
			}

			private boolean visitAsInBlock( Statement n ) {
				// visit n as if { n }
				openBlock();
				boolean returnChecked = visit( n );
				closeBlock();
				return returnChecked;
			}

			private void openBlock() {
				scope = scope.newBlockScope();
			}

			private void closeBlock() {
				scope = (VariableDeclarationScope) scope.parent();
			}

			@Override
			public Boolean visit( Statement n ) {
				return n.accept( this );
			}

			@Override
			public Boolean visit( BlockStatement n ) {
				boolean returnChecked = visitAsInBlock( n.enclosedStatement() );
				return assertReachableContinuation( n, returnChecked );
			}

			@Override
			public Boolean visit( ExpressionStatement n ) {
				synth( scope, n.expression(), enclosingMethod, n );
				return assertReachableContinuation( n, false );
			}

			@Override
			public Boolean visit( IfStatement n ) {
				GroundDataTypeOrVoid type = synth( scope, n.condition(), enclosingMethod, n );
				if( type.primitiveTypeTag() != PrimitiveTypeTag.BOOLEAN &&
						type.specialTypeTag() != SpecialTypeTag.BOOLEAN ) {
					throw new AstPositionedException( n.condition().position(),
							new StaticVerificationException( "required an instance of type '"
									+ PrimitiveTypeTag.BOOLEAN
									+ "', found '" + type + "'" ) );
				}
				boolean returnChecked = visitAsInBlock( n.ifBranch() );
				returnChecked &= visitAsInBlock( n.elseBranch() );
				return assertReachableContinuation( n, returnChecked );
			}

			private /* static */ final EnumSet< PrimitiveTypeTag > legalSwitchPrimitiveTypes = EnumSet.of(
					PrimitiveTypeTag.CHAR, PrimitiveTypeTag.BYTE, PrimitiveTypeTag.SHORT,
					PrimitiveTypeTag.INT );

			private /* static */ final EnumSet< SpecialTypeTag > legalSwitchSpecialTypes = EnumSet.of(
					SpecialTypeTag.BYTE, SpecialTypeTag.SHORT, SpecialTypeTag.INTEGER,
					SpecialTypeTag.STRING );

			@Override
			public Boolean visit( SwitchStatement n ) {
				throw new UnsupportedOperationException("Switch statements not allowed\n\tStatement at " + n.position().toString());
			}

			@Override
			public Boolean visit( TryCatchStatement n ) {
				boolean returnChecked = visitAsInBlock( n.body() );
				for( Pair< VariableDeclaration, Statement > c : n.catches() ) {
					GroundDataType te = visitGroundDataTypeExpression( scope, c.left().type(),
							false );
					if( te.worldArguments().size() > 1 || !te.isSubtypeOf_relaxed(
							universe().specialType( SpecialTypeTag.EXCEPTION ).applyTo(
									te.worldArguments() ) )
					) {
						throw new AstPositionedException( c.left().type().position(),
								new StaticVerificationException( "required an instance of type '"
										+ SpecialTypeTag.EXCEPTION
										+ "', found '" + te + "'" ) );
					}
					openBlock();  // ---
					try {
						scope.declareVariable( c.left().name().identifier(), te );
					} catch( StaticVerificationException e ) {
						throw new AstPositionedException( c.left().name().position(), e );
					}
					returnChecked &= visit( c.right() );
					closeBlock(); // ---
				}
				return assertReachableContinuation( n, returnChecked );
			}

			@Override
			public Boolean visit( NilStatement n ) {
				return false;
			}

			@Override
			public Boolean visit( ReturnStatement n ) {
				if( n.returnExpression() == null ) {
					if( expected == universe().voidType() ) {
						return assertReachableContinuation( n, true );
					} else {
						throw new AstPositionedException( n.position(),
								new StaticVerificationException(
										"missing return value" ) );
					}
				} else {
					if( expected == universe().voidType() ) {
						throw new AstPositionedException( n.returnExpression().position(),
								new StaticVerificationException(
										"cannot return a value from a method with 'void' result type" ) );
					} else {
						// Since we are now looking at a retrun statement we know exactly what type 
						// we expect (including its role). Because of this, we can set the homeworld 
						// of the expression before looking at the expression itself.
						// 
						// For the method "public int@A fun()" we know, before looking at any return 
						// statements which type we need. Therefore we can set the homeworld of the
						// expression directly from the checker.
						GroundDataTypeOrVoid found = synth( scope, n.returnExpression(), ((GroundDataType) expected).worldArguments(), enclosingMethod, n );
						if( !found.isAssignableTo_relaxed( expected ) ) {
							throw new AstPositionedException( n.position(),
									new StaticVerificationException(
											"required type '" + expected + "', found '" + found + "'" ) );
						}
						return assertReachableContinuation( n, true );
					}
				}
			}

			@Override
			public Boolean visit( VariableDeclarationStatement n ) {
				for( VariableDeclaration x : n.variables() ) {
					GroundDataType type = visitGroundDataTypeExpression( scope, x.type(), false );
					scope.declareVariable( x.name().identifier(), type );
					x.initializer().ifPresent( e -> synth( scope, e, enclosingMethod, n ) );
				}
				return assertReachableContinuation( n, false );
			}

			public boolean assertReachableContinuation( Statement n, boolean returnChecked ) {
				if( returnChecked && n.hasContinuation() ) {
					throw StaticVerificationException.of(
							"unreachable statement",
							n.continuation().position() );
				}
				returnChecked |= visit( n.continuation() );
				n.setReturnAnnotation( returnChecked );
				return returnChecked;
			}
		}

		/**
		 * A relaxed version of Synth. Doesn't throw exceptions on data location mismatch.
		 */
		private final class Synth extends AbstractChoralVisitor< GroundDataTypeOrVoid > {

			public Synth( 
				VariableDeclarationScope scope, 
				HigherMethod method,
				Statement statement 
			) {
				this( scope, false, method, statement );
			}

			public Synth( 
				VariableDeclarationScope scope, 
				boolean explicitConstructorArg, 
				HigherMethod method,
				Statement statement 
			) {
				this.scope = scope;
				this.explicitConstructorArg = explicitConstructorArg;
				this.enclosingMethod = method;
				this.enclosingStatement = statement;
			}

			public Synth( 
				VariableDeclarationScope scope, 
				List< ? extends World > homeWorlds, 
				HigherMethod method,
				Statement statement   
			) {
				this( scope, false, homeWorlds, method, statement );
			}

			public Synth( 
				VariableDeclarationScope scope, boolean explicitConstructorArg, 
				List< ? extends World > homeWorlds,
				HigherMethod method,
				Statement statement 
			) {
				this.scope = scope;
				this.explicitConstructorArg = explicitConstructorArg;
				this.homeWorlds = homeWorlds;
				this.enclosingMethod = method;
				this.enclosingStatement = statement;
			}
			
			private final VariableDeclarationScope scope;
			private GroundDataTypeOrVoid left = null;
			private boolean leftStatic = false;
			private final boolean explicitConstructorArg;
			/** The worlds at which the expression takes place. */
			List< ? extends World > homeWorlds = Collections.emptyList();
			/** Whether we need to check the location of the expression.
			 * <p>
			 * For scopedexpressions like "this.obj.val" we only want to check location once for
			 * the whole expression, not once for every sub-expression.
			 */
			boolean checkLocation = true;
			/** If we are visiting a {@link ScopedExpression}, this will be the full name of the
			 * expression.
			 */
			String fullName;
			/** A reference to the enclosing method. */
			HigherMethod enclosingMethod;
			/** A reference to the enclosing statement. */
			Statement enclosingStatement;

			@Override
			public GroundDataTypeOrVoid visit( Expression n ) {
				return n.accept( this );
			}

			@Override
			public GroundDataTypeOrVoid visit( ScopedExpression n ) {
				// Consider a program like:
				// ```
				// MyObj@B obj = ...;
				// int@A x = obj.first.second + 1@A;
				// ```
				// We want to infer that B needs to send `obj.first.second` to A. This means:
				// 1. When we get to the root of the scoped expression, record its full name.
				// 2. We disable communication inference for all sub-expressions of the scoped
				//    expression, except the innermost one.
				// 
				// Note that if `second` is a field in `obj.first`, then `obj.first.second` is 
				// a `ScopedExpression` with `scope=obj` and `scopedExpression=first.second`. 
				// `first.second` is then a `ScopedExpression` with `scope=first` and 
				// `scopedExpression=second`. `second` would then be a `FieldAccessExpression`.

				if(checkLocation){
					fullName = n.toString();
					checkLocation = false;
				}

				/* In a ScopedExpression like a.b.c the second layer would have `scope=a.b` and 
				 * `scopedExpression=c`. we would detect that this is the end of our ScopedExpression.
				 * However when `scope` is visited, it would also be a ScopedExpression which would be 
				 * detected as the innermost ScopedExpression (since this would have scope=a and 
				 * scopedExpression=b). 
				 * To get around this, we "turn off" homeWorlds, ensuring that no dependencies are detected
				 * 
				 * TODO
				 * 		The explanation above is not fully correct
				 * 		Maybe extending this "turn off" of homeWorlds to also cover the visit of 
				 * 		scopedExpression would make checkLocation unnecessary 
				 */
				List< ? extends World > savedHomeWorlds = homeWorlds;
				homeWorlds = Collections.emptyList();
				left = visit( n.scope() );
				homeWorlds = savedHomeWorlds;
				GroundDataTypeOrVoid right = visit( n.scopedExpression() );

				// if n.scopedExpression() is not a ScopedExpression then n.scopedExpression() is the 
				// innermost expression (usually a FieldAccessExpression or a MethodCallExpression)
				if( !(n.scopedExpression() instanceof ScopedExpression) && !right.isVoid() ){
					inferCommunications(fullName, ((GroundDataType)right).worldArguments(), n);
				}
				
				left = null;
				return annotate( n, right );
			}

			private GroundPrimitiveDataType assertUnbox(
					GroundDataTypeOrVoid type, Position position
			) {
				GroundPrimitiveDataType result = unbox( type );
				if( result == null ) {
					throw new AstPositionedException( position,
							new StaticVerificationException(
									"primitive type expected, '" + type + "' cannot be converted" ) );
				} else {
					return result;
				}
			}

			private GroundPrimitiveDataType unbox( GroundDataTypeOrVoid type ) {
				if( type instanceof GroundPrimitiveDataType ) {
					return (GroundPrimitiveDataType) type;
				}
				if( type instanceof GroundClass ) {
					GroundClass c = (GroundClass) type;
					for( PrimitiveTypeTag p : PrimitiveTypeTag.values() ) {
						if( p.boxedType() == c.specialTypeTag() ) {
							return universe().primitiveDataType( p ).applyTo( c.worldArguments() );
						}
					}
				}
				return null;
			}

			private GroundDataType visitBinaryOp(
					BinaryExpression.Operator operator, GroundDataTypeOrVoid tvl,
					GroundDataTypeOrVoid tvr,
					Position position
			) {
				if( !tvl.isVoid() && !tvr.isVoid() ) {
					GroundDataType tl = (GroundDataType) tvl;
					GroundDataType tr = (GroundDataType) tvr;

					List< ? extends World > worlds; 	// the worlds of this expression
					if( !homeWorlds.isEmpty() )
						worlds = homeWorlds;			// if homeworlds is set, use that
					else
						worlds = tl.worldArguments(); 	// if no homeworld is set, use the left side's worlds
					
					GroundPrimitiveDataType pl = null;
					GroundPrimitiveDataType pr = null;
					switch( operator ) {
						case PLUS: {
							if( tl.specialTypeTag() == SpecialTypeTag.STRING
									|| tr.specialTypeTag() == SpecialTypeTag.STRING
									|| ( ( tl.specialTypeTag() == SpecialTypeTag.CHARACTER ||
									tl.primitiveTypeTag() == PrimitiveTypeTag.CHAR ) &&
									( tr.specialTypeTag() == SpecialTypeTag.CHARACTER ||
											tr.primitiveTypeTag() == PrimitiveTypeTag.CHAR ) )
							) {
								return universe().specialType( SpecialTypeTag.STRING ).applyTo(
										worlds );
							}
						}
						case MINUS:
						case MULTIPLY:
						case DIVIDE:
						case REMAINDER:
							pl = assertUnbox( tl, position );
							pr = assertUnbox( tr, position );
							if( pl.primitiveTypeTag().isNumeric() && pr.primitiveTypeTag().isNumeric() ) {
								GroundPrimitiveDataType p = ( pl.primitiveTypeTag().compareTo(
										pr.primitiveTypeTag() ) > 0 )
										? pl
										: pr;
								if( p.primitiveTypeTag().compareTo(
										PrimitiveTypeTag.INT ) < 0 ) {
									// promote byte, char, short to int
									p = universe().primitiveDataType(
											PrimitiveTypeTag.INT ).applyTo(
												worlds );
								}
								return p;
							}
							break;
						case LESS:
						case LESS_EQUALS:
						case GREATER:
						case GREATER_EQUALS:
							pl = assertUnbox( tl, position );
							pr = assertUnbox( tr, position );
							if( pl.primitiveTypeTag().isNumeric() && pr.primitiveTypeTag().isNumeric() ) {
								return universe().primitiveDataType(
										PrimitiveTypeTag.BOOLEAN ).applyTo(
											worlds );
							}
							break;
						case OR:
						case AND:
							pl = assertUnbox( tl, position );
							pr = assertUnbox( tr, position );
							if( pl.primitiveTypeTag().isIntegral() && pr.primitiveTypeTag().isIntegral() ) {
								if( pl.primitiveTypeTag().compareTo(
										pr.primitiveTypeTag() ) > 0 ) {
									return pl;
								} else {
									return pr;
								}
							}
						case SHORT_CIRCUITED_OR:
						case SHORT_CIRCUITED_AND:
							pl = ( pl == null ) ? assertUnbox( tl, position ) : pl;
							pr = ( pr == null ) ? assertUnbox( tr, position ) : pr;
							if( pl.primitiveTypeTag() == PrimitiveTypeTag.BOOLEAN
									&& pr.primitiveTypeTag() == PrimitiveTypeTag.BOOLEAN ) {
								return tl;
							}
							break;
						case EQUALS:
						case NOT_EQUALS:
							if( ( tl instanceof GroundReferenceType && tr.isSubtypeOf_relaxed( tl ) ) ||
									( tr instanceof GroundReferenceType && tl.isSubtypeOf_relaxed(
											tr ) )
							) {
								return universe().primitiveDataType(
										PrimitiveTypeTag.BOOLEAN ).applyTo(
											worlds );
							} else {
								pl = assertUnbox( tl, position );
								pr = assertUnbox( tr, position );
								if( pl.primitiveTypeTag() == pr.primitiveTypeTag() ||
										( pl.primitiveTypeTag().isNumeric() && pr.primitiveTypeTag().isNumeric() )
								) {
									return universe().primitiveDataType(
											PrimitiveTypeTag.BOOLEAN ).applyTo(
												worlds );
								}
							}
					}
					
				}
				throw new AstPositionedException( position,
						new StaticVerificationException( "cannot apply '"
								+ operator + "' to '" + tvl
								+ "' and '" + tvr + "'" ) );
			}

			@Override
			public GroundDataType visit( AssignExpression n ) {
				GroundDataTypeOrVoid tvl = synth( scope, n.target(), explicitConstructorArg, enclosingMethod, enclosingStatement );
				if( tvl.isVoid() ) {
					throw new AstPositionedException( n.position(),
							new StaticVerificationException(
									"expected assignable variable" ) );
				}
				GroundDataType tl = (GroundDataType) tvl;
				homeWorlds = tl.worldArguments(); // the lefthand side of an assignment determines the worlds of the expression
				GroundDataTypeOrVoid tvr = synth( scope, n.value(), explicitConstructorArg, homeWorlds, enclosingMethod, enclosingStatement );
				if( tvr.isVoid() ) {
					throw new AstPositionedException( n.position(),
							new StaticVerificationException(
									"required type '" + tl + "', found '" + tvr + "'" ) );
				}
				GroundDataType tr = (GroundDataType) tvr;

				if( n.operator().hasOperation() ) {
					// tr might be promoted beyond tl
					tr = visitBinaryOp( n.operator().operation(), tl, tr, n.position() );
				}
				if( !tr.isAssignableTo_relaxed( tl ) ) {
					throw new AstPositionedException( n.position(),
							new StaticVerificationException(
									"required type '" + tl + "', found '" + tr + "'" ) );
				}
				return annotate( n, tl );
			}

			@Override
			public GroundDataType visit( BinaryExpression n ) {
				if( homeWorlds.isEmpty() )
					homeWorlds = setExpressionHome(n); 	// if homeworlds is not set, check if this 
														// expression's worlds are limited by literals

				GroundDataTypeOrVoid tl = synth( scope, n.left(), explicitConstructorArg, homeWorlds, enclosingMethod, enclosingStatement );
				// if homeWorlds was not initially set and the expression did not contain 
				// lliterals, the leftmost expression desides worlds
				if( homeWorlds.isEmpty() && !tl.isVoid() ) 	
					homeWorlds = ((GroundDataType)tl).worldArguments();
				GroundDataTypeOrVoid tr = synth( scope, n.right(), explicitConstructorArg, homeWorlds, enclosingMethod, enclosingStatement );
				return annotate( n, visitBinaryOp( n.operator(), tl, tr, n.position() ) );
			}

			@Override
			public GroundDataTypeOrVoid visit( EnclosedExpression n ) {
				return synth( scope, n.nestedExpression(), explicitConstructorArg, enclosingMethod, enclosingStatement );
			}

			private boolean checkMemberAccess( Member m ) {
				return ( !leftStatic || m.isStatic() ) && m.isAccessibleFrom( scope.lookupThis() );
			}

			@Override
			public GroundDataType visit( FieldAccessExpression n ) {
				// System.out.println( "FieldAccessExpression: " + n );
				String identifier = n.name().identifier();

				Optional< ? extends GroundDataType > result = Optional.empty();
				if( left == null ) {
					result = scope.lookupVariable( identifier );
					if( result.isEmpty() ) {
						left = scope.lookupThis();
						leftStatic = explicitConstructorArg;
					}
				}
				if( left instanceof GroundReferenceType ) {
					result = ( (GroundReferenceType) left ).field( identifier )
							.filter( this::checkMemberAccess )
							.map( Member.Field::type );
				}
				left = null;
				leftStatic = false;
				if( result.isEmpty() ) {
					throw new AstPositionedException( n.position(),
							new UnresolvedSymbolException( identifier ) );
				} else {
					if ( checkLocation ){ // If not part of a scopedexpression
						List< ? extends World > rightWorlds = result.get().worldArguments();
						
						// We should check world correspondence 
						inferCommunications(identifier, rightWorlds, n);
					}

					return annotate( n, result.get() );
				}
			}

			@Override
			public GroundDataType visit( StaticAccessExpression n ) {
				leftStatic = true;
				TypeExpression m = n.typeExpression();
				HigherReferenceType type = scope.assertLookupReferenceType( m.name().identifier() );
				List< World > worldArgs = m.worldArguments().stream()
						.map( x -> scope.lookupWorldParameter( x.name().identifier() ).orElseThrow(
								() -> new AstPositionedException( x.position(),
										new UnresolvedSymbolException( x.name().identifier() ) ) ) )
						.collect( Collectors.toList() );
				if( !m.typeArguments().isEmpty() ) {
					throw new AstPositionedException( m.typeArguments().get( 0 ).position(),
							new StaticVerificationException(
									"unexpected type argument in static member access" ) );
				}
				GroundReferenceType g = type.applyTo( worldArgs );
				annotate( n.typeExpression(), g );
				return annotate( n, g );
			}

			@Override
			public GroundDataType visit( ClassInstantiationExpression n ) {
				GroundClass t = visitGroundClassExpression( scope, n.typeExpression(), false );
				if( t.typeConstructor().isAbstract() ) {
					throw new AstPositionedException( n.position(),
							new StaticVerificationException(
									"'" + t + "' is abstract, cannot be instantiated" ) );
				}
				List< ? extends HigherReferenceType > typeArgs = n.typeArguments().stream()
						.map( x -> visitHigherReferenceTypeExpression( scope, x, false ) )
						.collect( Collectors.toList() );
				List< ? extends GroundDataType > args = n.arguments().stream()
						.map( x -> assertNotVoid(
								synth( scope, x, explicitConstructorArg, enclosingMethod, enclosingStatement ),
								x.position() ) )
						.collect( Collectors.toList() );
				List< ? extends Member.GroundCallable > ms = findMostSpecificCallable(
						typeArgs,
						args,
						t.constructors().filter( this::checkMemberAccess )
				);
				if( ms.isEmpty() ) {
					throw new AstPositionedException( n.position(),
							new StaticVerificationException( "cannot resolve constructor '" + t +
									args.stream().map( Object::toString ).collect( Formatting
											.joining( ",", "(", ")", "" ) )
									+ "'" ) );
				} else if( ms.size() > 1 ) {
					throw new AstPositionedException( n.position(),
							new StaticVerificationException( "ambiguous constructor invocation, " +
									ms.stream().map( x -> "'" + t +
													x.signature().parameters().stream()
															.map( y -> y.type().toString() )
															.collect( Formatting.joining( ",", "(", ")",
																	"" ) ) + "'" )
											.collect( Collectors.collectingAndThen(
													Collectors.toList(),
													Formatting.joiningOxfordComma() ) ) ) );
				}
				Member.GroundConstructor selected = (Member.GroundConstructor) ms.get( 0 );
				n.setConstructorAnnotation( selected );

				// Since findMostSpecificCallable has been relaxed to not check world 
				// corresponcence, we need to manually check world correspondence 
				// between the arguments and the selected method's parameters.
				for( int i = 0; i < args.size(); i++ ){
					List<? extends World> expectedArgWorlds = selected.higherCallable().innerCallable().signature().parameters().get(i).type().worldArguments();
					// probably a better way to check world correspondence, since the 
					// arguments have already been synthed earlier in this method
					synth( scope, n.arguments().get(i), explicitConstructorArg, expectedArgWorlds, enclosingMethod, enclosingStatement );
				}

				leftStatic = false;
				return t;
			}

			@Override
			public GroundDataTypeOrVoid visit( MethodCallExpression n ) {
				
				if( left == null ) { // only happens for simple method calls (local)
					left = scope.lookupThis();
					leftStatic = explicitConstructorArg;
				}

				List< ? extends HigherReferenceType > typeArgs = n.typeArguments().stream()
						.map( x -> visitHigherReferenceTypeExpression( scope, x, false ) )
						.collect( Collectors.toList() );
				List< ? extends GroundDataType > args = n.arguments().stream()
						.map( x -> {
							return assertNotVoid(
								synth( scope, x, explicitConstructorArg, enclosingMethod, enclosingStatement ),
								x.position() );
						} )
						.collect( Collectors.toList() );
				if( left instanceof GroundReferenceType ) {
					GroundReferenceType t = (GroundReferenceType) left;

					List< ? extends Member.GroundCallable > ms = findMostSpecificCallable(
							typeArgs,
							args,
							t.methods( n.name().identifier() ).filter( this::checkMemberAccess )
					);
					if( ms.isEmpty() ) {
						throw new AstPositionedException( n.position(),
								new StaticVerificationException( "cannot resolve method '"
										+ n.name().identifier()
										+ args.stream().map( Object::toString ).collect( Formatting
										.joining( ",", "(", ")", "" ) )
										+ "' in '" + t + "'" ) );
					} else if( ms.size() > 1 ) {
						throw new AstPositionedException( n.position(),
								new StaticVerificationException(
										"ambiguous method invocation, " +
												ms.stream().map( Member.GroundCallable::toString )
														.collect( Collectors.collectingAndThen(
																Collectors.toList(),
																Formatting.joiningOxfordComma() ) ) ) );
					}
					Member.GroundMethod selected = (Member.GroundMethod) ms.get( 0 );
					n.setMethodAnnotation( selected );
					leftStatic = false;

					// Since findMostSpecificCallable has been relaxed to not check world 
					// corresponcence, we need to check this manually
					for( int i = 0; i < args.size(); i++ ){
						List<? extends World> expectedArgWorlds = selected.higherCallable().innerCallable().signature().parameters().get(i).type().worldArguments();
						// probably a better way to check world correspondence, since the 
						// arguments have already been synthed earlier in this method
						synth( scope, n.arguments().get(i), explicitConstructorArg, expectedArgWorlds, enclosingMethod, enclosingStatement );
					}

					return selected.returnType();
				} else {
					throw new AstPositionedException( n.position(),
							new StaticVerificationException( "cannot resolve method '"
									+ n.name().identifier()
									+ args.stream().map( Object::toString ).collect( Formatting
									.joining( ",", "(", ")", "" ) )
									+ "' in 'void'" ) );
				}
			}

			@Override
			public GroundDataType visit( NotExpression n ) {
				GroundDataTypeOrVoid t = visit( n.expression() );
				GroundPrimitiveDataType p = assertUnbox( t, n.expression().position() );
				if( p.primitiveTypeTag() == PrimitiveTypeTag.BOOLEAN ) {
					return annotate( n, p );
				} else {
					throw new AstPositionedException( n.position(),
							new StaticVerificationException( "cannot apply '!' to '" + t
									+ "'" ) );
				}
			}

			@Override
			public GroundDataType visit( ThisExpression n ) {
				if( explicitConstructorArg ) {
					throw new AstPositionedException( n.position(),
							new StaticVerificationException(
									"cannot reference 'this' before constructor has been called" ) );

				}
				return annotate( n, scope.lookupThis() );
			}

			@Override
			public GroundDataType visit( SuperExpression n ) {
				throw new UnsupportedOperationException("Super expression not allowed\n\tExpression at " + n.position().toString());
			}

			@Override
			public GroundDataType visit( NullExpression n ) {
				throw new UnsupportedOperationException("Null expression not allowed\n\tExpression at " + n.position().toString());
			}

			public GroundDataType visit( LiteralExpression.BooleanLiteralExpression n ) {
				checkWorlds(n);
					
				return annotate( n, universe()
						.primitiveDataType( PrimitiveTypeTag.BOOLEAN )
						.applyTo( visitWorld( n.world() ) ) );
			}

			public GroundDataType visit( LiteralExpression.IntegerLiteralExpression n ) {
				checkWorlds(n);
				
				return annotate( n, universe()
						.primitiveDataType( PrimitiveTypeTag.INT )
						.applyTo( visitWorld( n.world() ) ) );
			}

			public GroundDataType visit( LiteralExpression.DoubleLiteralExpression n ) {
				checkWorlds(n);
				
				return annotate( n, universe()
						.primitiveDataType( PrimitiveTypeTag.DOUBLE )
						.applyTo( visitWorld( n.world() ) ) );
			}

			public GroundDataType visit( LiteralExpression.StringLiteralExpression n ) {
				checkWorlds(n);
				
				return annotate( n, universe()
						.specialType( SpecialTypeTag.STRING )
						.applyTo( List.of( visitWorld( n.world() ) ) ) );
			}

			@Override
			public GroundDataType visit( TypeExpression n ) {
				throw new UnsupportedOperationException("Type expression not allowed\n\tExpression at " + n.position().toString());
			}

			/**
			 * If the given expression's worlds don't correspond to homeworlds, a dependency is 
			 * added to the enclosing method.
			 */
			private void inferCommunications( 
				String expressionString, 
				List< ? extends World > fromWorlds, 
				Expression expression 
				){
				if( !homeWorlds.isEmpty() && !atHome(fromWorlds) ){
					enclosingMethod.addDependency(homeWorlds.stream().map( world -> (World)world ).toList(), expression, enclosingStatement);
				}
					
			}

			/**
			 * Returns true if all worlds in otherWorlds are equal to at least one world in homeWorlds
			 */
			private boolean atHome(List< ? extends World > otherWorlds ){
				return otherWorlds.stream().allMatch( other -> homeWorlds.stream().anyMatch( homeWorld -> other.equals( homeWorld )) );
			}

			private boolean atHome( List< ? extends World > home, List< ? extends World > otherWorlds ){
				return otherWorlds.stream().allMatch( other -> home.stream().anyMatch( homeWorld -> other.equals( homeWorld )) );
			}


			/**
			 * Checks the worldcorrespondence between the given literalexpression and homeworlds.
			 * If they don't match, an exception is thrown.
			 */
			private <T extends LiteralExpression<?>> void checkWorlds( T n ){
				if( !homeWorlds.isEmpty() && !atHome( List.of( visitWorld(n.world()) ) )){
					throw new AstPositionedException( n.position(),
							 new StaticVerificationException(
									 "Literal '" + n + "', cannot be used in an expression at world '" + homeWorlds + "'" ) );
				}
			}

			/**
			 * Checks if a given expression is locked to some world by looking at existance of literals
			 */
			private <E extends Expression > List<? extends World> setExpressionHome( E expression ){
				
				if( expression instanceof BinaryExpression ){
					List<? extends World> left = setExpressionHome( ((BinaryExpression)expression).left() );
					List<? extends World> right = setExpressionHome( ((BinaryExpression)expression).right() );
					
					if( !left.isEmpty() && !right.isEmpty() && !left.equals(right) ){
						throw new AstPositionedException( expression.position(),
							 new StaticVerificationException(
									 "Binary expression has literals of different worlds" ) );
					}
					return !left.isEmpty()? left: right;
				} else if( expression instanceof LiteralExpression ){
					return List.of( visitWorld(((LiteralExpression<?>)expression).world()) );
				}
				return Collections.emptyList();
			}

			public List< ? extends World > visitWorlds( List< WorldArgument > n ) {
				return n.stream().map( this::visitWorld ).collect( Collectors.toList() );
			}

			public World visitWorld( WorldArgument n ) {
				return annotate( n, scope.assertLookupWorldParameter( n.name() ) );
			}

		}
	}

	private interface Scope {

		Optional< ? extends HigherDataType > lookupDataType( String query );

		HigherDataType assertLookupDataType( String query );

		HigherDataType assertLookupDataType( Name query );

		Optional< ? extends HigherReferenceType > lookupReferenceType( String query );

		HigherReferenceType assertLookupReferenceType( String query );

		HigherReferenceType assertLookupReferenceType( Name query );

		Optional< ? extends HigherClassOrInterface > lookupClassOrInterface( String query );

		HigherClassOrInterface assertLookupClassOrInterface( String query );

		HigherClassOrInterface assertLookupClassOrInterface( Name query );

		Optional< ? extends HigherTypeParameter > lookupTypeParameter( String query );

		HigherTypeParameter assertLookupTypeParameter( String query );

		HigherTypeParameter assertLookupTypeParameter( Name query );

		Optional< ? extends World > lookupWorldParameter( String query );

		World assertLookupWorldParameter( String query );

		World assertLookupWorldParameter( Name query );

	}

	private static abstract class BaseScope implements Scope {

		@Override
		public final HigherDataType assertLookupDataType( String query ) {
			return lookupDataType( query ).orElseThrow(
					() -> new UnresolvedSymbolException( query ) );
		}

		@Override
		public final HigherDataType assertLookupDataType( Name query ) {
			return lookupDataType( query.identifier() ).orElseThrow(
					() -> new AstPositionedException( query.position(),
							new UnresolvedSymbolException( query.identifier() ) ) );
		}

		@Override
		public final HigherReferenceType assertLookupReferenceType( String query ) {
			return lookupReferenceType( query ).orElseThrow(
					() -> new UnresolvedSymbolException( query ) );
		}

		@Override
		public final HigherReferenceType assertLookupReferenceType( Name query ) {
			return lookupReferenceType( query.identifier() ).orElseThrow(
					() -> new AstPositionedException( query.position(),
							new UnresolvedSymbolException( query.identifier() ) ) );
		}

		@Override
		public final HigherClassOrInterface assertLookupClassOrInterface( String query ) {
			return lookupClassOrInterface( query ).orElseThrow(
					() -> new UnresolvedSymbolException( query ) );
		}

		@Override
		public final HigherClassOrInterface assertLookupClassOrInterface( Name query ) {
			return lookupClassOrInterface( query.identifier() ).orElseThrow(
					() -> new AstPositionedException( query.position(),
							new UnresolvedSymbolException( query.identifier() ) ) );
		}

		@Override
		public final HigherTypeParameter assertLookupTypeParameter( String query ) {
			return lookupTypeParameter( query ).orElseThrow(
					() -> new UnresolvedSymbolException( query ) );
		}

		@Override
		public final HigherTypeParameter assertLookupTypeParameter( Name query ) {
			return lookupTypeParameter( query.identifier() ).orElseThrow(
					() -> new AstPositionedException( query.position(),
							new UnresolvedSymbolException( query.identifier() ) ) );
		}

		@Override
		public final World assertLookupWorldParameter( String query ) {
			return lookupWorldParameter( query ).orElseThrow(
					() -> new UnresolvedSymbolException( query ) );
		}

		@Override
		public final World assertLookupWorldParameter( Name query ) {
			return lookupWorldParameter( query.identifier() ).orElseThrow(
					() -> new AstPositionedException( query.position(),
							new UnresolvedSymbolException( query.identifier() ) ) );
		}
	}

	public static class UnresolvedSymbolException
			extends StaticVerificationException {
		public UnresolvedSymbolException( String symbol ) {
			super( "cannot resolve symbol '" + symbol + "'" );
		}
	}

	private static abstract class ChildScope extends BaseScope {

		protected abstract Scope parent();

		@Override
		public Optional< ? extends HigherDataType > lookupDataType( String query ) {
			return parent().lookupDataType( query );
		}

		@Override
		public Optional< ? extends HigherReferenceType > lookupReferenceType( String query ) {
			return parent().lookupReferenceType( query );
		}

		@Override
		public Optional< ? extends HigherClassOrInterface > lookupClassOrInterface(
				String query
		) {
			return parent().lookupClassOrInterface( query );
		}

		@Override
		public Optional< ? extends HigherTypeParameter > lookupTypeParameter( String query ) {
			return parent().lookupTypeParameter( query );
		}

		@Override
		public Optional< ? extends World > lookupWorldParameter( String query ) {
			return parent().lookupWorldParameter( query );
		}

	}

	private static final class CompilationUnitScope extends BaseScope {

		private final List< HigherClassOrInterface > singleImports;
		private final List< ImportDeclaration > singleImportStatements;
		private final List< Package > onDemandImports;
		private final List< ImportDeclaration > onDemandImportStatements;
		private final Package declarationPackage;

		private final static String[] defaultOnDemandImports = new String[] { "java.lang", "choral.lang" };

		public CompilationUnitScope(
				Package declarationPackage, List< ImportDeclaration > declaredImports
		) {
			super();
			this.declarationPackage = declarationPackage;
			singleImportStatements = new ArrayList<>( declaredImports.size() );
			onDemandImportStatements = new ArrayList<>( declaredImports.size() );
			for( ImportDeclaration ip : declaredImports ) {
				if( ip.isOnDemand() ) {
					onDemandImportStatements.add( ip );
				} else {
					singleImportStatements.add( ip );
				}
			}
			singleImports = new ArrayList<>( singleImportStatements.size() );
			onDemandImports = new ArrayList<>(
					onDemandImportStatements.size() + defaultOnDemandImports.length );
			Package root = declarationPackage.universe().rootPackage();
			for( String defaultOnDemandImport : defaultOnDemandImports ) {
				onDemandImports.add( root.declarePackage( defaultOnDemandImport ) );
			}
		}

		private boolean pendingSingleImports = true;

		private void resolveSingleImports() {
			if( pendingSingleImports ) {
				for( ImportDeclaration ip : singleImportStatements ) {
					Package pkg = declarationPackage.universe().rootPackage();
					String[] path = ip.name().split( "\\." );
					int i = 0;
					while( i < path.length - 1 ) {
						Optional< Package > x = pkg.declaredPackage( path[ i ] );
						if( x.isPresent() ) {
							pkg = x.get();
						} else {
							throw new AstPositionedException( ip.position(),
									new StaticVerificationException(
											"cannot resolve symbol '" + path[ i ] + "'" ) );
						}
						i += 1;
					}
					Optional< ? extends HigherClassOrInterface > type = pkg.declaredType(
							path[ i ] );
					if( type.isPresent() ) {
						assertPublicAccess( type.get() );
						singleImports.add( type.get() );
					} else {
						throw new AstPositionedException( ip.position(),
								new StaticVerificationException(
										"cannot resolve symbol '" + path[ i ] + "'" ) );
					}
				}
				pendingSingleImports = false;
			}
		}

		private boolean pendingOnDemandImports = true;

		private void resolveOnDemandImports() {
			if( pendingOnDemandImports ) {
				for( ImportDeclaration ip : onDemandImportStatements ) {
					Package pkg = declarationPackage.universe().rootPackage();
					String[] path = ip.name().split( "\\." );
					int i = 0;
					while( i < path.length - 1 /* last one is always ".*" */ ) {
						Optional< Package > x = pkg.declaredPackage( path[ i ] );
						if( x.isPresent() ) {
							pkg = x.get();
						} else {
							throw new AstPositionedException( ip.position(),
									new StaticVerificationException(
											"cannot resolve symbol '" + path[ i ] + "'" ) );
						}
						i += 1;
					}
					onDemandImports.add( pkg );
				}
				pendingOnDemandImports = false;
			}
		}

		@Override
		public Optional< ? extends HigherDataType > lookupDataType( String query ) {
			Optional< ? extends HigherDataType > result = lookupClassOrInterface( query );
			if( result.isEmpty() ) {
				result = declarationPackage.universe().primitiveDataType( query );
			}
			return result;
		}

		@Override
		public Optional< ? extends HigherReferenceType > lookupReferenceType( String query ) {
			return lookupClassOrInterface( query );
		}

		@Override
		public Optional< ? extends HigherClassOrInterface > lookupClassOrInterface(
				String query
		) {
			String[] path = query.split( "\\." );
			Optional< ? extends HigherClassOrInterface > result;
			if( path.length > 1 ) {
				// fully qualified name (only because Choral does not have nested classes yet)
				Optional< Package > pkg = Optional.of(
						declarationPackage.universe().rootPackage() );
				int i = 0;
				while( pkg.isPresent() && i < path.length - 1 ) {
					pkg = pkg.get().declaredPackage( path[ i ] );
					i += 1;
				}
				if( pkg.isPresent() && i == path.length - 1 ) {
					Package p = pkg.get();
					result = p.declaredType( path[ i ] );
				} else {
					result = Optional.empty();
				}
			} else {
				// search current package
				result = declarationPackage.declaredType( query );
				// search imports
				if( result.isEmpty() ) {
					resolveSingleImports();
					for( HigherClassOrInterface type : singleImports ) {
						if( type.identifier().equals( query ) ) {
							result = Optional.of( type );
							break;
						}
					}
				}
				// search delayed imports
				if( result.isEmpty() ) {
					resolveOnDemandImports();
					List< HigherClassOrInterface > results = onDemandImports.stream()
							.map( x -> x.declaredType( query ) ).filter(
									Optional::isPresent ).map(
									Optional::get )
							.filter( this::hasPublicAccess ).collect( Collectors.toList() );
					if( results.size() == 0 ) {
						result = Optional.empty();
					} else if( results.size() == 1 ) {
						result = Optional.of( results.get( 0 ) );
					} else {
						throw new StaticVerificationException(
								"reference to '" + query + "' is ambiguous, " +
										results.stream().map(
														x -> "'" + x.identifier( true ) + "'" )
												.collect( Collectors.collectingAndThen(
														Collectors.toList(),
														Formatting.joiningOxfordComma() ) ) +
										" are ambiguous"
						);
					}
				}
			}
			result.ifPresent( this::assertPublicAccess );
			return result;
		}

		public Optional< ? extends HigherTypeParameter > lookupTypeParameter( String query ) {
			return Optional.empty();
		}

		public Optional< World > lookupWorldParameter( String query ) {
			return Optional.empty();
		}

		private final Map< HigherClassOrInterface, ClassOrInterfaceStaticScope > templateScopes = new HashMap<>();

		public ClassOrInterfaceStaticScope getScope( HigherClassOrInterface type ) {
			ClassOrInterfaceStaticScope scope = templateScopes.get( type );
			if( scope == null ) {
				scope = new ClassOrInterfaceStaticScope( this, type );
				templateScopes.put( type, scope );
			}
			return scope;
		}

		private boolean hasPublicAccess( HigherClassOrInterface type ) {
			return type.declarationContext() == declarationPackage || type.isPublic();
		}

		private void assertPublicAccess( HigherClassOrInterface type ) {
			if( !hasPublicAccess( type ) ) {
				throw new StaticVerificationException( type.variety().labelSingular
						+ " '"
						+ type.identifier( true )
						+ "' has not public access" );
			}
		}
	}

	private interface ClassOrInterfaceScope extends Scope {
		CallableScope getScope( Member.HigherCallable callable );
	}

	private static final class ClassOrInterfaceInstanceScope
			extends ChildScope
			implements ClassOrInterfaceScope, TypeParameterDeclarationScope {

		ClassOrInterfaceInstanceScope( ClassOrInterfaceStaticScope parent ) {
			this.parent = parent;
		}

		private final ClassOrInterfaceStaticScope parent;

		@Override
		protected ClassOrInterfaceStaticScope parent() {
			return parent;
		}

		@Override
		public Optional< ? extends HigherDataType > lookupDataType( String query ) {
			Optional< ? extends HigherDataType > result = parent.type.typeParameter( query );
			if( result.isEmpty() ) {
				result = super.lookupDataType( query );
			}
			return result;
		}

		@Override
		public Optional< ? extends HigherReferenceType > lookupReferenceType( String query ) {
			Optional< ? extends HigherReferenceType > result = parent.type.typeParameter( query );
			if( result.isEmpty() ) {
				result = super.lookupReferenceType( query );
			}
			return result;
		}

		@Override
		public Optional< ? extends HigherTypeParameter > lookupTypeParameter( String query ) {
			return parent.type.typeParameter( query );
		}

		private final Map< HigherTypeParameter, TypeParameterScope > typeParameterScopes = new HashMap<>();

		@Override
		public TypeParameterScope getScope( HigherTypeParameter parameter ) {
			TypeParameterScope scope = typeParameterScopes.get( parameter );
			if( scope == null ) {
				scope = new TypeParameterScope( this, parameter );
				typeParameterScopes.put( parameter, scope );
			}
			return scope;
		}

		private final Map< Member.HigherCallable, CallableScope > callableScopes = new HashMap<>();

		@Override
		public CallableScope getScope( Member.HigherCallable callable ) {
			CallableScope scope = callableScopes.get( callable );
			if( scope == null ) {
				scope = new CallableScope( this, callable );
				callableScopes.put( callable, scope );
			}
			return scope;
		}
	}

	private static final class ClassOrInterfaceStaticScope
			extends ChildScope
			implements ClassOrInterfaceScope {

		ClassOrInterfaceStaticScope( CompilationUnitScope parent, HigherClassOrInterface type ) {
			this.parent = parent;
			this.type = type;
		}

		private final CompilationUnitScope parent;

		@Override
		protected CompilationUnitScope parent() {
			return parent;
		}

		private final HigherClassOrInterface type;

		@Override
		public Optional< ? extends World > lookupWorldParameter( String query ) {
			return type.worldParameter( query );
		}

		private final ClassOrInterfaceInstanceScope instanceScope = new ClassOrInterfaceInstanceScope(
				this );

		public ClassOrInterfaceInstanceScope getScope() {
			return instanceScope;
		}

		private final Map< Member.HigherCallable, CallableScope > callableScopes = new HashMap<>();

		public CallableScope getScope( Member.HigherCallable callable ) {
			CallableScope scope = callableScopes.get( callable );
			if( scope == null ) {
				scope = new CallableScope( this, callable );
				callableScopes.put( callable, scope );
			}
			return scope;
		}

	}

	private interface TypeParameterDeclarationScope
			extends Scope {
		TypeParameterScope getScope( HigherTypeParameter p );
	}

	private static final class TypeParameterScope
			extends ChildScope {

		private final HigherTypeParameter typeParameter;

		public TypeParameterScope(
				TypeParameterDeclarationScope parent, HigherTypeParameter typeParameter
		) {
			this.parent = parent;
			this.typeParameter = typeParameter;
		}

		private final TypeParameterDeclarationScope parent;

		@Override
		protected TypeParameterDeclarationScope parent() {
			return parent;
		}

		@Override
		public Optional< ? extends World > lookupWorldParameter( String query ) {
			return typeParameter.worldParameter( query );
		}

	}

	private static final class CallableScope
			extends ChildScope
			implements TypeParameterDeclarationScope {

		private final Member.HigherCallable callable;

		public CallableScope( ClassOrInterfaceScope parent, Member.HigherCallable callable ) {
			this.parent = parent;
			this.callable = callable;
		}

		private final ClassOrInterfaceScope parent;

		@Override
		protected ClassOrInterfaceScope parent() {
			return parent;
		}

		@Override
		public Optional< ? extends HigherDataType > lookupDataType( String query ) {
			Optional< ? extends HigherDataType > result = callable.typeParameter( query );
			if( result.isEmpty() ) {
				return super.lookupDataType( query );
			}
			return result;
		}

		@Override
		public Optional< ? extends HigherReferenceType > lookupReferenceType( String query ) {
			Optional< ? extends HigherReferenceType > result = callable.typeParameter( query );
			if( result.isEmpty() ) {
				return super.lookupReferenceType( query );
			}
			return result;
		}

		@Override
		public Optional< ? extends HigherTypeParameter > lookupTypeParameter( String query ) {
			Optional< ? extends HigherTypeParameter > result = callable.typeParameter( query );
			if( result.isEmpty() && !callable.isStatic() ) {
				return super.lookupTypeParameter( query );
			}
			return result;
		}

		private final Map< HigherTypeParameter, TypeParameterScope > typeParameterScopes = new HashMap<>();

		@Override
		public TypeParameterScope getScope( HigherTypeParameter parameter ) {
			TypeParameterScope scope = typeParameterScopes.get( parameter );
			if( scope == null ) {
				scope = new TypeParameterScope( this, parameter );
				typeParameterScopes.put( parameter, scope );
			}
			return scope;
		}

		private CallableBodyScope bodyScope;

		public CallableBodyScope getScope() {
			if( bodyScope == null ) {
				bodyScope = new CallableBodyScope( this );
			}
			return bodyScope;
		}
	}

	private static final class CallableBodyScope extends ChildScope
			implements VariableDeclarationScope {

		private CallableBodyScope( CallableScope parent ) {
			this.parent = parent;
			for( Signature.Parameter p : parent.callable.innerCallable().signature().parameters() ) {
				variables.put( p.identifier(), p.type() );
			}
		}

		private final CallableScope parent;

		@Override
		public CallableScope parent() {
			return parent;
		}

		private final Map< String, GroundDataType > variables = new HashMap<>();

		@Override
		public List<Pair<String, GroundInterface>> getChannels(){
			List<Pair<String, GroundInterface>> channels = new ArrayList<>();
			HigherDataType diDataChannel = assertLookupDataType("choral.channels.DiDataChannel");
			HigherDataType diSelectChannel = assertLookupDataType("choral.channels.DiSelectChannel");
			

			// Look for channels in `this`
			lookupThis().fields().forEach( field -> {
				
				if( field.type().typeConstructor() instanceof HigherInterface ){
					HigherInterface typec = (HigherInterface)field.type().typeConstructor();
					if( typec.isSubtypeOf_relaxed( diDataChannel ) || typec.isSubtypeOf( diSelectChannel ) ){
						channels.add(new Pair<>(field.identifier(), (GroundInterface)field.type()));
					}
				}
			} );
			
			// Look for channels in this method's list of parameters
			variables.forEach( (key, val) -> {
				
				if( val.typeConstructor() instanceof HigherInterface ){
					HigherInterface typec = (HigherInterface)val.typeConstructor();
					if( typec.isSubtypeOf_relaxed( diDataChannel ) || typec.isSubtypeOf( diSelectChannel ) ){
						channels.add(new Pair<>(key, (GroundInterface)val));
					}					
				}
			} );
			return channels;
		}

		@Override
		public void declareVariable( String identifier, GroundDataType type ) {
			if( lookupVariable( identifier ).isEmpty() ) {
				variables.put( identifier, type );
			} else {
				throw new StaticVerificationException( "variable '" + identifier
						+ "' already defined in the scope" );
			}
		}

		@Override
		public Optional< ? extends GroundDataType > lookupVariable( String identifier ) {
			return Optional.ofNullable( variables.get( identifier ) );
		}

		@Override
		public Optional< ? extends GroundDataType > lookupVariableOrField( String identifier ) {
			GroundDataType result = variables.get( identifier );
			if( result == null ) {
				return parent().callable.declarationContext().field( identifier )
						.map( Member.Field::type );
			} else {
				return Optional.of( result );
			}
		}

		@Override
		public GroundClass lookupThis() {
			return (GroundClass) parent.callable.declarationContext();
		}

		@Override
		public GroundClass lookupSuper() {
			return lookupThis().extendedClass().orElseThrow(
					() -> new UnresolvedSymbolException( "super" ) );
		}

		public BlockScope newBlockScope() {
			return new BlockScope( this );
		}

	}

	private static class BlockScope extends ChildScope
			implements VariableDeclarationScope {

		private BlockScope( VariableDeclarationScope parent ) {
			this.parent = parent;
		}

		private final VariableDeclarationScope parent;

		@Override
		public VariableDeclarationScope parent() {
			return parent;
		}

		private final Map< String, GroundDataType > variables = new HashMap<>();

		@Override
		public List<Pair<String, GroundInterface>> getChannels(){
			List<Pair<String, GroundInterface>> channels = new ArrayList<>();
			HigherDataType diDataChannel = assertLookupDataType("choral.channels.DiDataChannel");
			HigherDataType diSelectChannel = assertLookupDataType("choral.channels.DiSelectChannel");

			// this fields
			lookupThis().fields().forEach( field -> {
				
				if( field.type().typeConstructor() instanceof HigherInterface ){
					HigherInterface typec = (HigherInterface)field.type().typeConstructor();
					if( typec.isSubtypeOf_relaxed( diDataChannel ) || typec.isSubtypeOf( diSelectChannel ) ){
						channels.add(new Pair<>(field.identifier(), (GroundInterface)field.type()));
					}
				}
			} );
			
			// method arguments
			variables.forEach( (key, val) -> {
				
				if( val.typeConstructor() instanceof HigherInterface ){
					HigherInterface typec = (HigherInterface)val.typeConstructor();
					if( typec.isSubtypeOf_relaxed( diDataChannel ) || typec.isSubtypeOf( diSelectChannel ) ){
						channels.add(new Pair<>(key, (GroundInterface)val));
					}					
				}
			} );
			if( parent() instanceof VariableDeclarationScope ){
				parent.getChannels().forEach( ch -> {
					if(!channels.contains(ch))
						channels.add(ch);
				} );;
			}
			return channels;
		}

		@Override
		public void declareVariable( String identifier, GroundDataType type ) {
			if( lookupVariable( identifier ).isEmpty() ) {
				variables.put( identifier, type );
			} else {
				throw new StaticVerificationException( "variable '" + identifier
						+ "' already defined in the scope" );
			}
		}

		@Override
		public Optional< ? extends GroundDataType > lookupVariable( String identifier ) {
			GroundDataType result = variables.get( identifier );
			if( result == null ) {
				return parent().lookupVariable( identifier );
			} else {
				return Optional.of( result );
			}
		}

		@Override
		public Optional< ? extends GroundDataType > lookupVariableOrField( String identifier ) {
			GroundDataType result = variables.get( identifier );
			if( result == null ) {
				return parent().lookupVariableOrField( identifier );
			} else {
				return Optional.of( result );
			}
		}

		@Override
		public GroundClass lookupThis() {
			return parent().lookupThis();
		}

		@Override
		public GroundClass lookupSuper() {
			return parent().lookupSuper();
		}

		public BlockScope newBlockScope() {
			return new BlockScope( this );
		}

	}

	private interface VariableDeclarationScope extends Scope {

		Optional< ? extends GroundDataType > lookupVariable( String identifier );

		default GroundDataType assertLookupVariable( String identifier ) {
			return lookupVariable( identifier ).orElseThrow(
					() -> new UnresolvedSymbolException( identifier ) );
		}

		default GroundDataType assertLookupVariable( Name query ) {
			return lookupVariable( query.identifier() ).orElseThrow(
					() -> new AstPositionedException( query.position(),
							new UnresolvedSymbolException( query.identifier() ) ) );
		}

		void declareVariable( String identifier, GroundDataType type );

		Optional< ? extends GroundDataType > lookupVariableOrField( String identifier );

		default GroundDataType assertLookupVariableOrField( String identifier ) {
			return lookupVariableOrField( identifier ).orElseThrow(
					() -> new UnresolvedSymbolException( identifier ) );
		}

		default GroundDataType assertLookupVariableOrField( Name query ) {
			return lookupVariableOrField( query.identifier() ).orElseThrow(
					() -> new AstPositionedException( query.position(),
							new UnresolvedSymbolException( query.identifier() ) ) );
		}

		GroundClass lookupThis();

		GroundClass lookupSuper();

		Scope parent();

		BlockScope newBlockScope();

		/**
		 * Collects channels available in the scope by looking at the fields of "this" 
		 * and the enclosing method's arguments
		 */
		List<Pair<String, GroundInterface>> getChannels();

	}

	private static class TaskQueue {

		final Map< HigherClassOrInterface, TaskQueue.Task > hierarchyConstructionTasks = new HashMap<>();

		static class Task
				implements Comparable< TaskQueue.Task >, Runnable {

			private final Phase phase;
			private final Runnable task;

			public Task( Phase phase, Runnable task ) {
				this.phase = phase;
				this.task = task;
			}

			int rounds = 0;

			@Override
			public int compareTo( TaskQueue.Task o ) {
				int i = this.phase.compareTo( o.phase );
				if( i == 0 ) {
					i = Integer.compare( this.rounds, o.rounds );
				}
				return i;
			}

			@Override
			public void run() {
				if( status() == Status.READY ) {
					status = Status.PROCESSING;
					task.run();
					status = Status.FINISHED;
				}
			}

			private void prepare() {
				if( status() == Status.WAITING ) {
					if( isReady() ) {
						status = Status.READY;
					} else {
						rounds += 1;
					}
				}
			}

			protected boolean isReady() {
				return true;
			}

			enum Status {WAITING, READY, PROCESSING, FINISHED}

			protected TaskQueue.Task.Status status = TaskQueue.Task.Status.WAITING;

			public TaskQueue.Task.Status status() {
				return status;
			}

		}

		PriorityQueue< TaskQueue.Task > tasks = new PriorityQueue<>(
				Comparator.naturalOrder() );

		public void process( Phase to ) {
			while( !tasks.isEmpty() ) {
				if( tasks.peek().phase.compareTo( to ) < 1 ) {
					TaskQueue.Task task = tasks.poll();
					task.prepare();
					switch( task.status() ) {
						case READY -> task.run();
						case WAITING -> enqueue( task );
					}
				} else {
					// no more tasks for this and prior phases
					break;
				}
			}
		}

		public void process() {
			while( !tasks.isEmpty() ) {
				TaskQueue.Task task = tasks.poll();
				task.prepare();
				switch( task.status() ) {
					case READY -> task.run();
					case WAITING -> enqueue( task );
				}
			}
		}

		void enqueue( TaskQueue.Task t ) {
			if( t.status() == Task.Status.WAITING || t.status() == Task.Status.READY ) {
				tasks.add( t );
			}
		}

		void enqueue( Phase p, Runnable t ) {
			enqueue( new TaskQueue.Task( p, t ) );
		}

	}

}
