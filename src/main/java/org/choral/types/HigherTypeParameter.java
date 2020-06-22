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
import org.choral.utils.Formatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class HigherTypeParameter extends HigherReferenceType {

	public HigherTypeParameter(
			Universe universe,
			String identifier,
			List< World > worldParameters
	) {
		super( universe, worldParameters );
		this.identifier = identifier;
	}

	public HigherTypeParameter(
			Universe universe,
			String identifier,
			List< World > worldParameters,
			Node sourceCode
	) {
		this( universe, identifier, worldParameters );
		setSourceCode( sourceCode );
	}

	private final String identifier;

	public String identifier() {
		return identifier;
	}

	@Override
	public String toString() {
		return identifier;
	}

	private TypeParameterDeclarationContext declarationContext;

	public TypeParameterDeclarationContext declarationContext() {
		return declarationContext;
	}

	public void setDeclarationContext( TypeParameterDeclarationContext declarationContext ) {
		assert ( this.declarationContext == null );
		this.declarationContext = declarationContext;
	}

	HigherReferenceType getRawType() {
		if( innerType().isBoundFinalised() ) {
			return new HigherReferenceType( universe(),
					this.worldParameters.stream()
							.map( x -> new World( universe(), x.identifier() ) ).collect(
							Collectors.toList() ) ) {
				@Override
				public GroundReferenceType applyTo( List< ? extends World > args ) {
					return HigherTypeParameter.this.applyTo( args );
				}
			};
		} else {
			return this;
		}
	}

	public boolean checkWithinBounds( Substitution substitution ) {
		HigherReferenceType typeArg = substitution.get( this );
		return isSameKind( typeArg ) && innerType.upperBound()
				.allMatch( u -> typeArg.applyTo( worldParameters ).isSubtypeOf(
						u.applySubstitution( substitution ) ) );
	}

	public void assertWithinBounds( Substitution substitution ) {
		if( !checkWithinBounds( substitution ) ) {
			HigherReferenceType typeArg = substitution.get( this );
			List< World > worldArgs = World.freshWorlds( universe(), worldParameters.size(), "X" );
			Substitution s2 = new Substitution() {
				@Override
				public World get( World placeHolder ) {
					int i = worldParameters.indexOf( placeHolder );
					return ( i < 0 )
							? substitution.get( placeHolder )
							: worldArgs.get( i );
				}

				@Override
				public HigherReferenceType get( HigherTypeParameter placeHolder ) {
					return substitution.get( placeHolder );
				}
			};
			throw new StaticVerificationException( "type argument '"
					+ typeArg
					+ "' is not within bounds, '" + typeArg.applyTo( worldArgs ) + "' must extend "
					+ prettyTypeList( innerType.upperBound().map( x -> x.applySubstitution( s2 ) ) )
					+ " for any role "
					+ prettyTypeList( worldArgs ) );
		}
	}

	private Definition innerType = new Definition();

	public Definition innerType() {
		return innerType;
	}

	@Override
	public GroundReferenceType applyTo( List< ? extends World > args ) {
		return innerType().applySubstitution( getApplicationSubstitution( args ) );
	}

	public final class Definition extends HigherReferenceType.Definition
			implements GroundTypeParameter {

		private Definition(){}

		public String toString() {
			return typeConstructor().toString() +
					worldArguments().stream().map( World::toString ).collect(
							Formatting.joining( ",", "@(", ")", "" ) );
		}

		@Override
		public HigherTypeParameter typeConstructor() {
			return HigherTypeParameter.this;
		}

		private final HashMap< Substitution, GroundReferenceType > alphaIndex = new HashMap<>();

		@Override
		public GroundReferenceType applySubstitution( Substitution substitution ) {
			GroundReferenceType result = alphaIndex.get( substitution );
			if( result == null ) {
				HigherReferenceType t = substitution.get( this.typeConstructor() );
				if( t == typeConstructor() ) {
					result = new Proxy( substitution );
				} else {
					result = t.applyTo( worldArguments().stream()
							.map( substitution::get )
							.collect( Collectors.toCollection(
									() -> new ArrayList<>( worldArguments().size() ) ) )
					);
				}
				alphaIndex.put( substitution, result );
			}
			return result;
		}

		private boolean boundFinalised = false;

		@Override
		public boolean isBoundFinalised() {
			return boundFinalised;
		}

		public void finaliseBound() {
			if( upperClass == null ) {
				setUpperClass();
			}
			this.boundFinalised = true;
		}

		private GroundReferenceType upperClass = null;

		private boolean upperClassImplicit = true;

		private void setUpperClass() {
			assert ( !isBoundFinalised() );
			upperClass = universe().topReferenceType( worldArguments() );
		}

		@Override
		public GroundReferenceType upperClass() {
			return upperClass;
		}

		@Override
		public boolean isUpperClassImplicit() {
			return upperClassImplicit;
		}

		private final List< GroundInterface > upperInterfaces = new ArrayList<>( 10 );

		@Override
		public Stream< ? extends GroundInterface > upperInterfaces() {
			return upperInterfaces.stream();
		}

		public void addUpperBound( GroundReferenceType type ) {
			assert ( !isBoundFinalised() );
			if( type.worldArguments().size() != worldArguments().size() ||
					!type.worldArguments().containsAll( worldParameters ) ) {
				throw new StaticVerificationException(
						"illegal bound, '" + type + "' and '" + this + "' must have the same roles" );
			}
			if( type instanceof GroundInterface ) {
				if( upperClass == null ) {
					setUpperClass();
				}
				if( upperInterfaces().anyMatch( x -> x.isEquivalentTo( type ) ) ) {
					throw new StaticVerificationException(
							"duplicate parameter bound, '" + type + "' is repeated" );
				}
				upperInterfaces.add( (GroundInterface) type );
			} else {
				if( upperClass == null ) {
					upperClass = type;
					upperClassImplicit = false;
				} else {
					String s;
					if( type instanceof GroundEnum ) {
						s = " an enum";
					} else if( type instanceof GroundClass ) {
						s = " a class";
					} else {
						s = " a type parameter";
					}
					throw new StaticVerificationException(
							"interface expected, '" + type + "' is " + s );
				}
			}
		}

		@Override
		public Stream< ? extends GroundReferenceType > upperBound() {
			return Stream.concat( Stream.of( upperClass ), upperInterfaces() );
		}

		@Override
		protected boolean isEquivalentTo( GroundDataType type ) {
			if( type == this ) {
				return true;
			} else if( type instanceof Proxy ) {
				Proxy other = (Proxy) type;
				return ( other.definition() == this ) &&
						worldArguments().equals( other.worldArguments() );
			} else {
				return false;
			}
		}

		@Override
		protected boolean isSubtypeOf( GroundDataType type, boolean strict ) {
			return ( !strict && isEquivalentTo( type ) )
					|| upperClass().isSubtypeOf( type, false )
					|| upperInterfaces().anyMatch( x -> x.isSubtypeOf( type, false ) );
		}

		@Override
		public Stream< ? extends Member.Field > fields() {
			// todo merge
			return Stream.concat( upperClass.fields(),
					upperInterfaces().flatMap( GroundReferenceType::fields ) )
					.filter( Member::isPublic );
		}

		@Override
		public Stream< ? extends Member.HigherMethod > methods() {
			return Stream.concat( upperClass.methods(),
					upperInterfaces().flatMap( GroundReferenceType::methods ) )
					.filter( Member::isPublic );
		}

		@Override
		public final boolean isInterfaceFinalised() {
			return isBoundFinalised() && upperBound().allMatch(
					GroundReferenceType::isInterfaceFinalised );
		}

	}

	private final class Proxy extends HigherReferenceType.Proxy implements GroundTypeParameter {

		private Proxy( Substitution substitution ) {
			super( substitution );
		}

		@Override
		public String toString() {
			return typeConstructor().toString() +
					worldArguments().stream().map( World::toString ).collect(
							Formatting.joining( ",", "@(", ")", "" ) );
		}

		@Override
		public HigherTypeParameter typeConstructor() {
			return HigherTypeParameter.this;
		}

		@Override
		protected Definition definition() {
			return typeConstructor().innerType();
		}

		@Override
		public boolean isBoundFinalised() {
			return definition().isBoundFinalised();
		}

		@Override
		public boolean isUpperClassImplicit() {
			return definition().isUpperClassImplicit();
		}

		@Override
		public GroundReferenceType upperClass() {
			return definition().upperClass().applySubstitution( substitution() );
		}

		@Override
		public Stream< ? extends GroundInterface > upperInterfaces() {
			return definition().upperInterfaces().map( x -> x.applySubstitution( substitution() ) );
		}

		@Override
		public Stream< ? extends GroundReferenceType > upperBound() {
			return definition().upperBound().map( x -> x.applySubstitution( substitution() ) );
		}

		@Override
		public GroundReferenceType applySubstitution( Substitution substitution ) {
			return definition().applySubstitution( substitution().andThen( substitution ) );
		}

		@Override
		protected boolean isEquivalentTo( GroundDataType type ) {
			if( type instanceof Definition ) {
				return type.isEquivalentTo( this );
			} else if( type instanceof Proxy ) {
				Proxy other = (Proxy) type;
				return ( this.definition() == other.definition() ) &&
						worldArguments().equals( other.worldArguments() );
			} else {
				return false;
			}
		}

		@Override
		protected boolean isSubtypeOf( GroundDataType type, boolean strict ) {
			return ( !strict && isEquivalentTo( type ) )
					|| upperClass().isSubtypeOf( type, false )
					|| upperInterfaces().anyMatch( x -> x.isSubtypeOf( type, false ) );
		}

		@Override
		public final boolean isInterfaceFinalised() {
			return definition().isInterfaceFinalised();
		}

	}

}


