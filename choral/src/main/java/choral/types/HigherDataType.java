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

import choral.exceptions.StaticVerificationException;
import choral.types.kinds.Kind;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class HigherDataType extends TypeBase
		implements DataType, WorldParameterDeclarationContext {

	HigherDataType( Universe universe, List< World > worldParameters ) {
		super( universe );
		this.worldParameters = List.copyOf( worldParameters );
		String[] names = new String[ worldParameters.size() ];
		int i = 0;
		for( World x : worldParameters ) {
			x.setDeclarationContext( this );
			for( int j = 0; j < i; j++ ) {
				if( names[ j ].equals( x.identifier() ) ) {
					throw StaticVerificationException.of(
							"duplicate role parameter '" + names[ j ] + "'", x.sourceCode() );
				}
			}
			names[ i++ ] = x.identifier();
		}
	}

	@Override
	public final boolean isHigherType() {
		return true;
	}

	protected final List< World > worldParameters;

	@Override
	public final List< ? extends World > worldParameters() {
		return worldParameters;
	}

	@Override
	public final Optional< ? extends World > worldParameter( int index ) {
		if( 0 <= index && index < worldParameters.size() ) {
			return Optional.of( worldParameters.get( index ) );
		} else {
			return Optional.empty();
		}
	}

	@Override
	public final Optional< ? extends World > worldParameter( String name ) {
		return worldParameters.stream().filter( m -> m.identifier().equals( name ) ).findAny();
	}

	public abstract GroundDataType applyTo( List< ? extends World > args );

	public GroundDataType applyTo(
			List< ? extends World > worldArgs, List< ? extends HigherReferenceType > typeArgs
	) {
		if( typeArgs.isEmpty() ) {
			return applyTo( worldArgs );
		} else {
			// default implementation, only classes and interfaces have type parameters.
			throw new StaticVerificationException(
					"illegal type instantiation: expected 0 type arguments but found " + typeArgs.size() );
		}
	}

	public HigherDataType partiallyApplyTo( List< ? extends HigherReferenceType > typeArgs ) {
		if( typeArgs.isEmpty() ) {
			return this;
		} else {
			// default implementation, only classes and interfaces have type parameters.
			throw new StaticVerificationException(
					"illegal type instantiation: expected 0 type arguments but found " + typeArgs.size() );
		}
	}

	protected void checkApplicationArguments( List< ? extends World > worldArgs ) {
		if( worldArgs.size() != worldParameters.size() ) {
			throw new StaticVerificationException(
					"illegal type instantiation: expected " + worldParameters.size() + " role arguments but found " + worldArgs.size() );
		}
		for( int i = 0; i < worldArgs.size(); i++ ) {
			World w = worldArgs.get( i );
			for( int j = 0; j < i; j++ ) {
				if( w == worldArgs.get( j ) ) {
					throw new StaticVerificationException(
							"illegal type instantiation: role '" + w + "' must play exactly one role in '" + this + "'" );
				}
			}
		}
	}

	protected Substitution getApplicationSubstitution( List< ? extends World > worldArgs ) {
		checkApplicationArguments( worldArgs );
		return new Substitution() {
//            @Override
//            public Collection<? extends World> worldParameters() {
//                return worldParameters;
//            }
//
//            @Override
//            public Collection<? extends HigherTypeParameter> typeParameters() {
//                return Collections.EMPTY_LIST;
//            }

			@Override
			public World get( World placeHolder ) {
				int i = worldParameters.indexOf( placeHolder );
				return ( i == -1 ) ? placeHolder : worldArgs.get( i );
			}
		};
	}

	@Override
	public Kind kind() {
		return Kind.getAtTower( worldParameters().size(), Kind.getStar() );
	}

	@Override
	public boolean isEquivalentTo( Type type ) {
		if( type instanceof HigherDataType ) {
			HigherDataType t = (HigherDataType) type;
			if( worldParameters.size() == t.worldParameters.size() ) {
				return applyTo( worldParameters ).isEquivalentTo( t.applyTo( worldParameters ) );
			}
		}
		return false;
	}

	@Override
	public boolean isEquivalentTo_relaxed( Type type ) {
		if( type instanceof HigherDataType ) {
			HigherDataType t = (HigherDataType) type;
			return applyTo( worldParameters ).isEquivalentTo_relaxed( t.applyTo( worldParameters ) );
		}
		return false;
	}

	@Override
	public boolean isSubtypeOf( Type type, boolean strict ) {
		if( type instanceof HigherDataType ) {
			HigherDataType t = (HigherDataType) type;
			if( worldParameters.size() == t.worldParameters.size() ) {
				return applyTo( worldParameters ).isSubtypeOf( t.applyTo( worldParameters ),
						strict );
			}
		}
		return false;
	}

	@Override
	public boolean isSubtypeOf_relaxed( Type type, boolean strict ) {
		if( type instanceof HigherDataType ) {
			HigherDataType t = (HigherDataType) type;
			return applyTo( worldParameters ).isSubtypeOf_relaxed( t.applyTo( worldParameters ),
					strict );
		}
		return false;
	}

	protected abstract class Definition extends TypeBase implements GroundDataType {

		Definition() {
			super( HigherDataType.this.universe() );
		}

		@Override
		public final boolean isHigherType() {
			return false;
		}

		@Override
		public final List< ? extends World > worldArguments() {
			return typeConstructor().worldParameters();
		}

		@Override
		public boolean isInstantiationChecked() {
			return true;
		}

		@Override
		public void checkInstantiation() {
		}

		@Override
		public final Kind kind() {
			return Kind.getStar();
		}

		@Override
		public final boolean isEquivalentTo( Type type ) {
			return this.isSameKind( type ) &&
					type instanceof GroundDataType &&
					isEquivalentTo( (GroundDataType) type );
		}

		@Override
		public final boolean isEquivalentTo_relaxed( Type type ) {
			return this.isSameKind( type ) &&
					type instanceof GroundDataType &&
					isEquivalentTo_relaxed( (GroundDataType) type );
		}

		protected abstract boolean isEquivalentTo( GroundDataType type );

		protected abstract boolean isEquivalentTo_relaxed( GroundDataType type );

		@Override
		public final boolean isSubtypeOf( Type type, boolean strict ) {
			return this.isSameKind( type ) &&
					type instanceof GroundDataType &&
					isSubtypeOf( (GroundDataType) type, strict );
		}

		@Override
		public final boolean isSubtypeOf_relaxed( Type type, boolean strict ) {
			return this.isSameKind( type ) &&
					type instanceof GroundDataType &&
					isSubtypeOf_relaxed( (GroundDataType) type, strict );
		}

		protected abstract boolean isSubtypeOf( GroundDataType type, boolean strict );

		protected abstract boolean isSubtypeOf_relaxed( GroundDataType type, boolean strict );

	}

	protected abstract class Proxy extends TypeBase implements GroundDataType {

		private final Substitution substitution;

		Proxy( Substitution substitution ) {
			super( HigherDataType.this.universe() );
			this.substitution = substitution;
		}

		@Override
		public final boolean isHigherType() {
			return false;
		}

		protected abstract Definition definition();

		protected Substitution substitution() {
			return substitution;
		}

		public final List< ? extends World > worldArguments() {
			return definition().worldArguments().stream().map( substitution()::get ).collect(
					Collectors.toList() );
		}

		@Override
		public boolean isInstantiationChecked() {
			return true;
		}

		@Override
		public void checkInstantiation() {
		}

		@Override
		public final Kind kind() {
			return definition().kind();
		}

		@Override
		public final boolean isEquivalentTo( Type type ) {
			return this.isSameKind( type ) &&
					type instanceof GroundDataType &&
					isEquivalentTo( (GroundDataType) type );
		}

		@Override
		public final boolean isEquivalentTo_relaxed( Type type ) {
			return this.isSameKind( type ) &&
					type instanceof GroundDataType &&
					isEquivalentTo_relaxed( (GroundDataType) type );
		}

		protected abstract boolean isEquivalentTo( GroundDataType type );

		protected abstract boolean isEquivalentTo_relaxed( GroundDataType type );

		@Override
		public final boolean isSubtypeOf( Type type, boolean strict ) {
			return this.isSameKind( type ) &&
					type instanceof GroundDataType &&
					isSubtypeOf( (GroundDataType) type, strict );
		}

		@Override
		public final boolean isSubtypeOf_relaxed( Type type, boolean strict ) {
			return this.isSameKind( type ) &&
					type instanceof GroundDataType &&
					isSubtypeOf_relaxed( (GroundDataType) type, strict );
		}

		protected abstract boolean isSubtypeOf( GroundDataType type, boolean strict );

		protected abstract boolean isSubtypeOf_relaxed( GroundDataType type, boolean strict );

	}

}
