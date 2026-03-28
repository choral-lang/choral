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
			// inherited fields
			extendedClassesOrInterfaces().flatMap( GroundReferenceType::fields )
					.filter( x -> x.isAccessibleFrom( this )
							&& declaredFields().noneMatch(
							y -> x.identifier().equals( y.identifier() ) ) )
					.forEach( inheritedFields::add );
			// inherited methods (sec. 8.4.8)
			extendedClassesOrInterfaces().flatMap( GroundReferenceType::methods )
					.filter( x -> x.isAccessibleFrom( this ) )
					.forEach( methodToInherit -> {
						boolean inherited = true;     // true iff methodToInherit should be inherited
						boolean implemented = false;  // true iff a method in this class that implements methodToInherit
						for( Member.HigherMethod declaredMethod : declaredMethods ) {
							if( declaredMethod.isSubSignatureOf( methodToInherit ) ) {
								// If the parent method is a selection method, mark the child as a selection method too
								if( methodToInherit.isSelectionMethod() ) {
									declaredMethod.setSelectionMethod();
								}
								if (methodToInherit.isTypeSelectionMethod()) {
									declaredMethod.setTypeSelectionMethod();
								}

								// Now check that 'declaredMethod' satisfies all the requirements in JLS 8.4.8
								checkOverrideRequirementsOrThrow(declaredMethod, methodToInherit);

								implemented = !declaredMethod.isAbstract();
								if( declaredMethod.sameSignatureAsErasureOf( methodToInherit ) && !declaredMethod.sameSignatureAs( methodToInherit ) ) {
									inherited = true;
									for( Member.HigherMethod z : inheritedMethods ) {
										if( z.isSubSignatureOf( methodToInherit ) ) {
											// // TODO When does this happen?
											if( !z.isReturnTypeAssignable( methodToInherit ) ) {
												throw new StaticVerificationException(
														"method '" + z
																+ "' in '" + z.declarationContext()
																+ "' clashes with method '" + methodToInherit
																+ "' in '" + methodToInherit.declarationContext()
																+ "', attempting to use incompatible return type" );
											}
											inherited = false;
											break;
										}
									}
								}
								else {
									inherited = false;
								}
								break;
							}
							// TODO When does this happen?
							// (8.4.8.3) It's a compile-time error if:
							// 1. child's signature is not a subsignature of parent's signature; and
							// 2. child's signature has the same erasure as parent's signature.
							else if( methodToInherit.sameErasureAs( declaredMethod ) ) {
								throw new StaticVerificationException( "method '" + declaredMethod
										+ "' in '" + this + "' clashes with method '"
										+ methodToInherit + "' in '" + methodToInherit.declarationContext()
										+ "', both methods have the same erasure" );
							}
						}
						if( inherited ) {
							// TODO check implementation
							// bad variable name??
							boolean implementationRequirementSatisfied = false;
							if( !implemented && !isAbstract() && methodToInherit.isAbstract() ) {
								for(Member.HigherMethod inheritedMethod : inheritedMethods){
									if(!inheritedMethod.isAbstract() && inheritedMethod.isSubSignatureOf(methodToInherit)
										&& inheritedMethod.isReturnTypeAssignable(methodToInherit)){
										implementationRequirementSatisfied = true;
										break;
									}
								}
								if(!implementationRequirementSatisfied) {
									throw new StaticVerificationException( "'" + this + "' must either "
										+ "be declared as abstract or implement abstract method '"
										+ methodToInherit + "' in '" + methodToInherit.declarationContext() + "'" );
								}
							}
							boolean isDiamondDuplicate = false;
							// handle default methods with identical signature to existing inherited default method
							if(methodToInherit.isDefault()){
								for(Member.HigherMethod inheritedMethod : inheritedMethods){
									// sameSignatureOf method is bi-directional
									// methodToInherit.SameSignatureOf(inheritedMethod) == inheritedMethod.sameSignatureOf(methodToInherit)
									boolean sameSignature = methodToInherit.sameSignatureAs(inheritedMethod);
									
									// only throw exception if both is default. 
									if(inheritedMethod.isDefault() && sameSignature){
										GroundReferenceType xContext = methodToInherit.declarationContext();
										GroundReferenceType inheritedContext = inheritedMethod.declarationContext();
										
										// diamond path duplicate -> method is already present
										if(xContext.isEquivalentTo_relaxed(inheritedContext)){
											isDiamondDuplicate = true;
											break;
										}

										// Check if one methods defining interface is more specific than the others'
										boolean xPriority = xContext.isSubtypeOf_relaxed(inheritedContext);
										boolean inheritedPriotiy = inheritedContext.isSubtypeOf_relaxed(xContext);

										// If neither interface has priority, it means two completely separate interfaces 
										// defined identical default methods -> illegal. 
										if(!xPriority && !inheritedPriotiy){
											throw new StaticVerificationException("Duplicate default methods inherited. " +
											"'" + this + "' must override '" + methodToInherit + "'' from '" + methodToInherit.declarationContext() +
											"' which is identical to '" + inheritedMethod + "' from '" + 
											inheritedMethod.declarationContext() + "'");
										}

										// Defensive check, in case interface finalisation order ever changes. 
										assert (xPriority ? methodToInherit.isReturnTypeAssignable(inheritedMethod)
														: inheritedMethod.isReturnTypeAssignable(methodToInherit))
											: "Return type incompatibility was not caught. Error in finaliseInterface. " 
											+ " Return type compatibility assumption was made based on interface finalization order.";
									}
								}
							}
							if (!implementationRequirementSatisfied && !implemented && !isDiamondDuplicate){
								inheritedMethods.add( methodToInherit.copyFor( this ) );
							}
						}
					} );
			interfaceFinalised = true;
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
			if( !child.isReturnTypeAssignable(parent) ) {
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
