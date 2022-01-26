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

import com.google.common.collect.Streams;

import java.util.Optional;
import java.util.stream.Stream;

public interface GroundClass extends GroundClassOrInterface, Class {

	@Override
	default boolean isHigherType() {
		return false;
	}

	@Override
	default boolean isBoxedType() {
		return typeConstructor().isBoxedType();
	}

	@Override
	default GroundPrimitiveDataType unboxedType() {
		if( isBoxedType() ) {
			return typeConstructor().unboxedType().applyTo( worldArguments() );
		} else {
			return null;
		}
	}

	@Override
	HigherClass typeConstructor();

	@Override
	GroundClass applySubstitution( Substitution substitution );

	@Override
	default boolean isAssignableTo( GroundDataTypeOrVoid type ) {
		if( isBoxedType() && type instanceof GroundPrimitiveDataType ) {
			return unboxedType().isAssignableTo( type );
		} else {
			return !type.isVoid() && ( type instanceof GroundDataType )
					&& isSubtypeOf( (GroundDataType) type );
		}
	}

	default Stream< ? extends GroundClassOrInterface > extendedClassesOrInterfaces() {
		if( extendedClass().isPresent() ) {
			return Streams.concat( Stream.of( extendedClass().get() ), extendedInterfaces() );
		} else {
			return extendedInterfaces();
		}
	}

	Optional< ? extends GroundClass > extendedClass();

	Stream< ? extends Member.HigherConstructor > constructors();
}
