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

package choral.types;

import choral.ast.Node;
import choral.exceptions.StaticVerificationException;
import choral.types.kinds.Kind;
import choral.utils.Formatting;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static choral.types.Modifier.*;
import static choral.types.ModifierUtils.assertAccessModifiers;
import static choral.types.ModifierUtils.assertLegalModifiers;

public abstract class HigherClassOrInterface extends HigherReferenceType
		implements ClassOrInterface, TypeParameterDeclarationContext {

	public HigherClassOrInterface(
			Package declarationContext,
			EnumSet< Modifier > modifiers,
			String identifier,
			List< World > worldsParameters,
			List< HigherTypeParameter > typeParameters
	) {
		this( declarationContext, modifiers, identifier, worldsParameters, typeParameters, null,
				true );
	}

	HigherClassOrInterface(
			Package declarationContext,
			EnumSet< Modifier > modifiers,
			String identifier,
			List< World > worldsParameters,
			List< HigherTypeParameter > typeParameters,
			Node sourceCode
	) {
		this( declarationContext, modifiers, identifier, worldsParameters, typeParameters,
				sourceCode, true );
	}

	HigherClassOrInterface(
			Package declarationContext,
			EnumSet< Modifier > modifiers,
			String identifier,
			List< World > worldsParameters,
			List< HigherTypeParameter > typeParameters,
			Node sourceCode,
			boolean registerWithDeclarationContext
	) {
		super( declarationContext.universe(), worldsParameters );
		setSourceCode( sourceCode );
		this.declarationContext = declarationContext;
		this.identifier = identifier;
		if( registerWithDeclarationContext ) {
			this.declarationContext.registerDeclaredType( this );
		}
		this.typeParameters = new ArrayList<>( typeParameters );
		String[] names = new String[ typeParameters.size() ];
		int i = 0;
		for( HigherTypeParameter x : typeParameters ) {
			x.setDeclarationContext( this );
			for( int j = 0; j < i; j++ ) {
				if( names[ j ].equals( x.identifier() ) ) {
					throw StaticVerificationException.of(
							"duplicate parameter '" + names[ j ] + "'", x.sourceCode() );
				}
			}
			names[ i++ ] = x.identifier();
		}
		this.modifiers = EnumSet.copyOf( modifiers );
		assertModifiers( modifiers );
		this.tag = universe().registerSpecialType( this );
	}

	private final Universe.SpecialTypeTag tag;

	@Override
	public Universe.SpecialTypeTag specialTypeTag() {
		return tag;
	}

	private final EnumSet< Modifier > modifiers;

	protected void assertModifiers( EnumSet< Modifier > modifiers ) {
		assertLegalModifiers( legalOuterModifiers, modifiers, "for " + variety().labelPlural );
		assertAccessModifiers( modifiers );
		// for nested types only
		//   assertIllegalCombinationOfModifiers(modifiers,ABSTRACT,STATIC);
		//   assertIllegalCombinationOfModifiers(modifiers,ABSTRACT,PRIVATE);
		//   assertIllegalCombinationOfModifiers(modifiers,ABSTRACT,FINAL);
	}

	private static final EnumSet< Modifier > legalOuterModifiers = EnumSet.of( PUBLIC, ABSTRACT,
			FINAL );

	public final boolean isAbstract() {
		return modifiers.contains( ABSTRACT );
	}

	public final boolean isPublic() {
		return modifiers.contains( PUBLIC );
	}

	public final boolean isPrivate() {
		return modifiers.contains( PRIVATE );
	}

	public final boolean isProtected() {
		return modifiers.contains( PROTECTED );
	}

	public final boolean isPackagePrivate() {
		return !isPublic() && !isProtected() && !isPrivate();
	}

	public final boolean isFinal() {
		return modifiers.contains( FINAL );
	}

	public final boolean isStatic() {
		return modifiers.contains( STATIC );
	}

	private final Package declarationContext;

	public Package declarationContext() {
		return declarationContext;
	}

	@Override
	public Package declarationPackage() {
		return declarationContext;
	}

	private final String identifier;

	public String identifier() {
		return identifier( false );
	}

	public String identifier( boolean qualified ) {
		if( qualified && !declarationContext.isRoot() ) {
			return declarationContext().identifier( true ) + "." + identifier;
		} else {
			return this.identifier;
		}
	}

	@Override
	public String toString() {
		return identifier( true );
	}

	protected final ArrayList< HigherTypeParameter > typeParameters;

	@Override
	public List< ? extends HigherTypeParameter > typeParameters() {
		return Collections.unmodifiableList( typeParameters );
	}

	@Override
	public Optional< ? extends HigherTypeParameter > typeParameter( int index ) {
		if( 0 <= index && index < typeParameters.size() ) {
			return Optional.of( typeParameters.get( index ) );
		} else {
			return Optional.empty();
		}
	}

	@Override
	public Optional< ? extends HigherTypeParameter > typeParameter( String name ) {
		return typeParameters.stream().filter( x -> x.identifier().equals( name ) ).findAny();
	}

	@Override
	public GroundClassOrInterface applyTo( List< ? extends World > args ) {
		return applyTo( args, List.of() );
	}

	@Override
	public abstract GroundClassOrInterface applyTo(
			List< ? extends World > worldArgs, List< ? extends HigherReferenceType > typeArgs
	);


	@Override
	public HigherReferenceType partiallyApplyTo( List< ? extends HigherReferenceType > typeArgs ) {
		if( typeArgs.isEmpty() ) {
			return this;
		} else {
			// type lambda
			return new Lambda( typeArgs );
		}
	}

	private class Lambda extends HigherReferenceType {
		Lambda( List< ? extends HigherReferenceType > typeArgs ) {
			super( HigherClassOrInterface.this.universe(),
					World.freshWorlds( HigherClassOrInterface.this.universe(),
							HigherClassOrInterface.this.worldParameters.size(), "X" ) );
			this.typeArgs = typeArgs;
		}

		private final List< ? extends HigherReferenceType > typeArgs;

		@Override
		public String toString() {
			return //this.worldParameters.stream().map( World::toString ).collect(
					//Formatting.joining( ",", "@(", ")->(", "" ) )
					identifier( true )
							//+ this.worldParameters.stream().map( World::toString ).collect(
							//Formatting.joining( ",", "@(", ")", "" ) )
							+ this.typeArgs.stream().map( HigherReferenceType::toString ).collect(
							Formatting.joining( ",", "<", ">", "" ) )
					//+ ")"
					;
		}

		@Override
		public GroundReferenceType applyTo( List< ? extends World > args ) {
			return HigherClassOrInterface.this.applyTo( args, typeArgs );
		}

		@Override
		public boolean equals( Object obj ) {
			if( obj instanceof Lambda ) {
				Lambda other = (Lambda) obj;
				return this.isSameKind( other ) && this.applyTo(
						this.worldParameters ).isEquivalentTo(
						other.applyTo( this.worldParameters ) );
			}
			return false;
		}
	}

	@Override
	protected final Substitution getApplicationSubstitution( List< ? extends World > worldArgs ) {
		return getApplicationSubstitution( worldArgs, List.of() );
	}

	protected final Substitution getApplicationSubstitution(
			List< ? extends World > worldArgs, List< ? extends HigherReferenceType > typeArgs
	) {
		checkApplicationArguments( worldArgs, typeArgs );
		return new Substitution() {

			@Override
			public World get( World placeHolder ) {
				int i = worldParameters.indexOf( placeHolder );
				return ( i == -1 ) ? placeHolder : worldArgs.get( i );
			}

			@Override
			public HigherReferenceType get( HigherTypeParameter placeHolder ) {
				int i = typeParameters.indexOf( placeHolder );
				return ( i == -1 ) ? placeHolder : typeArgs.get( i );
			}
		};
	}

	protected final void checkApplicationArguments(
			List< ? extends World > worldArgs, List< ? extends HigherReferenceType > typeArgs
	) {
		super.checkApplicationArguments( worldArgs );
		if( typeArgs.size() != typeParameters.size() ) {
			throw new StaticVerificationException(
					"illegal type instantiation: expected " + typeParameters.size() + " type arguments but found " + typeArgs.size() );
		}
	}

	@Override
	public final Kind kind() {
		return Kind.getAtTower( worldParameters().size(),
				Kind.getTower(
						typeParameters.stream().map( HigherTypeParameter::kind ).collect(
								Collectors.toList() ),
						Kind.getStar() ) );
	}

	public abstract Definition innerType();

	public abstract class Definition extends HigherReferenceType.Definition
			implements GroundClassOrInterface {

		Definition() {
		}

		public final String toString() {
			return typeConstructor().toString() +
					worldArguments().stream().map( World::toString ).collect(
							Formatting.joining( ",", "@(", ")", "" ) ) +
					typeArguments().stream().map( HigherReferenceType::toString ).collect(
							Formatting.joining( ",", "<", ">", "" ) );
		}

		@Override
		public final List< ? extends HigherReferenceType > typeArguments() {
			return typeConstructor().typeParameters();
		}

		private boolean inheritanceFinalised = false;

		@Override
		public final boolean isInheritanceFinalised() {
			return inheritanceFinalised;
		}

		public void finaliseInheritance() {
			if( !isInheritanceFinalised() ) {
				allExtendedInterfaces = Stream.concat( extendedInterfaces(),
						extendedClassesOrInterfaces().flatMap(
								GroundClassOrInterface::allExtendedInterfaces ) )
						.collect( Collectors.toList() );
				extendedClassesOrInterfaces().flatMap(
						GroundClassOrInterface::allExtendedInterfaces )
						.forEach( x -> {
							extendedInterfaces().filter( y ->
									x.typeConstructor() == y.typeConstructor() &&
											x.worldArguments().equals( y.worldArguments() ) &&
											!x.typeArguments().equals( y.typeArguments() )
							).findAny().ifPresent( y -> {
										throw new StaticVerificationException(
												"illegal inheritance, cannot implement both '"
														+ y + "' and " + x + "'" );
									}
							);
						} );
				inheritanceFinalised = true;
			}
		}

		private final List< GroundInterface > extendedInterfaces = new ArrayList<>();

		private List< GroundInterface > allExtendedInterfaces;

		public void addExtendedInterface( GroundInterface type ) {
			assert ( !isInheritanceFinalised() );
			if( type.worldArguments().size() != worldArguments().size() ||
					!type.worldArguments().containsAll( worldParameters ) ) {
				throw new StaticVerificationException(
						"illegal inheritance, '" + type + "' and '" + this + "' must have the same roles" );
			}
			if( extendedInterfaces().anyMatch( x -> x.isEquivalentTo( type ) ) ) {
				throw new StaticVerificationException(
						"illegal inheritance, '" + type + "' is repeated" );
			}
			extendedInterfaces.add( type );
		}

		@Override
		public final Stream< GroundInterface > extendedInterfaces() {
			return extendedInterfaces.stream();
		}

		public final Stream< GroundInterface > allExtendedInterfaces() {
			if( allExtendedInterfaces == null ) {
				return Stream.concat( extendedInterfaces(), extendedClassesOrInterfaces().flatMap(
						GroundClassOrInterface::allExtendedInterfaces ) );
			} else {
				return allExtendedInterfaces.stream();
			}
		}

		@Override
		protected boolean isEquivalentTo( GroundDataType type ) {
			if( type == this ) {
				return true;
			} else if( type instanceof Proxy ) {
				Proxy other = (Proxy) type;
				return ( other.definition() == this ) &&
						worldArguments().equals( other.worldArguments() ) &&
						typeArguments().equals( other.typeArguments() );
			} else {
				return false;
			}
		}

		@Override
		protected boolean isSubtypeOf( GroundDataType type, boolean strict ) {
			return ( !strict && isEquivalentTo( type ) )
					|| extendedInterfaces().anyMatch( x -> x.isSubtypeOf( type, false ) )
					|| ( type.isEquivalentTo(
					universe().topReferenceType( worldArguments() ) ) );
		}

		private boolean interfaceFinalised = false;

		@Override
		public final boolean isInterfaceFinalised() {
			return interfaceFinalised;
		}

		public void finaliseInterface() {
			assert ( isInheritanceFinalised() && extendedClassesOrInterfaces()
					.allMatch( GroundReferenceType::isInterfaceFinalised ) );
			if( interfaceFinalised ) {
				return;
			}
			// inherited fields
			extendedClassesOrInterfaces().flatMap( GroundReferenceType::fields )
					.filter( x -> x.isAccessibleFrom( this )
							&& declaredFields().noneMatch(
							y -> x.identifier().equals( y.identifier() ) ) )
					.forEach( inheritedFields::add );
			// inherited methods (sec. 8.4.8)
			extendedClassesOrInterfaces().flatMap( GroundReferenceType::methods )
					.filter( x -> x.isAccessibleFrom( this ) )
					.forEach( x -> {
						boolean inherited = true;
						boolean implemented = false;
						for( Member.HigherMethod y : declaredMethods ) {
							boolean sameSignature = y.sameSignatureOf( x );
							boolean sameErasure = y.sameSignatureErasureOf( x );
							if( sameSignature || sameErasure ) {
								// check both instance or both static
								if( !y.isStatic() && x.isStatic() ) {
									throw new StaticVerificationException( "instance method '" + y
											+ "' in '" + this + "' cannot override static method '"
											+ x + "' in '" + x.declarationContext() + "'" );
								}
								if( y.isStatic() && !x.isStatic() ) {
									throw new StaticVerificationException( "static method '" + y
											+ "' in '" + this + "' cannot override instance method '"
											+ x + "' in '" + x.declarationContext() + "'" );
								}
								// check access privileges
								if( y.isPrivate() || ( x.isPublic() && !y.isPublic() )
										|| ( x.isProtected() && y.isPackagePrivate() ) ) {
									throw new StaticVerificationException( "method '" + y
											+ "' in '" + this + "' clashes with method '"
											+ x + "' in '" + x.declarationContext()
											+ "', attempting to assign weaker access privileges '"
											+ ModifierUtils.prettyAccess( y.modifiers() ) + "' to '"
											+ ModifierUtils.prettyAccess( x.modifiers() ) + "'" );
								}
								// check not final
								if( x.isFinal() ) {
									throw new StaticVerificationException( "method '" + y
											+ "' in '" + this + "' cannot override final method '"
											+ x + "' in '" + x.declarationContext() + "'" );
								}
								// check assignable return type;
								if( !y.isReturnTypeAssignable( x ) ) {
									throw new StaticVerificationException( "method '" + y
											+ "' in '" + this + "' clashes with method '"
											+ x + "' in '" + x.declarationContext()
											+ "', attempting to use incompatible return type" );
								}
								// inherit selection annotation
								if( x.isSelectionMethod() && sameSignature ) {
									y.setSelectionMethod();
								}
								implemented = !y.isAbstract();
								inherited = sameErasure && !sameSignature;
								if( inherited ) {
									for( Member.HigherMethod z : inheritedMethods ) {
										if( z.isSubSignatureOf( x ) ) {
											// check assignable return type;
											if( !z.isReturnTypeAssignable( x ) ) {
												throw new StaticVerificationException(
														"method '" + z
																+ "' in '" + z.declarationContext()
																+ "' clashes with method '" + x
																+ "' in '" + x.declarationContext()
																+ "', attempting to use incompatible return type" );
											}
											inherited = false;
											break;
										}
									}
								}
								break;
							} else {
								if( x.sameErasureAs( y ) ) {
									throw new StaticVerificationException( "method '" + y
											+ "' in '" + this + "' clashes with method '"
											+ x + "' in '" + x.declarationContext()
											+ "', both methods have the same erasure" );
								}
							}
						}
						if( inherited ) {
							// check implementation
							if( !implemented && !isAbstract() && x.isAbstract() ) {
								throw new StaticVerificationException( "'" + this + "' must either "
										+ "be declared as abstract or implement abstract method '"
										+ x + "' in '" + x.declarationContext() + "'" );
							}
							inheritedMethods.add( x.copyFor( this ) );
						}
					} );
			interfaceFinalised = true;
		}


		protected final List< Member.Field > inheritedFields = new LinkedList<>();

		protected final List< Member.HigherMethod > inheritedMethods = new LinkedList<>();

		protected final List< Member.Field > declaredFields = new ArrayList<>();

		protected final List< Member.HigherMethod > declaredMethods = new ArrayList<>();

		@Override
		public final Stream< Member.Field > declaredFields() {
			return declaredFields.stream();
		}

		public void addField( Member.Field field ) {
			assert ( !interfaceFinalised );
			assert ( field.declarationContext() == this );
			if( declaredFields().anyMatch( x -> x.identifier().equals( field.identifier() ) ) ) {
				throw new StaticVerificationException(
						"duplicate variable '" + field.identifier() + "' in "
								+ typeConstructor().variety().labelSingular + " '" + typeConstructor() );
			}
			declaredFields.add( field );
		}

		public void addMethod( Member.HigherMethod method ) {
			assert ( !interfaceFinalised );
			assert ( method.declarationContext() == this );
			for( Member.HigherMethod x : declaredMethods ) {
				if( x.sameErasureAs( method ) ) {
					if( x.sameSignatureOf( method ) ) {
						throw new StaticVerificationException( "method '" + method
								+ "' is already defined in '" + typeConstructor() + "'" );
					} else {
						throw new StaticVerificationException( "method '" + method
								+ "' clashes with '" + x
								+ "', both methods have the same erasure" );
					}
				}
			}
			declaredMethods.add( method );
		}

		@Override
		public final Stream< Member.HigherMethod > declaredMethods() {
			return declaredMethods.stream();
		}
	}

	protected abstract class Proxy extends HigherReferenceType.Proxy
			implements GroundClassOrInterface {

		Proxy( Substitution substitution ) {
			super( substitution );
			instantiationChecked = typeConstructor().typeParameters().isEmpty();
		}

		public final String toString() {
			return typeConstructor().toString() +
					worldArguments().stream().map( World::toString ).collect(
							Formatting.joining( ",", "@(", ")", "" ) ) +
					typeArguments().stream().map( HigherReferenceType::toString ).collect(
							Formatting.joining( ",", "<", ">", "" ) );
		}

		public final List< ? extends HigherReferenceType > typeArguments() {
			return typeConstructor().typeParameters().stream().map( substitution()::get ).collect(
					Collectors.toList() );
		}

		@Override
		protected abstract Definition definition();

		private boolean instantiationChecked;

		@Override
		public boolean isInstantiationChecked() {
			return instantiationChecked;
		}

		@Override
		public void checkInstantiation() {
			if( !instantiationChecked ) {
				for( HigherTypeParameter t : typeConstructor().typeParameters ) {
					t.assertWithinBounds( substitution() );
				}
				instantiationChecked = true;
			}
		}

		@Override
		public final boolean isInheritanceFinalised() {
			return definition().isInheritanceFinalised();
		}

		@Override
		public final Stream< GroundInterface > extendedInterfaces() {
			return definition().extendedInterfaces().map(
					x -> x.applySubstitution( substitution() ) );
		}

		@Override
		public final Stream< GroundInterface > allExtendedInterfaces() {
			return definition().allExtendedInterfaces().map(
					x -> x.applySubstitution( substitution() ) );
		}

		@Override
		protected boolean isEquivalentTo( GroundDataType type ) {
			if( type instanceof Definition ) {
				return type.isEquivalentTo( this );
			} else if( type instanceof Proxy ) {
				Proxy other = (Proxy) type;
				return ( this.definition() == other.definition() ) &&
						worldArguments().equals( other.worldArguments() ) &&
						typeArguments().equals( other.typeArguments() );
			} else {
				return false;
			}
		}

		@Override
		protected boolean isSubtypeOf( GroundDataType type, boolean strict ) {
			return ( !strict && isEquivalentTo( type ) )
					|| extendedInterfaces().anyMatch( x -> x.isSubtypeOf( type, false ) )
					|| ( type.isEquivalentTo(
					universe().topReferenceType( worldArguments() ) ) );
		}

		@Override
		public final boolean isInterfaceFinalised() {
			return definition().isInterfaceFinalised();
		}

		public final Stream< Member.Field > declaredFields() {
			return definition().declaredFields().map( x -> x.applySubstitution( substitution() ) );
		}

		public final Stream< Member.HigherMethod > declaredMethods() {
			return definition().declaredMethods().map( x -> x.applySubstitution( substitution() ) );
		}
	}

}

