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

import choral.utils.Formatting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HigherPrimitiveDataType extends HigherDataType implements PrimitiveDataType {

	HigherPrimitiveDataType( Universe universe, Universe.PrimitiveTypeTag tag ) {
		super( universe, List.of( new World( universe, World.DEFAULT_NAME ) ) );
		this.tag = tag;
	}

	private final Universe.PrimitiveTypeTag tag;

	@Override
	public Universe.PrimitiveTypeTag primitiveTypeTag() {
		return tag;
	}

	public String identifier() {
		return tag.identifier;
	}

	public String toString() {
		return identifier();
	}

	@Override
	public HigherClass boxedType() {
		return (HigherClass) universe().specialType( tag.boxedType );
	}

	private final Definition innerType = new Definition();

	protected Definition innerType() {
		return innerType;
	}

	@Override
	public GroundPrimitiveDataType applyTo( List< ? extends World > args ) {
		return innerType().applySubstitution( getApplicationSubstitution( args ) );
	}

	public GroundPrimitiveDataType applyTo( World arg ) {
		return applyTo( List.of( arg ) );
	}

	private final class Definition extends HigherDataType.Definition
			implements GroundPrimitiveDataType {

		private Definition() {
		}

		@Override
		public final String toString() {
			return typeConstructor().toString() + worldArguments().stream().map( World::toString )
					.collect( Formatting.joining( ",", "@(", ")", "" ) );
		}

		@Override
		public HigherPrimitiveDataType typeConstructor() {
			return HigherPrimitiveDataType.this;
		}

		private final Map< World, Proxy > alphaIndex = new HashMap<>();

		@Override
		public GroundPrimitiveDataType applySubstitution( Substitution substitution ) {
			World w = substitution.get( worldParameters.get( 0 ) );
			Proxy result = alphaIndex.get( w );
			if( result == null ) {
				result = new Proxy( substitution );
				alphaIndex.put( w, result );
			}
			return result;
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
			return ( !strict && isEquivalentTo( type ) );
		}

	}

	private final class Proxy extends HigherDataType.Proxy implements GroundPrimitiveDataType {

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
		public HigherPrimitiveDataType typeConstructor() {
			return HigherPrimitiveDataType.this;
		}

		@Override
		protected Definition definition() {
			return typeConstructor().innerType();
		}

		@Override
		public GroundPrimitiveDataType applySubstitution( Substitution substitution ) {
			return new Proxy( substitution().andThen( substitution ) );
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
			return ( !strict && isEquivalentTo( type ) );
		}

	}

}
