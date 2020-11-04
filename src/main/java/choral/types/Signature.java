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
import choral.utils.Formatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Signature {

	public Signature() {
		parameters = new ArrayList<>();
	}

	public Signature( List< Parameter > parameters ) {
		this( parameters, true );
	}

	Signature( List< Parameter > parameters, boolean performChecks ) {
		if( performChecks ) {
			this.parameters = new ArrayList<>( parameters );
			String[] names = new String[ parameters.size() ];
			int i = 0;
			for( Parameter x : parameters ) {
				for( int j = 0; j < i; j++ ) {
					if( names[ j ].equals( x.identifier() ) ) {
						throw new StaticVerificationException(
								"duplicate signature parameter '" + names[ j ] + "'" );
					}
				}
				names[ i++ ] = x.identifier();
			}
		} else {
			this.parameters = parameters;
		}
		finalise();
	}

	private final List< Parameter > parameters;

	public List< ? extends Parameter > parameters() {
		return Collections.unmodifiableList( parameters );
	}

	public Optional< ? extends Parameter > parameter( int index ) {
		if( 0 <= index && index < parameters.size() ) {
			return Optional.of( parameters.get( index ) );
		} else {
			return Optional.empty();
		}
	}

	public Optional< ? extends Parameter > parameter( String identifier ) {
		return parameters.stream().filter( x -> x.identifier().equals( identifier ) ).findAny();
	}

	public int arity() {
		return parameters.size();
	}

	private boolean finalised = false;

	public boolean isFinalised() {
		return finalised;
	}

	public void finalise() {
		this.finalised = true;
	}

	public void addParameter( String identifier, GroundDataType type ) {
		assert ( !finalised );
		for( Parameter x : parameters ) {
			if( identifier.equals( x.identifier() ) ) {
				throw new StaticVerificationException(
						"duplicate signature parameter '" + identifier + "'" );
			}
		}
		parameters.add( new Parameter( identifier, type ) );
	}

	Signature applySubstitution( Substitution substitution ) {
		return new Signature(
				parameters.stream().map( x -> x.applySubstitution( substitution ) ).collect(
						Collectors.toList() ),
				false );
	}

	@Override
	public String toString() {
		return parameters.stream().map( Parameter::type ).map( GroundDataType::toString )
				.collect( Formatting.joining( ",", "(", ")", "()" ) );
	}

	public static final class Parameter {

		private final String identifier;
		private final GroundDataType type;
		// private final boolean isFinal;

		public Parameter( String identifier, GroundDataType type ) {
			this.identifier = identifier;
			this.type = type;
		}

		public String identifier() {
			return identifier;
		}

		public GroundDataType type() {
			return type;
		}

		Parameter applySubstitution( Substitution substitution ) {
			return new Parameter( identifier, type.applySubstitution( substitution ) );
		}

		@Override
		public String toString() {
			return type + " " + identifier;
		}
	}
}
