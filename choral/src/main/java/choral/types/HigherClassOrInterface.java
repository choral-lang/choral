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

/** @see HigherDataType */
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

	/**
	 * Returns the GroundClass or GroundInterface that serves as this type's definition.
	 * @see HigherDataType.Proxy
	 */
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
						.forEach( x -> extendedInterfaces().filter( y ->
								x.typeConstructor() == y.typeConstructor() &&
										x.worldArguments().equals( y.worldArguments() ) &&
										!x.typeArguments().equals( y.typeArguments() )
						).findAny().ifPresent( y -> {
									throw new StaticVerificationException(
											"illegal inheritance, cannot implement both '"
													+ y + "' and " + x + "'" );
								}
						) );
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
		protected boolean isEquivalentTo_relaxed( GroundDataType type ) {
			if( type == this ) {
				return true;
			} else if( type instanceof Proxy ) {
				Proxy other = (Proxy) type;
				if (other.definition() != this)
					return false;
				if (typeArguments().size() != other.typeArguments().size())
					return false;
				for( int i = 0; i < typeArguments().size(); i++ ){
					if ( !typeArguments().get(i).isEquivalentTo_relaxed( other.typeArguments().get(i) ) )
						return false;
				}
				return true;
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
		protected boolean isSubtypeOf_relaxed( GroundDataType type, boolean strict ) {
			return ( !strict && isEquivalentTo_relaxed( type ) )
					|| extendedInterfaces().anyMatch( x -> x.isSubtypeOf_relaxed( type, false ) )
					|| ( type.isEquivalentTo_relaxed(
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
			inheritFields();
			inheritMethods();
			interfaceFinalised = true;
		}

		/**
		 * (JLS 8.3) Inherit fields from direct superclasses and superinterfaces.
		 * A field is inherited if it is accessible from this type and not hidden
		 * by a field declared in this type with the same name.
		 */
		private void inheritFields() {
			extendedClassesOrInterfaces().flatMap( GroundReferenceType::fields )
					.filter( x -> x.isAccessibleFrom( this ) &&
							declaredFields().noneMatch( y -> x.identifier().equals( y.identifier() ) ) )
					.forEach( inheritedFields::add );
		}

		/**
		 * Collect inherited methods, applying JLS rules for overriding,
		 * override-equivalent resolution, return-type compatibility, and abstract
		 * obligations (JLS 8.4.8 for classes, 9.4.1 for interfaces).
		 */
		private void inheritMethods() {
			List< Member.HigherMethod > candidates = new ArrayList<>();
			Set< Member.HigherMethod > implementedByDeclared = new HashSet<>();

			if( this instanceof GroundClass gc ) {
				// === CLASS INHERITANCE (JLS 8.4.8) ===

				// Phase 1a: Inherit concrete methods from direct superclass
				gc.extendedClass().ifPresent( superclass ->
						superclass.methods()
								.filter( m -> !m.isAbstract() && m.isAccessibleFrom( this ) )
								.forEach( m -> collectCandidate(
										m, candidates, implementedByDeclared ) )
				);

				List< Member.HigherMethod > concreteSuperclassMethods =
						new ArrayList<>( candidates );

				// (JLS 8.4.8 (d)) An abstract supertype method is not inherited if a
				// concrete superclass method already has a subsignature.
				gc.extendedClass().ifPresent( superclass ->
						superclass.methods()
								.filter( m -> m.isAbstract() && m.isAccessibleFrom( this ) )
								.forEach( m -> collectCandidateIfNotSatisfied(
										m, candidates, concreteSuperclassMethods,
										implementedByDeclared ) )
				);

				// (JLS 9.4.1.1) Static interface methods are not inherited.
				extendedInterfaces()
						.flatMap( GroundReferenceType::methods )
						.filter( m -> !m.isStatic() && m.isAccessibleFrom( this ) )
						.forEach( m -> collectCandidateIfNotSatisfied(
								m, candidates, concreteSuperclassMethods,
								implementedByDeclared ) );

			} else {
				// (JLS 9.4.1.1) Static interface methods are not inherited.
				extendedInterfaces()
						.flatMap( GroundReferenceType::methods )
						.filter( m -> !m.isStatic() && m.isAccessibleFrom( this ) )
						.forEach( m -> collectCandidate(
								m, candidates, implementedByDeclared ) );
			}

			resolveOverrideEquivalentGroups( candidates );
			checkInheritedMethodCompatibility( candidates, implementedByDeclared );

			if( this instanceof GroundClass gc && !isAbstract() ) {
				checkInaccessibleAbstractObligations( gc );
			}

			for( Member.HigherMethod m : candidates ) {
				inheritedMethods.add( m.copyFor( this ) );
			}
		}

		/**
		 * Add {@code methodToInherit} as a candidate unless a declared method overrides it
		 * (JLS 8.4.8.1–.3). Records an entry in {@code implementedByDeclared} when the
		 * overriding declared method is concrete. Erasure-only overrides keep the parent
		 * around for bridge generation.
		 */
		private void collectCandidate(
				Member.HigherMethod methodToInherit,
				List< Member.HigherMethod > candidates,
				Set< Member.HigherMethod > implementedByDeclared
		) {
			for( Member.HigherMethod declaredMethod : declaredMethods ) {
				if( declaredMethod.isSubSignatureOf( methodToInherit ) ) {
					propagateSelectionFlags( declaredMethod, methodToInherit );
					checkOverrideRequirementsOrThrow( declaredMethod, methodToInherit );

					if( !declaredMethod.isAbstract() ) {
						implementedByDeclared.add( methodToInherit );
					}

					// (JLS 8.4.8.3) Erasure-only override: declared method matches erasure
					// but not full signature. The parent method may still need to be inherited
					// for bridge method purposes.
					if( declaredMethod.sameSignatureAsErasureOf( methodToInherit )
							&& !declaredMethod.sameSignatureAs( methodToInherit )
							&& !isAlreadyCoveredByCandidate( methodToInherit, candidates ) ) {
						candidates.add( methodToInherit );
					}
					return;
				}
				if( methodToInherit.sameErasureAs( declaredMethod ) ) {
					// (JLS 8.4.8.3) Same erasure but not a subsignature.
					throw new StaticVerificationException( "method '" + declaredMethod
							+ "' in '" + this + "' clashes with method '"
							+ methodToInherit + "' in '" + methodToInherit.declarationContext()
							+ "', both methods have the same erasure" );
				}
			}

			candidates.add( methodToInherit );
		}

		/**
		 * (JLS 8.4.8 (d)) Variant of {@link #collectCandidate} that skips parent methods
		 * already satisfied by a concrete superclass method.
		 */
		private void collectCandidateIfNotSatisfied(
				Member.HigherMethod methodToInherit,
				List< Member.HigherMethod > candidates,
				List< Member.HigherMethod > concreteSuperclassMethods,
				Set< Member.HigherMethod > implementedByDeclared
		) {
			for( Member.HigherMethod c : concreteSuperclassMethods ) {
				if( c.isSubSignatureOf( methodToInherit ) ) {
					if( !c.isReturnTypeSubstitutableFor( methodToInherit ) ) {
						throw new StaticVerificationException(
								"method '" + c
										+ "' in '" + c.declarationContext()
										+ "' clashes with method '" + methodToInherit
										+ "' in '" + methodToInherit.declarationContext()
										+ "', attempting to use incompatible return type" );
					}
					return;
				}
			}
			collectCandidate( methodToInherit, candidates, implementedByDeclared );
		}

		/**
		 * Checks whether an already-collected candidate covers a method that needs
		 * inheritance only for bridge purposes (erasure-only override).
		 */
		private boolean isAlreadyCoveredByCandidate(
				Member.HigherMethod methodToInherit,
				List< Member.HigherMethod > candidates
		) {
			for( Member.HigherMethod z : candidates ) {
				if( z.isSubSignatureOf( methodToInherit ) ) {
					if( !z.isReturnTypeSubstitutableFor( methodToInherit ) ) {
						throw new StaticVerificationException(
								"method '" + z
										+ "' in '" + z.declarationContext()
										+ "' clashes with method '" + methodToInherit
										+ "' in '" + methodToInherit.declarationContext()
										+ "', attempting to use incompatible return type" );
					}
					return true;
				}
			}
			return false;
		}

		private void propagateSelectionFlags(
				Member.HigherMethod declaredMethod, Member.HigherMethod parentMethod
		) {
			if( parentMethod.isSelectionMethod() ) {
				declaredMethod.setSelectionMethod();
			}
			if( parentMethod.isTypeSelectionMethod() ) {
				declaredMethod.setTypeSelectionMethod();
			}
		}

		/**
		 * Resolve override-equivalent pairs in {@code candidates}, removing duplicates and
		 * losers by specificity, diamond identity, or default-method resolution. Throws on
		 * unresolvable conflicts (JLS 8.4.8.4 / 9.4.1.3). Modifies {@code candidates} in place.
		 */
		private void resolveOverrideEquivalentGroups(
				List< Member.HigherMethod > candidates
		) {
			// Backward iteration so removals don't shift unprocessed indices.
			for( int i = candidates.size() - 1; i >= 0; i-- ) {
				Member.HigherMethod m = candidates.get( i );
				for( int j = 0; j < i; j++ ) {
					Member.HigherMethod earlier = candidates.get( j );
					if( !m.sameSignatureAs( earlier ) ) {
						continue;
					}

					// Two override-equivalent inherited methods found.
					// Determine which to keep, or error on conflict.
					int resolution = resolveOverrideEquivalentPair( m, earlier );
					if( resolution < 0 ) {
						// Remove m (earlier wins or it's a duplicate)
						candidates.remove( i );
						break;
					} else if( resolution > 0 ) {
						// Remove earlier (m wins)
						candidates.remove( j );
						i--; // adjust index since we removed before i
						break;
					}
					// resolution == 0: both are abstract, keep both
				}
			}
		}

		/**
		 * Resolves a pair of override-equivalent inherited methods.
		 *
		 * @return negative if {@code m} should be removed (earlier wins),
		 *         positive if {@code earlier} should be removed (m wins),
		 *         zero if both should be kept (e.g., both abstract).
		 * @throws StaticVerificationException on unresolvable conflict
		 */
		private int resolveOverrideEquivalentPair(
				Member.HigherMethod m, Member.HigherMethod earlier
		) {
			GroundReferenceType mContext = m.declarationContext();
			GroundReferenceType earlierContext = earlier.declarationContext();

			// (JLS 8.4.8, 9.4.1) The more specific declarer wins.
			if( mContext.isSubtypeOf_relaxed( earlierContext ) ) {
				return 1;
			}
			if( earlierContext.isSubtypeOf_relaxed( mContext ) ) {
				return -1;
			}

			// (JLS 8.4.8.4, 9.4.1.3) Diamond: same default method via two paths.
			if( m.isDefault() && earlier.isDefault()
					&& shareCommonDefaultOrigin( m, mContext, earlierContext ) ) {
				return -1;
			}

			// JLS 8.4.8.4: "It is a compile-time error if a class C inherits a default
			// method whose signature is override-equivalent with another method inherited
			// by C, unless there exists an abstract method declared in a superclass of C
			// and inherited by C that is override-equivalent with the two methods."
			//
			// JLS 9.4.1.3: "It is a compile-time error if an interface I inherits a
			// default method whose signature is override-equivalent with another method
			// inherited by I."
			if( m.isDefault() || earlier.isDefault() ) {
				// For classes, check whether the superclass-abstract exception applies
				if( !isInterface() && hasSuperclassAbstractOverrideEquivalent( m ) ) {
					// The superclass abstract method neutralizes the conflict.
					// Keep both — Phase 4 will verify return-type compatibility.
					return 0;
				}
				throw new StaticVerificationException(
						"Duplicate default methods inherited. "
								+ "'" + this + "' must override '" + m
								+ "'' from '" + m.declarationContext()
								+ "' which is identical to '" + earlier
								+ "' from '" + earlier.declarationContext() + "'" );
			}

			return 0;
		}

		/**
		 * Checks whether there exists an abstract method declared
		 * in a superclass of this class (and inherited by this class) that is
		 * override-equivalent with the given method. This only applies to classes (not interfaces).
		 */
		private boolean hasSuperclassAbstractOverrideEquivalent( Member.HigherMethod method ) {
			if( !( this instanceof GroundClass gc ) ) {
				return false;
			}
			return gc.extendedClass()
					.map( superclass -> superclass.methods()
							.anyMatch( sm -> sm.isAbstract() && sm.isSubSignatureOf( method ) ) )
					.orElse( false );
		}

		/**
		 * (JLS 8.4.8.4 / 9.4.1.3) Among override-equivalent inherited methods, one must be
		 * return-type-substitutable for every other. (JLS 8.1.5 / 8.4.3.1) A concrete class
		 * must implement every inherited abstract method.
		 */
		private void checkInheritedMethodCompatibility(
				List< Member.HigherMethod > candidates,
				Set< Member.HigherMethod > implementedByDeclared
		) {
			for( int i = 0; i < candidates.size(); i++ ) {
				Member.HigherMethod m = candidates.get( i );

				// Check return-type compatibility against all earlier override-equivalent
				// methods in the candidate list.
				// JLS 8.4.8.4: "one of the inherited methods must be return-type-substitutable
				// for every other inherited method; otherwise, a compile-time error occurs."
				// JLS 9.4.1.3: same rule for interfaces.
				for( int j = 0; j < i; j++ ) {
					Member.HigherMethod earlier = candidates.get( j );
					if( !m.sameSignatureAs( earlier ) ) {
						continue;
					}
					if( !m.isReturnTypeSubstitutableFor( earlier )
							&& !earlier.isReturnTypeSubstitutableFor( m ) ) {
						throw new StaticVerificationException(
								"method '" + m + "' in '" + m.declarationContext()
										+ "' clashes with method '" + earlier
										+ "' in '" + earlier.declarationContext()
										+ "', attempting to use incompatible return type" );
					}
				}

				// Check abstract implementation obligations.
				// JLS 8.1.5: "A non-abstract class must implement all abstract methods
				// from its superinterfaces."
				// JLS 8.4.3.1: "Every non-abstract subclass of an abstract class must
				// implement its abstract methods."
				if( !isAbstract() && m.isAbstract() && !implementedByDeclared.contains( m ) ) {
					checkAbstractImplementation( m, candidates );
				}
			}
		}

		/**
		 * For a concrete class inheriting an abstract method, checks whether an
		 * already-inherited concrete method satisfies the implementation requirement.
		 * Throws if no implementation is found.
		 */
		private void checkAbstractImplementation(
				Member.HigherMethod abstractMethod,
				List< Member.HigherMethod > candidates
		) {
			for( Member.HigherMethod candidate : candidates ) {
				if( candidate != abstractMethod
						&& !candidate.isAbstract()
						&& candidate.isSubSignatureOf( abstractMethod )
						&& candidate.isReturnTypeSubstitutableFor( abstractMethod ) ) {
					return;
				}
			}
			throw new StaticVerificationException( "'" + this + "' must either "
					+ "be declared as abstract or implement abstract method '"
					+ abstractMethod + "' in '" + abstractMethod.declarationContext() + "'" );
		}

		/**
		 * (JLS 8.1.5 / 8.4.3.1) An inaccessible abstract superclass method can never be
		 * overridden (JLS 8.4.8.1 requires accessibility), so any such method is always an
		 * error for a concrete subclass.
		 */
		private void checkInaccessibleAbstractObligations( GroundClass gc ) {
			gc.extendedClass().ifPresent( superclass ->
					superclass.methods()
							.filter( m -> m.isAbstract() && !m.isAccessibleFrom( this ) )
							.forEach( m -> {
								throw new StaticVerificationException(
										"Implementation is not abstract and does not"
												+ " override abstract method '"
												+ m + "' in '"
												+ m.declarationContext() + "'" );
							} )
			);
		}

		/**
		 * True if a common ancestor interface of {@code context1} and {@code context2}
		 * declares a default method with {@code method}'s signature (diamond inheritance).
		 */
		private boolean shareCommonDefaultOrigin(
				Member.HigherMethod method,
				GroundReferenceType context1,
				GroundReferenceType context2
		) {
			if( !( context1 instanceof GroundClassOrInterface c1 )
					|| !( context2 instanceof GroundClassOrInterface c2 ) ) {
				return false;
			}
			Set< HigherClassOrInterface > context2Supers = new HashSet<>();
			context2Supers.add( c2.typeConstructor() );
			c2.allExtendedInterfaces().forEach( i -> context2Supers.add( i.typeConstructor() ) );
			// Check if any super-interface of context1 is also a super-interface of context2
			// and declares a default method with the same signature
			return c1.allExtendedInterfaces().anyMatch( i -> {
				if( !context2Supers.contains( i.typeConstructor() ) ) {
					return false;
				}
				// Check if this common ancestor declares the method
				return i.typeConstructor().innerType().declaredMethods()
						.anyMatch( m -> m.isDefault() && m.sameSignatureAs( method ) );
			} );
		}

		private void checkOverrideRequirementsOrThrow(Member.HigherMethod child, Member.HigherMethod parent) {
			// (8.4.3.3) Ensure we're not overriding a final method
			if( parent.isFinal() ) {
				throw new StaticVerificationException( "method '" + child
						+ "' in '" + this + "' cannot override final method '"
						+ parent + "' in '" + parent.declarationContext() + "'" );
			}
			// (8.4.8.1) Ensure instance methods don't override static methods
			if( !child.isStatic() && parent.isStatic() ) {
				throw new StaticVerificationException( "instance method '" + child
						+ "' in '" + this + "' cannot override static method '"
						+ parent + "' in '" + parent.declarationContext() + "'" );
			}
			// (8.4.8.2) Ensure static methods don't hide instance methods
			if( child.isStatic() && !parent.isStatic() ) {
				throw new StaticVerificationException( "static method '" + child
						+ "' in '" + this + "' cannot override instance method '"
						+ parent + "' in '" + parent.declarationContext() + "'" );
			}
			// (8.4.8.3) Ensure method return types are covariant
			if( !child.isReturnTypeSubstitutableFor(parent) ) {
				throw new StaticVerificationException( "method '" + child
						+ "' in '" + this + "' clashes with method '"
						+ parent + "' in '" + parent.declarationContext()
						+ "', attempting to use incompatible return type" );
			}

			// (8.4.8.3) JLS says we should issue a warning if child is not a subtype of parent; we skip that check.
			// (8.4.8.3) Choral doesn't have checked exceptions yet, so we skip those checks.

			// (8.4.8.3) Ensure the access modifiers are compatible
			if( child.isPrivate() || ( parent.isPublic() && !child.isPublic() )
					|| ( parent.isProtected() && child.isPackagePrivate() ) ) {
				throw new StaticVerificationException( "method '" + child
						+ "' in '" + this + "' clashes with method '"
						+ parent + "' in '" + parent.declarationContext()
						+ "', attempting to assign weaker access privileges '"
						+ ModifierUtils.prettyAccess( child.modifiers() ) + "' to '"
						+ ModifierUtils.prettyAccess( parent.modifiers() ) + "'" );
			}
		}


		protected final List< Member.Field > inheritedFields = new LinkedList<>();

		protected final List< Member.HigherMethod > inheritedMethods = new LinkedList<>();

		protected final List< Member.Field > declaredFields = new ArrayList<>();

		protected final List< Member.HigherMethod > declaredMethods = new ArrayList<>();

		@Override
		public final Stream< Member.Field > declaredFields() {
			return declaredFields.stream();
		}

		@Override
		public Stream< ? extends Member.Field > fields() {
			return Stream.concat( declaredFields(), inheritedFields.stream() );
		}

		@Override
		public Stream< ? extends Member.HigherMethod > methods() {
			return Stream.concat( declaredMethods(), inheritedMethods.stream() );
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
					if( x.sameSignatureAs( method ) ) {
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

	/** @see HigherDataType.Proxy */
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
		protected boolean isEquivalentTo_relaxed( GroundDataType type ) {
			if( type instanceof Definition ) {
				return type.isEquivalentTo_relaxed( this );
			} else if( type instanceof Proxy ) {
				Proxy other = (Proxy) type;
				if (this.definition() != other.definition())
					return false;
				if (typeArguments().size() != other.typeArguments().size()) 
					return false;
				for( int i = 0; i < typeArguments().size(); i++ ){
					if ( !typeArguments().get(i).isEquivalentTo_relaxed( other.typeArguments().get(i) ) )
						return false;
				}
				return true;
						
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
		protected boolean isSubtypeOf_relaxed( GroundDataType type, boolean strict ) {
			return ( !strict && isEquivalentTo_relaxed( type ) )
					|| extendedInterfaces().anyMatch( x -> x.isSubtypeOf_relaxed( type, false ) )
					|| ( type.isEquivalentTo_relaxed(
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
