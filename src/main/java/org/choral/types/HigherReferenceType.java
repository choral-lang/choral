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

import java.util.*;
import java.util.stream.Stream;

public abstract class HigherReferenceType extends HigherDataType implements ReferenceType {

	HigherReferenceType(
			Universe universe,
			List< World > worldParameters
	) {
		super( universe, worldParameters );
	}

//	public abstract HigherReferenceType.Definition innerType();

	public abstract GroundReferenceType applyTo( List< ? extends World > args );

	protected abstract class Definition extends HigherDataType.Definition
			implements GroundReferenceType {

		Definition(){}

		public final Optional< ? extends Member.Field > field( String name ) {
			return fields()
					.filter( m -> m.identifier().equals( name ) )
					.findAny();
		}

		public final Stream< ? extends Member.HigherMethod > methods( String name ) {
			return methods().filter( m -> m.identifier().equals( name ) );
		}

	}

	protected abstract class Proxy extends HigherDataType.Proxy implements GroundReferenceType {

		Proxy( Substitution substitution ) {
			super( substitution );
		}

		@Override
		protected abstract HigherReferenceType.Definition definition();

		public Stream< ? extends Member.Field > fields() {
			return definition().fields().map( x -> x.applySubstitution( substitution() ) );
		}

		public Optional< ? extends Member.Field > field( String name ) {
			return definition().field( name ).map( x -> x.applySubstitution( substitution() ) );
		}

		public Stream< ? extends Member.HigherMethod > methods() {
			return definition().methods().map( x -> x.applySubstitution( substitution() ) );
		}

		public Stream< ? extends Member.HigherMethod > methods( String name ) {
			return definition().methods( name ).map( x -> x.applySubstitution( substitution() ) );
		}

	}
}

