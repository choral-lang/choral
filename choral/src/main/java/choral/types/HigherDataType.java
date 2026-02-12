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

/**
 * A "higher" datatype is a type with {@link World}s as parameters. For example, in the definition
 * {@code class Foo@(A,B) { ... }}, the class {@code Foo} is a higher class.
 */
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

		protected abstract boolean isEquivalentTo( GroundDataType type );

		@Override
		public final boolean isSubtypeOf( Type type, boolean strict ) {
			return this.isSameKind( type ) &&
					type instanceof GroundDataType &&
					isSubtypeOf( (GroundDataType) type, strict );
		}

		protected abstract boolean isSubtypeOf( GroundDataType type, boolean strict );

	}

	/**
	 * Proxies are a way to deal with partially-constructed types and type abstraction/application.
	 * <p>
	 * Say we encounter class {@code Foo@A { int@A x }} and create an entry in our type definition
	 * table. The entry says that {@code Foo} takes a world parameter {@code A}, that it has a field
	 * {@code x}, etc. Imagine that later on we encounter a field like {@code Foo@B y}. To
	 * construct this type, we'd need to fetch the definition of {@code Foo} from the type table and
	 * instantiate it by replacing the formal parameter {@code A} with the actual parameter
	 * {@code B}. Proxies are a way to do this instantiation lazily.
	 * <p>
	 * Why do we need lazy instantiation in the first place? Say we have the definition
	 * {@code class Bar@A { Bar@A x }}. To enter the definition of {@code Bar} in the type table, we
	 * need to say it has a field {@code x} whose type is the result of applying {@code Bar} to
	 * {@code A}... But we haven't defined {@code Bar} yet! So, we enter a Proxy into the
	 * type table instead.
	 * <p>
	 * A class <code>class Foo@(A,B) { ... }</code> is modeled as a HigherClass with world parameters
	 * A and B. Internally, the HigherClass maintains a GroundClass as an "archetype".
	 * Every time Foo is applied to a pair of worlds, say C and D, the
	 * instance of HigherClass returns a Proxy. The Proxy redirects all queries (e.g., for methods,
	 * fields, etc) to the archetype and then applies the substitution [C/A, D/B] to the result on
	 * the fly. This trick allows the type checker to incrementally enrich the definition of Foo
	 * without having to run around backpatching all its instances.
	 * <p>
	 * Consider for instance the definition of Enum from the standard library. Its lifting to Choral
	 * would be class <code>Enum@A< T@B extends Enum@B<T> > {...}</code>. To build the definition of
	 * Enum, we need to specify that it takes a type parameter, and that the type parameter has Enum as an
	 * upper bound. Having Enum is as an upper bound is a problem, because Enum hasn't been defined
	 * yet! But with the Archetype-Proxy trick, we can use a proxy for Enum@B and continue building
	 * the definition of Enum.
	 */
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

		protected abstract boolean isEquivalentTo( GroundDataType type );

		@Override
		public final boolean isSubtypeOf( Type type, boolean strict ) {
			return this.isSameKind( type ) &&
					type instanceof GroundDataType &&
					isSubtypeOf( (GroundDataType) type, strict );
		}

		protected abstract boolean isSubtypeOf( GroundDataType type, boolean strict );

	}

}
