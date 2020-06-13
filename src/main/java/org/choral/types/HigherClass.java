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

package org.choral.types;

import org.choral.ast.Node;
import org.choral.exceptions.StaticVerificationException;
import org.choral.types.Member.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.choral.types.Modifier.*;

public class HigherClass extends HigherClassOrInterface implements Class {

	public HigherClass(
			Package declarationContext,
			EnumSet< Modifier > modifiers,
			String identifier,
			List< World > worldsParameters,
			List< HigherTypeParameter > typeParameters
	) {
		super( declarationContext, modifiers, identifier, worldsParameters, typeParameters );
	}

	public HigherClass(
			Package declarationContext,
			EnumSet< Modifier > modifiers,
			String identifier,
			List< World > worldsParameters,
			List< HigherTypeParameter > typeParameters,
			Node sourceCode
	) {
		super( declarationContext, modifiers, identifier, worldsParameters, typeParameters,
				sourceCode );
	}

	HigherClass(
			Package declarationContext,
			EnumSet< Modifier > modifiers,
			String identifier,
			List< World > worldsParameters,
			List< HigherTypeParameter > typeParameters,
			boolean registerWithDeclarationContext
	) {
		super( declarationContext, modifiers, identifier, worldsParameters, typeParameters, null,
				registerWithDeclarationContext );
	}

	@Override
	public Variety variety() {
		return Variety.CLASS;
	}

	@Override
	public GroundClass applyTo( List< ? extends World > args ) {
		return applyTo( args,
				typeParameters.stream().map( HigherTypeParameter::getRawType ).collect(
						Collectors.toList() ) );
	}

	@Override
	public GroundClass applyTo(
			List< ? extends World > worldArgs, List< ? extends HigherReferenceType > typeArgs
	) {
		return innerType().applySubstitution( getApplicationSubstitution( worldArgs, typeArgs ) );
	}

	private final Definition innerType = new Definition();

	@Override
	public Definition innerType() {
		return innerType;
	}

	public class Definition extends HigherClassOrInterface.Definition implements GroundClass {

		@Override
		public HigherClass typeConstructor() {
			return HigherClass.this;
		}

		private final HashMap< Substitution, GroundClass > alphaIndex = new HashMap<>();

		@Override
		public GroundClass applySubstitution( Substitution substitution ) {
			GroundClass result = alphaIndex.get( substitution );
			if( result == null ) {
				result = new Proxy( substitution );
				alphaIndex.put( substitution, result );
			}
			return result;
		}

		protected GroundClass extendedClass = null;

		public void setExtendedClass() {
			HigherClass c = universe().topReferenceType( worldArguments().size() );
			if( c != typeConstructor() ) {
				setExtendedClass( c.applyTo( worldArguments() ) );
			} // else no extended class for Object and Any
		}

		public final void setExtendedClass( GroundClass type ) {
			if( type.typeConstructor().isFinal() ) {
				throw new StaticVerificationException(
						"illegal inheritance, cannot inherit from final '" + type + "'" );
			}
			if( type.typeConstructor() == universe().specialType( Universe.SpecialTypeTag.ENUM )
					&& variety() != Variety.ENUM ) {
				throw new StaticVerificationException(
						"illegal inheritance, only enum types can inherit from '" + universe().specialType(
								Universe.SpecialTypeTag.ENUM ) + "'" );
			}
			if( type.worldArguments().size() != worldArguments().size() ||
					!type.worldArguments().containsAll( worldParameters ) ) {
				throw new StaticVerificationException(
						"illegal inheritance, '" + type + "' and '" + this + "' must have the same roles" );
			}
			extendedClass = type;
		}

		@Override
		public final Optional< ? extends GroundClass > extendedClass() {
			return Optional.ofNullable( extendedClass );
		}

		@Override
		public final Stream< ? extends GroundClassOrInterface > extendedClassesOrInterfaces() {
			if( extendedClass == null ) {
				return super.extendedInterfaces();
			} else {
				return Stream.concat( Stream.of( extendedClass ), super.extendedInterfaces() );
			}
		}

		@Override
		protected boolean isSubtypeOf( GroundDataType type, boolean strict ) {
			return ( !strict && isEquivalentTo( type ) )
					|| ( extendedClass().isPresent() && extendedClass().get().isSubtypeOf( type,
					false ) )
					|| extendedInterfaces().anyMatch( x -> x.isSubtypeOf( type, false ) );
		}

		@Override
		public void finaliseInterface() {
			// default empty constructor
			if( constructors.isEmpty() && ( extendedClass == null // top class
					|| extendedClass.constructors().filter( x -> x.isAccessibleFrom( this ) )
					.anyMatch( x -> x.typeParameters().size() == 0 && x.arity() == 0 ) )
			) {
				Member.HigherConstructor c = new Member.HigherConstructor(
						this,
						EnumSet.of( PUBLIC ),
						List.of()
				);
				c.innerCallable().finalise();
				addConstructor( c );
			}
			// inherit fields and methods
			if( extendedClass != null ) {
				extendedClass.fields().filter( x -> x.isAccessibleFrom( this )
						&& declaredFields().noneMatch(
						y -> x.identifier().equals( y.identifier() ) ) )
						.forEach( inheritedFields::add );
				extendedClass.methods().filter( x -> x.isAccessibleFrom( this ) ).forEach( x -> {
					boolean inherited = true;
					for( HigherMethod y : declaredMethods ) {
						// y overrides x
						if( y.isOverrideEquivalent( x ) ) {
							// check both instance or both static
							if( !y.isStatic() && x.isStatic() ) {
								throw new StaticVerificationException( "instance method '" + y
										+ "' in '" + this + "' cannot override static method '"
										+ x + "' in '" + extendedClass + "'" );
							}
							// check access privileges
							if( y.isPrivate() || ( x.isPublic() && !y.isPublic() )
									|| ( x.isProtected() && y.isPackagePrivate() ) ) {
								throw new StaticVerificationException( "method '" + y
										+ "' in '" + this + "' clashes with method '"
										+ x + "' in '" + extendedClass
										+ "', attempting to assign weaker access privileges '"
										+ ModifierUtils.prettyAccess( y.modifiers() ) + "' to '"
										+ ModifierUtils.prettyAccess( x.modifiers() ) + "'" );
							}
							// check assignable return type;
							if( !y.isReturnTypeAssignable(x) ){
								throw new StaticVerificationException( "method '" + y
										+ "' in '" + this + "' clashes with method '"
										+ x + "' in '" + extendedClass
										+ "', attempting to use incompatible return type" );
							}
							// inherit selection annotation
							if( x.isSelectionMethod() ) {
								y.setSelectionMethod();
							}
							inherited = false;
							break;
						} else {
							// check clash

							// check
						}
					}
					if( inherited ) {
						inheritedMethods.add( x );
					}
				} );
			}
			// ToDo: check implementations
			// for( GroundInterface X : extended )
//					if( needsImplementation ) {
//						throw new StaticVerificationException( variety().labelSingular + " '"
//								+ identifier( true ) + "' must either be abstract or "
//								+ "implement abstract method '" + x + "' in '"
//								+ x.declarationContext().typeConstructor() + "'" );
//					}
			super.finaliseInterface();
		}

		private final List< Member.Field > inheritedFields = new LinkedList<>();

		private final List< Member.HigherMethod > inheritedMethods = new LinkedList<>();

		private final List< Member.HigherConstructor > constructors = new LinkedList<>();

		@Override
		public final Stream< ? extends Member.HigherConstructor > constructors() {
			return constructors.stream();
		}

		public void addConstructor( Member.HigherConstructor constructor ) {
			assert ( !isInterfaceFinalised() );
			assert ( constructor.declarationContext() == this );
			// ToDo: checks
			constructors.add( constructor );
		}

		@Override
		public final Stream< ? extends Member.Field > fields() {
			return Stream.concat( declaredFields(), inheritedFields.stream() );
		}

		@Override
		public final Stream< ? extends Member.HigherMethod > methods() {
			return Stream.concat( declaredMethods(), inheritedMethods.stream() );
		}

	}

	protected class Proxy extends HigherClassOrInterface.Proxy implements GroundClass {

		public Proxy( Substitution substitution ) {
			super( substitution );
		}

		@Override
		public HigherClass typeConstructor() {
			return HigherClass.this;
		}

		@Override
		protected Definition definition() {
			return typeConstructor().innerType();
		}

		@Override
		public GroundClass applySubstitution( Substitution substitution ) {
			return definition().applySubstitution( substitution().andThen( substitution ) );
		}

		public final Optional< ? extends GroundClass > extendedClass() {
			return definition().extendedClass().map( x -> x.applySubstitution( substitution() ) );
		}

		@Override
		protected boolean isSubtypeOf( GroundDataType type, boolean strict ) {
			return ( !strict && isEquivalentTo( type ) )
					|| ( extendedClass().isPresent() && extendedClass().get().isSubtypeOf( type,
					false ) )
					|| extendedInterfaces().anyMatch( x -> x.isSubtypeOf( type, false ) );
		}

		@Override
		public final Stream< ? extends Member.HigherConstructor > constructors() {
			return definition().constructors().map( x -> x.applySubstitution( substitution() ) );
		}

	}

}




