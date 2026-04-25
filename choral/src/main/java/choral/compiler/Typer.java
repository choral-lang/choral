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

import choral.ast.*;
import choral.ast.body.*;
import choral.ast.expression.*;
import choral.ast.statement.*;
import choral.ast.type.FormalTypeParameter;
import choral.ast.type.FormalWorldParameter;
import choral.ast.type.TypeExpression;
import choral.ast.type.WorldArgument;
import choral.ast.visitors.AbstractChoralVisitor;
import choral.compiler.typer.ClassLifter;
import choral.compiler.typer.Phase;
import choral.compiler.typer.TaskQueue;
import choral.compiler.typer.scope.*;
import choral.exceptions.AstPositionedException;
import choral.exceptions.StaticVerificationException;
import choral.types.Package;
import choral.types.*;
import choral.types.Member.HigherCallable;
import choral.types.Universe.PrimitiveTypeTag;
import choral.types.Universe.SpecialTypeTag;
import choral.utils.Formatting;
import choral.utils.Pair;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Typer {

	/**
	 * The main entry point for type checking.
	 * @param sourceUnits Compilation units representing source code
	 * @param headerUnits Compilation units representing header files
	 * @return A reference to the sourceUnits, now annotated with type information
	 */
	public static Collection< CompilationUnit > annotate(
			Collection< CompilationUnit > sourceUnits,
			Collection< CompilationUnit > headerUnits,
			TyperOptions opts
	) {
		Universe universe = new Universe();
		return annotate( sourceUnits, headerUnits, universe, opts );
	}

	public static Collection< CompilationUnit > annotate(
			Collection< CompilationUnit > sourceUnits,
			Collection< CompilationUnit > headerUnits,
			Universe universe,
			TyperOptions opts
	) {
		TaskQueue taskQueue = new TaskQueue();
		Visitor headerVisitor = new HeaderVisitor( taskQueue, universe, opts );
		headerUnits.forEach( cu -> taskQueue.enqueue( Phase.TYPE_SYMBOL_DECLARATIONS,
				() -> headerVisitor.visit( cu ) ) );
		Visitor sourceVisitor = new SourceVisitor( taskQueue, universe, opts );
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
		private final ClassLifter classLifter;
		protected final TyperOptions opts;

		public Visitor( TaskQueue taskQueue, Universe universe, TyperOptions opts ) {
			this.taskQueue = taskQueue;
			this.universe = universe;
			this.classLifter = new ClassLifter( universe, taskQueue, opts );
			this.opts = opts;
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
				pkg = pkg.declarePackage( n.packageDeclaration().get() );
			}
			CompilationUnitScope scope = new CompilationUnitScope( pkg, n.imports(), classLifter );
			for( choral.ast.body.Class x : n.classes() ) {
				checkPrimaryTemplate( x, n, "class" );
				visitClass( scope, pkg, x );
			}
			for( choral.ast.body.Enum x : n.enums() ) {
				checkPrimaryTemplate( x, n, "enum" );
				visitEnum( scope, pkg, x );
			}
			for( choral.ast.body.Interface x : n.interfaces() ) {
				checkPrimaryTemplate( x, n, "interface" );
				visitInterface( scope, pkg, x );
			}
			visitImportDeclarations( scope, n.imports() );
		}

		protected void visitImportDeclarations(Scope scope, List< ImportDeclaration > ns ) {
		}

		protected abstract void checkPrimaryTemplate(
				TemplateDeclaration n, CompilationUnit cu, String family
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
			ClassOrInterfaceStaticScope classOrInterfaceStaticScope = declarationScope.getScope( t );
			TaskQueue.Task ht = new TaskQueue.Task( Phase.HIERARCHY, () -> {
				if( n.superClass().isEmpty() ) {
					t.innerType().setExtendedClass(); // default
					if( t.innerType().extendedClass().isPresent() ) {
						checkExtendsForCycles( t.innerType(), t.innerType().extendedClass().get() );
					}
				} else {
					try {
						GroundClass s = visitGroundClassExpression(
								classOrInterfaceStaticScope.getInstanceScope(),
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
								classOrInterfaceStaticScope.getInstanceScope(),
								x, true );
						t.innerType().addExtendedInterface( s );
						checkExtendsForCycles( t.innerType(), s );
					} catch( StaticVerificationException e ) {
						throw new AstPositionedException( x.position(), e );
					}
				}
				t.innerType().finaliseInheritance();
				visitTypeParametersBound( classOrInterfaceStaticScope.getInstanceScope(),
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
											: classOrInterfaceStaticScope.getInstanceScope(),
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
						throw new AstPositionedException( nm.position(),
								new StaticVerificationException(
										"abstract method in non-abstract class" ) );
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
							: classOrInterfaceStaticScope.getInstanceScope().getScope( tm );
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
					CallableScope callableScope = classOrInterfaceStaticScope.getInstanceScope().getScope(
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
				taskQueue.enqueue( new TaskQueue.MemberTask( Phase.MEMBER_DECLARATIONS, t, () -> {
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
				taskQueue.enqueue( new TaskQueue.MemberTask( Phase.MEMBER_DECLARATIONS, t, () -> {
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
								classOrInterfaceStaticScope.getInstanceScope(),
								x, true );
						t.innerType().addExtendedInterface( s );
						checkExtendsForCycles( t.innerType(), s );
					} catch( StaticVerificationException e ) {
						throw new AstPositionedException( x.position(), e );
					}
				}
				t.innerType().finaliseInheritance();
				taskQueue.hierarchyConstructionTasks.remove( t );
				visitTypeParametersBound( classOrInterfaceStaticScope.getInstanceScope(),
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
							: classOrInterfaceStaticScope.getInstanceScope().getScope( tm );
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
					taskQueue.enqueue( Phase.MEMBER_DEFINITIONS, () -> {
						try {
							visitMethodBody( methodScope.getScope(), tm,
									nm.body().orElse( null ) );
						} catch( StaticVerificationException e ) {
							throw new AstPositionedException( nm.position(), e );
						}
					} );
				}
				taskQueue.enqueue( new TaskQueue.MemberTask( Phase.MEMBER_DECLARATIONS, t, () -> {
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
				taskQueue.enqueue( new TaskQueue.MemberTask( Phase.MEMBER_DECLARATIONS, p, () -> {
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
				System.err.println(n.name());
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

	}

	private static class HeaderVisitor extends Visitor {
		public HeaderVisitor( TaskQueue taskQueue, Universe universe, TyperOptions opts ) {
			super( taskQueue, universe, opts );
		}

		@Override
		protected void checkPrimaryTemplate(
				TemplateDeclaration n, CompilationUnit cu, String family
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

	private static class SourceVisitor extends Visitor {

		public SourceVisitor( TaskQueue taskQueue, Universe universe, TyperOptions opts ) {
			super( taskQueue, universe, opts );
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
				TemplateDeclaration n, CompilationUnit cu, String family
		) {
			String sourceFile = cu.position().sourceFile();
			if (sourceFile == null) return;

			int k = Math.max( 0, sourceFile.lastIndexOf( '.' ) );
			int j = Math.min( k, sourceFile.lastIndexOf( File.separatorChar ) + 1 );
			String primaryType = sourceFile.substring( j, k );

			if( n.isPublic() && !n.name().identifier().equals( primaryType ) ) {
				throw new AstPositionedException( n.position(),
						new StaticVerificationException( family
								+ " '" + n.name().identifier()
								+ "' is public, should be declared in a file named '"
								+ n.name().identifier()
								+ SourceObject.ChoralSourceObject.FILE_EXTENSION + "'" ) );
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
				CallableBodyScope bodyScope, Member.HigherMethod callable, Statement body
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
					callable.addChannel(bodyScope.getChannels());
					boolean returnChecked = check(
							body, callable.innerCallable().returnType(), bodyScope, callable
					);
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
						? (GroundClass) scope.lookupThis()
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
						.map( x -> assertNotVoid( synth( scope, x, true, callable ), x.position() ) )
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
			callable.addChannel(scope.getChannels()); // find all available channels
			check( d.blockStatements(), universe().voidType(), scope, callable );
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
							incompatible = opts.relaxed() ?
									!a.isSubtypeOf_relaxed( p ) :
									!a.isSubtypeOf( p );
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
							incompatible = opts.relaxed() ?
									!a.isAssignableTo_relaxed( p ) :
									!a.isAssignableTo( p );
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
							mcsub &= mp.isSubtypeOf( cp );
							cmsub &= cp.isSubtypeOf( mp );
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

			if ( opts.relaxed() ) {
				return filterMethodsUsingWorlds( ms, args );
			}
			else {
				return ms;
			}
		}

		/**
		 * In the "relaxed" typer mode, there might be multiple methods that have exactly
		 * the same datatypes as parameters and differ only in worlds: for example,
		 * {@code myMethod(int@A, bool@B)} and {@code myMethod(int@B, bool@A)}. If that
		 * happens, we use the DWIM ("Do What I Mean") resolution strategy explained below.
		 * <p>
		 * If there's a method whose parameter worlds match the argument worlds exactly,
		 * e.g. {@code myMethod(int@A, bool@B)} with invocation {@code myMethod(1@A, true@B},
		 * we assume that's the method they wanted to invoke---even though they could
		 * conceivably have wanted us to infer this:
		 * {@code myMethod(chAB.com(1@A), chBA.com(true@B)}.
		 * <p>
		 * We also go a step further. Maybe there's a method that matches all argument worlds
		 * exactly, except for the i-th argument. So the i-th argument will have to be sent
		 * to some other world---but which one? Well, if all method overloads have the i-th
		 * parameter at the same world, then our choice is deterministic. So if we invoke
		 * {@code myMethod(1@A, true@A)} and our overloads are {@code myMethod(int@A, bool@B)}
		 * and {@code myMethod(int@B, bool@B)}, then we'll assume they wanted us to infer
		 * {@code myMethod(1@A, chAB.com(true@A))}---even though they could conceivably
		 * have wanted us to infer {@code myMethod(chAB.com(1@A), chAB.com(true@A))}.
		 */
		private static List<Member.GroundCallable> filterMethodsUsingWorlds(
				List< Member.GroundCallable > methods,
				List< ? extends GroundDataType > args
		){
			if (methods.size() <= 1)
				return methods;
			for( int i = 0; i < args.size(); i++ ){
				final int index = i;
				var argWorlds = args.get(index).worldArguments();

				// Find the methods whose i-th parameter world matches our i-th argument world.
				// If there are no such methods, check if they all have their i-th parameter
				// at the same world---if so, we can proceed because any of them would require
				// the same communications.
				var eligibleMethods = methods.stream()
						.filter( method -> getParamWorlds(method, index).equals(argWorlds) )
						.toList();
				if( eligibleMethods.isEmpty() ){
					var firstWorlds = getParamWorlds(methods.get(0), index);
					boolean allAtSameWorld = methods.stream().allMatch( method ->
							getParamWorlds(method, index).equals(firstWorlds) );
					if( !allAtSameWorld ){
						return Collections.emptyList();
					}
				} else {
					methods = eligibleMethods;
				}
			}

			return methods;
		}

		/**
		 * Returns the list of worlds associated with the i-th parameter of the given method.
		 * For example, the 1-th world of {@code myMethod(int@A, bool@B)} is B.
		 */
		private static List< ? extends World > getParamWorlds( Member.GroundCallable method, int i ){
			return method.signature().parameters().get(i).type().worldArguments();
		}

		boolean check(
				Statement statement,
				GroundDataTypeOrVoid expectedType,
				VariableDeclarationScope scope,
				Member.HigherCallable enclosingMethod
		) {
			return new Check( scope, expectedType, enclosingMethod, opts ).visit( statement );
		}

		GroundDataTypeOrVoid synth(
				VariableDeclarationScope scope,
				Expression n,
				boolean explicitConstructorArg,
				Member.HigherCallable enclosingMethod
		) {
			return new Synth(
					scope, explicitConstructorArg, Collections.emptyList(), enclosingMethod, null, opts
			).visit( n );
		}

		GroundDataType assertNotVoid( GroundDataTypeOrVoid t, Position position ) {
			if( t.isVoid() ) {
				throw new AstPositionedException( position,
						new StaticVerificationException( "data type expected, found 'void'" ) );
			} else {
				return (GroundDataType) t;
			}
		}

		private final class Check extends AbstractChoralVisitor< Boolean > {

			private VariableDeclarationScope scope;
			/** The type we expect to find. */
			private final GroundDataTypeOrVoid expected;
			/** The method that contains the current expression. */
			private final HigherCallable enclosingMethod;

			private final TyperOptions opts;

			public Check(
					VariableDeclarationScope scope,
					GroundDataTypeOrVoid expected,
					HigherCallable method,
					TyperOptions opts
			) {
				this.scope = scope;
				this.expected = expected;
				this.enclosingMethod = method;
				this.opts = opts;
			}

			private boolean visitAsInBlock( Statement n ) {
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
				synth( n.expression(), n );
				return assertReachableContinuation( n, false );
			}

			@Override
			public Boolean visit( IfStatement n ) {
				GroundDataTypeOrVoid type = synth( n.condition(), n );
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
				GroundDataTypeOrVoid guard = synth( n.guard(), n );
				if( !legalSwitchPrimitiveTypes.contains( guard.primitiveTypeTag() )
						&& !legalSwitchSpecialTypes.contains( guard.specialTypeTag() )
						&& !guard.isEnum() ) {
					throw new AstPositionedException( n.guard().position(),
							new StaticVerificationException( "incompatible types, found '" + guard
									+ "', required an instance of '" + PrimitiveTypeTag.CHAR
									+ "', '" + PrimitiveTypeTag.BYTE
									+ "', '" + PrimitiveTypeTag.SHORT
									+ "', '" + PrimitiveTypeTag.INT
									+ "', '" + SpecialTypeTag.BYTE
									+ "', '" + SpecialTypeTag.SHORT
									+ "', '" + SpecialTypeTag.INTEGER
									+ "', '" + SpecialTypeTag.STRING
									+ "', or an enum type" ) );
				}
				boolean returnChecked = true;
				// determines whether a case falls into the default case or not.
				boolean hasDefault = false;
				Set<String> casesFound = new HashSet<>(n.cases().size());
				for( Map.Entry< SwitchArgument< ? >, Statement > entry : n.cases().entrySet() ) {
					if( entry.getKey() instanceof SwitchArgument.SwitchArgumentLabel label) {
						if( guard.isEnum() ) {
							GroundEnum ge = (GroundEnum) guard;
							String id = label.argument().identifier();
							if( ge.field( id ).isEmpty() ) {
								throw new AstPositionedException( label.argument().position(),
										new UnresolvedSymbolException( id ) );
							} else {
								if(!casesFound.add(id)){
									throw new AstPositionedException( label.argument().position(),
										new StaticVerificationException(
												"duplicate case '" + id + "'" ) );
								}
							}
						} else {
							throw new AstPositionedException( label.argument().position(),
									new StaticVerificationException(
											"required a literal of type '" + guard + "', found a label" ) );
						}
					} else if( entry.getKey() instanceof SwitchArgument.SwitchArgumentLiteral l) {
						GroundDataTypeOrVoid argument = synth( l.argument(), n );
						String s = l.argument().content().toString();
						if( !argument.isAssignableTo( guard ) ) {
							throw new AstPositionedException( l.position(),
									new StaticVerificationException( "required type '" + guard
											+ "', found '" + guard + "'" ) );
						}
						if(!casesFound.add(s)){
							throw new AstPositionedException( l.argument().position(),
									new StaticVerificationException( "duplicate case '" + s + "'" ) );
						}
					} else {
						hasDefault = true;
					}
					returnChecked &= visitAsInBlock( entry.getValue() );
				}
				returnChecked &= hasDefault;
				return assertReachableContinuation( n, returnChecked );
			}

			@Override
			public Boolean visit( TryCatchStatement n ) {
				boolean returnChecked = visitAsInBlock( n.body() );
				// The "VariableDeclaration" here represents the caught exception
				for( Pair< VariableDeclaration, Statement > c : n.catches() ) {
					// get the type of the exception
					GroundDataType te = visitGroundDataTypeExpression(
							scope, c.left().type(), false );
					GroundClassOrInterface expectedType = universe()
							.specialType( SpecialTypeTag.EXCEPTION )
							.applyTo( te.worldArguments() );
					boolean isSubtype = opts.relaxed() ?
							te.isSubtypeOf_relaxed( expectedType ) :
							te.isSubtypeOf( expectedType );
					// exceptions only allowed one role
					if( te.worldArguments().size() > 1 || !isSubtype ) {
						throw new AstPositionedException( c.left().type().position(),
								new StaticVerificationException( "required an instance of type '"
										+ SpecialTypeTag.EXCEPTION
										+ "', found '" + te + "'" ) );
					}
					openBlock();  // ---
					try {
						// check whether variable already exists
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
					if( expected instanceof GroundDataType expected ) {
						List< ? extends World > expectedLocation = expected.worldArguments();
						GroundDataTypeOrVoid found = synth( n.returnExpression(), n, expectedLocation );
						boolean isAssignable = opts.relaxed() ?
								found.isAssignableTo_relaxed( expected ) :
								found.isAssignableTo( expected );
						if( !isAssignable ) {
							throw new AstPositionedException( n.position(),
									new StaticVerificationException(
											"required type '" + expected + "', found '" + found + "'" ) );
						}
						return assertReachableContinuation( n, true );
					} else {
						throw new AstPositionedException( n.returnExpression().position(),
								new StaticVerificationException(
										"cannot return a value from a method with 'void' result type" ) );
					}
				}
			}

			@Override
			public Boolean visit( VariableDeclarationStatement n ) {
				for( VariableDeclaration x : n.variables() ) {
					GroundDataType type = visitGroundDataTypeExpression( scope, x.type(), false );
					scope.declareVariable( x.name().identifier(), type );
					x.initializer().ifPresent( e -> synth( e, n ) );
				}
				return assertReachableContinuation( n, false );
			}

			/**
			 * Visits the statement's continuation and checks for unreachable statements (e.g., statements
			 * that will never be executed because the method is guaranteed to return or throw before
			 * control flow reaches them).
			 * @param n A statement that has already been typechecked.
			 * @param returnChecked True if 'n' is guaranteed to return before reaching its continuation.
			 * @return True if 'n' or its continuation are guaranteed to return a value.
			 */
			public boolean assertReachableContinuation( Statement n, boolean returnChecked ) {
				if( returnChecked && n.hasContinuation() ) {
					throw new AstPositionedException( n.continuation().position(),
							new StaticVerificationException( "unreachable statement" ) );
				}
				returnChecked |= visit( n.continuation() );
				n.setReturnAnnotation( returnChecked );
				return returnChecked;
			}

			GroundDataTypeOrVoid synth(
					Expression n, Statement statement
			) {
				return new Synth(
						scope, false, Collections.emptyList(), enclosingMethod, statement, opts
				).visit( n );
			}

			GroundDataTypeOrVoid synth(
					Expression n, Statement statement, List< ? extends World > homeWorlds
			) {
				return new Synth(
						scope, false, homeWorlds, enclosingMethod, statement, opts
				).visit( n );
			}

		}

		private final class Synth extends AbstractChoralVisitor< GroundDataTypeOrVoid > {

			public Synth(
				VariableDeclarationScope scope,
				boolean explicitConstructorArg,
				List< ? extends World > homeWorlds,
				HigherCallable method,
				Statement statement,
				TyperOptions opts
			) {
				this.scope = scope;
				this.explicitConstructorArg = explicitConstructorArg;
				this.homeWorlds = homeWorlds;
				this.enclosingMethod = method;
				this.enclosingStatement = statement;
				this.opts = opts;
			}

			private final VariableDeclarationScope scope;
			private GroundDataTypeOrVoid left = null;
			private boolean leftStatic = false;
			/**
			 * Whether we're typechecking the arguments of an explicit constructor invocation,
			 * e.g. the arguments of `super(...)` or `this(...)`.
			 */
			private final boolean explicitConstructorArg;
			private final TyperOptions opts;

			/**
			 * The set of worlds where we think the expression will be evaluated. For example, in
			 * the statement {@code int@B z = x + y;}, we set B as the home world. We use this
			 * information for communication inference: if x was located at another world A, we'd
			 * infer that B needs a copy of x from A.
			 */
			private List< ? extends World > homeWorlds;
			/** The method that contains the current expression. */
			private final HigherCallable enclosingMethod;
			/** The statement that contains the current expression. */
			private final Statement enclosingStatement;

			private boolean isInStaticContext() {
				return explicitConstructorArg || enclosingMethod.isStatic();
			}

			GroundDataTypeOrVoid synth( Expression n ) {
				return new Synth(
						scope, explicitConstructorArg, homeWorlds, enclosingMethod, enclosingStatement, opts
				).visit( n );
			}

			GroundDataTypeOrVoid synth( Expression n, List< ? extends World > homeWorlds ) {
				return new Synth(
					scope, explicitConstructorArg, homeWorlds, enclosingMethod, enclosingStatement, opts
				).visit( n );
			}

			@Override
			public GroundDataTypeOrVoid visit( Expression n ) {
				return n.accept( this );
			}

			@Override
			public GroundDataTypeOrVoid visit( ScopedExpression n ) {
				// This is a tricky case for the "relaxed" typing mode. Consider a program like:
				// ```
				// MyObj@B obj = ...;
				// int@A x = obj.first.second + 1@A;
				// ```
				// If we typecheck naively, we'll see that `obj` is a value at B being used in an
				// expression at A, and we'll naively infer that A depends on `obj`. Instead, we
				// want to infer that B needs to send `obj.first.second` to A.
				//
				// This means:
				// 1. When we get to the outermost part of the scoped expression, we record its full
				//    name.
				// 2. We disable world inference for all sub-expressions of the scoped
				//    expression, except the innermost one.

				// Temporarily turn off bookkeeping for the relaxed typer...
				List< ? extends World > savedHomeWorlds = homeWorlds;
				homeWorlds = Collections.emptyList();

				left = visit( n.scope() );
				GroundDataTypeOrVoid right = visit( n.scopedExpression() );

				// ...turn bookkeeping back on for the relaxed typer and record any dependencies
				// if `n` is the innermost part of the scoped expression.
				homeWorlds = savedHomeWorlds;
				if( !(n.scopedExpression() instanceof ScopedExpression) ){
					recordDependencies(n, right, homeWorlds);
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
				if( type instanceof GroundPrimitiveDataType groundType) {
					return groundType;
				}
				if( type instanceof GroundClass groundClass) {
					for( PrimitiveTypeTag p : PrimitiveTypeTag.values() ) {
						if( p.boxedType() == groundClass.specialTypeTag() ) {
							return universe().primitiveDataType( p ).applyTo( groundClass.worldArguments() );
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
				if( tvl.isVoid() || tvr.isVoid() ) {
					throw new AstPositionedException( position,
							new StaticVerificationException( "cannot apply '"
									+ operator + "' to '" + tvl
									+ "' and '" + tvr + "'" ) );
				}

				GroundDataType leftType = (GroundDataType) tvl;
				GroundDataType rightType = (GroundDataType) tvr;

				List< ? extends World > worlds;
				if ( opts.relaxed() ) {
					worlds = homeWorlds.isEmpty() ? leftType.worldArguments() : homeWorlds;
				}
				else {
					worlds = leftType.worldArguments();

					if( !( leftType.worldArguments().size() == 1 &&
							rightType.worldArguments().size() == 1 &&
							leftType.worldArguments().equals( rightType.worldArguments() )
							)
					) {
						throw new AstPositionedException( position,
								new StaticVerificationException( "cannot apply '"
										+ operator + "' to '" + tvl
										+ "' and '" + tvr + "'" ) );
					}
				}

				GroundPrimitiveDataType pl = null;
				GroundPrimitiveDataType pr = null;
				switch( operator ) {
					case PLUS: {
						if( leftType.specialTypeTag() == SpecialTypeTag.STRING
								|| rightType.specialTypeTag() == SpecialTypeTag.STRING
								|| ( ( leftType.specialTypeTag() == SpecialTypeTag.CHARACTER ||
								leftType.primitiveTypeTag() == PrimitiveTypeTag.CHAR ) &&
								( rightType.specialTypeTag() == SpecialTypeTag.CHARACTER ||
										rightType.primitiveTypeTag() == PrimitiveTypeTag.CHAR ) )
						) {
							return universe().specialType( SpecialTypeTag.STRING )
									.applyTo( worlds );
						}
					}
					case MINUS:
					case MULTIPLY:
					case DIVIDE:
					case REMAINDER:
						pl = assertUnbox( leftType, position );
						pr = assertUnbox( rightType, position );
						if( pl.primitiveTypeTag().isNumeric() && pr.primitiveTypeTag().isNumeric() ) {
							GroundPrimitiveDataType p =
									( pl.primitiveTypeTag().compareTo( pr.primitiveTypeTag() ) > 0 )
									? pl
									: pr;
							if( p.primitiveTypeTag().compareTo( PrimitiveTypeTag.INT ) < 0 ) {
								p = universe().primitiveDataType( PrimitiveTypeTag.INT )
										.applyTo( worlds );
							}
							return universe().primitiveDataType( p.primitiveTypeTag() )
									.applyTo( worlds );
						}
						break;
					case LESS:
					case LESS_EQUALS:
					case GREATER:
					case GREATER_EQUALS:
						pl = assertUnbox( leftType, position );
						pr = assertUnbox( rightType, position );
						if( pl.primitiveTypeTag().isNumeric() && pr.primitiveTypeTag().isNumeric() ) {
							return universe().primitiveDataType( PrimitiveTypeTag.BOOLEAN )
									.applyTo( worlds );
						}
						break;
					case OR: // bitwise / non-short-circuting comparison.
					case AND:
						pl = assertUnbox( leftType, position );
						pr = assertUnbox( rightType, position );
						if( pl.primitiveTypeTag().isIntegral() && pr.primitiveTypeTag().isIntegral() ) {
							if( pl.primitiveTypeTag().compareTo( pr.primitiveTypeTag() ) > 0 ) {
								return universe().primitiveDataType( pl.primitiveTypeTag() )
										.applyTo( worlds );
							} else {
								return universe().primitiveDataType( pr.primitiveTypeTag() )
										.applyTo( worlds );
							}
						}
					case SHORT_CIRCUITED_OR:
					case SHORT_CIRCUITED_AND:
						pl = ( pl == null ) ? assertUnbox( leftType, position ) : pl;
						pr = ( pr == null ) ? assertUnbox( rightType, position ) : pr;
						if( pl.primitiveTypeTag() == PrimitiveTypeTag.BOOLEAN
								&& pr.primitiveTypeTag() == PrimitiveTypeTag.BOOLEAN ) {
							return leftType;
						}
						break;
					case EQUALS:
					case NOT_EQUALS:
						boolean trSubtype = opts.relaxed() ? rightType.isSubtypeOf_relaxed( leftType ) : rightType.isSubtypeOf( leftType );
						boolean tlSubtype = opts.relaxed() ? leftType.isSubtypeOf_relaxed( rightType ) : leftType.isSubtypeOf( rightType );
						if( ( leftType instanceof GroundReferenceType && trSubtype ) ||
							( rightType instanceof GroundReferenceType && tlSubtype ) ) {
							return universe().primitiveDataType( PrimitiveTypeTag.BOOLEAN )
									.applyTo( worlds );
						} else {
							pl = assertUnbox( leftType, position );
							pr = assertUnbox( rightType, position );
							if( pl.primitiveTypeTag() == pr.primitiveTypeTag() ||
									( pl.primitiveTypeTag().isNumeric() && pr.primitiveTypeTag().isNumeric() )
							) {
								return universe().primitiveDataType( PrimitiveTypeTag.BOOLEAN )
										.applyTo( worlds );
							}
						}
				}
				throw new AstPositionedException( position,
						new StaticVerificationException( "cannot apply '"
								+ operator + "' to '" + leftType
								+ "' and '" + rightType + "'" ) );
			}

			@Override
			public GroundDataType visit( AssignExpression n ) {
				GroundDataTypeOrVoid tvl = synth( n.target(), Collections.emptyList() );
				if( tvl.isVoid() ) {
					throw new AstPositionedException( n.position(),
							new StaticVerificationException(
									"expected assignable variable" ) );
				}
				GroundDataType tl = (GroundDataType) tvl;
				// the lefthand side of an assignment determines the worlds of the expression
				homeWorlds = tl.worldArguments();
				GroundDataTypeOrVoid tvr = synth( n.value() );
				if( tvr.isVoid() ) {
					throw new AstPositionedException( n.position(),
							new StaticVerificationException(
									"required type '" + tl + "', found '" + tvr + "'" ) );
				}
				GroundDataType tr = (GroundDataType) tvr;

				// operator of this expression is not "=", so it has an operation besides assigning
				if( n.operator().hasOperation() ) {
					tr = visitBinaryOp( n.operator().operation(), tl, tr, n.position() );
				}
				boolean assignable = opts.relaxed() ?
						tr.isAssignableTo_relaxed( tl ) :
						tr.isAssignableTo( tl );
				if( !assignable ) {
					throw new AstPositionedException( n.position(),
							new StaticVerificationException(
									"required type '" + tl + "', found '" + tr + "'" ) );
				}
				return annotate( n, tl );
			}

			@Override
			public GroundDataType visit( BinaryExpression n ) {
				GroundDataTypeOrVoid tl = synth( n.left() );
				// In "relaxed" typing mode, we let the left arguments determine where the
				// expression should be evaluated.
				if( homeWorlds.isEmpty() && !tl.isVoid() )
					homeWorlds = ((GroundDataType)tl).worldArguments();
				GroundDataTypeOrVoid tr = synth( n.right() );
				// the expression n, is annotated with a primitive type with world(s)
				// the type being the type of the entire binary expression
				return annotate( n, visitBinaryOp( n.operator(), tl, tr, n.position() ) );
			}

			@Override
			public GroundDataTypeOrVoid visit( EnclosedExpression n ) {
				return annotate( n, synth( n.nestedExpression() ) );
			}

			private boolean checkMemberAccess( Member m ) {
				return ( !leftStatic || m.isStatic() ) && m.isAccessibleFrom( scope.lookupThis() );
			}

			@Override
			public GroundDataType visit( FieldAccessExpression n ) {
				String identifier = n.name().identifier();
				Optional< ? extends GroundDataType > result = Optional.empty();
				if( left == null ) {
					result = scope.lookupVariable( identifier );
					if( result.isEmpty() ) {
						left = scope.lookupThis();
						leftStatic = isInStaticContext();
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
					recordDependencies(n, result.get(), homeWorlds);
					return annotate( n, result.get() );
				}
			}

			@Override
			public GroundDataType visit( StaticAccessExpression n ) {
				leftStatic = true;
				TypeExpression m = n.typeExpression();
				HigherReferenceType type = scope.assertLookupReferenceType( m.name().identifier() );
				// Check if all the worldarguments of the passed expression can be found in the
				// current scope. If any worldarguments can't be found, throw exception.
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
				Pair<List< ? extends HigherReferenceType >, List< ? extends GroundDataType >> typeargsArgs = getArgsTypeargs(scope, n);

				List< ? extends Member.GroundCallable > ms = findMostSpecificCallable(
						typeargsArgs.left(),
						typeargsArgs.right(),
						t.constructors().filter( this::checkMemberAccess )
				);
				if( ms.isEmpty() ) {
					throw new AstPositionedException( n.position(),
							new StaticVerificationException( "cannot resolve constructor '" + t +
									typeargsArgs.right().stream().map( Object::toString ).collect( Formatting
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

				recordDependencies(selected, n.arguments());

				leftStatic = false;
				return t;
			}

			@Override
			public GroundDataTypeOrVoid visit( MethodCallExpression n ) {
				if( left == null ) { // only happens for simple method calls (local)
					left = scope.lookupThis();
					leftStatic = isInStaticContext();
				}
				Pair<List< ? extends HigherReferenceType >, List< ? extends GroundDataType >> typeargsArgs = getArgsTypeargs(scope, n);

				if( left instanceof GroundReferenceType type) {
					List< ? extends Member.GroundCallable > ms = findMostSpecificCallable(
							typeargsArgs.left(),
							typeargsArgs.right(),
							type.methods( n.name().identifier() ).filter( this::checkMemberAccess )
					);
					if( ms.isEmpty() ) {
						throw new AstPositionedException( n.position(),
								new StaticVerificationException( "cannot resolve method '"
										+ n.name().identifier()
										+ typeargsArgs.right().stream().map( Object::toString ).collect( Formatting
										.joining( ",", "(", ")", "" ) )
										+ "' in '" + type + "'" ) );
					}
					else if( ms.size() > 1 ) {
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

					recordDependencies(selected, n.arguments());
					recordDependencies(n, selected.returnType(), homeWorlds);

					return annotate( n, selected.returnType());
				} else {
					throw new AstPositionedException( n.position(),
							new StaticVerificationException( "cannot resolve method '"
									+ n.name().identifier()
									+ typeargsArgs.right().stream().map( Object::toString ).collect( Formatting
									.joining( ",", "(", ")", "" ) )
									+ "' in 'void'" ) );
				}
			}

			/**
			 * Get the type arguments and regular arguments for a methodcallExpression or a
			 * ClassInstantiationExpression. Type arguments are stored in the left value, and
			 * regular arguments in the right value.
			 */
			public Pair<List< ? extends HigherReferenceType >, List< ? extends GroundDataType >>
				getArgsTypeargs(VariableDeclarationScope scope, InvocationExpression expression) {

				List< ? extends HigherReferenceType > typeArgs = expression.typeArguments().stream()
						.map( x -> visitHigherReferenceTypeExpression( scope, x, false ) )
						.collect( Collectors.toList() );
				List< ? extends GroundDataType > args = expression.arguments().stream()
						.map( x -> assertNotVoid(
								synth( x, Collections.emptyList() ),
								x.position() ) )
						.collect( Collectors.toList() );
				return new Pair<>( typeArgs, args );
			}


			/**
			 * Ensures the passed expression is a boolean.
			 */
			@Override
			public GroundDataType visit( NotExpression n ) {
				// t -> type ??
				GroundDataTypeOrVoid t = visit( n.expression() );
				// p -> primitive ??
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
				if( enclosingMethod.isStatic() ) {
					throw new AstPositionedException( n.position(),
							new StaticVerificationException(
									"non-static variable 'this' cannot be referenced from a static context" ) );
				}
				return annotate( n, scope.lookupThis() );
			}

			@Override
			public GroundDataType visit( SuperExpression n ) {
				if( explicitConstructorArg ) {
					throw new AstPositionedException( n.position(),
							new StaticVerificationException(
									"cannot reference 'super' before supertype constructor has been called" ) );

				}
				if( enclosingMethod.isStatic() ) {
					throw new AstPositionedException( n.position(),
							new StaticVerificationException(
									"non-static variable 'super' cannot be referenced from a static context" ) );
				}
				return annotate( n, scope.lookupSuper() );
			}

			@Override
			public GroundDataType visit( NullExpression n ) {
				return annotate( n, universe().nullType( visitWorlds( n.worlds() ) ) );
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
				return annotate( n, visitGroundDataTypeExpression( scope, n, false ) );
			}

			/**
			 * In the "relaxed" typing mode, check if an expression's location matches its
			 * expected location. If it doesn't, record the expression as a dependency.
			 * @param type The expression's type
			 * @param toWorlds The place where the expression's result should be located
			 */
			private void recordDependencies(
				Expression expression,
				GroundDataTypeOrVoid type,
				List< ? extends World > toWorlds
			){
				if (!opts.relaxed() || type.isVoid()) return;
				var foreignWorlds = ((GroundDataType) type).worldArguments();
				if( !toWorlds.containsAll( foreignWorlds ) ){
					enclosingMethod.addDependencies(toWorlds, expression, enclosingStatement);
				}
			}

			/**
			 * In the "relaxed" typing mode, check the locations of the arguments match the
			 * locations expected by the method. Any argument that doesn't match is recorded
			 * as a dependency for communication inference.
			 * @param method The method being invoked
			 * @param args The arguments passed to the method; we assume the arguments already
			 *             have type annotations
			 */
			private void recordDependencies(
				Member.GroundCallable method,
				List< Expression > args
			) {
				if ( !opts.relaxed() ) return;
				for( int i = 0; i < args.size(); i++ ){
					var toWorlds = getParamWorlds( method, i );
					var argument = args.get(i);
					var argType = argument.typeAnnotation().get();
					recordDependencies(argument, argType, toWorlds);
				}
			}

			/** Throws an exception if the literal isn't in the world we expect it to be in. */
			private <T extends LiteralExpression<?>> void checkWorlds( T n ){
				if ( !opts.relaxed() ) return;
				if( !homeWorlds.isEmpty() && !homeWorlds.contains( visitWorld( n.world() ) ) ){
					throw new AstPositionedException( n.position(),
							 new StaticVerificationException(
									 "Literal '" + n + "', can't be used in an expression at role '"
									 + homeWorlds + "'" ) );
				}
			}

			public List< ? extends World > visitWorlds( List< WorldArgument > n ) {
				return n.stream().map( this::visitWorld ).toList();
			}

			public World visitWorld( WorldArgument n ) {
				return annotate( n, scope.assertLookupWorldParameter( n.name() ) );
			}

		}
	}

}
