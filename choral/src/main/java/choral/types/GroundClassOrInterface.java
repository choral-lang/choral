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

import java.util.List;
import java.util.stream.Stream;

public interface GroundClassOrInterface extends ClassOrInterface, GroundReferenceType {

	HigherClassOrInterface typeConstructor();

	@Override
	default Variety variety() {
		return typeConstructor().variety();
	}

	@Override
	default Package declarationPackage() {
		return typeConstructor().declarationPackage();
	}

	List< ? extends HigherReferenceType > typeArguments();

	GroundClassOrInterface applySubstitution( Substitution substitution );

	boolean isInheritanceFinalised();

	default Stream< ? extends GroundClassOrInterface > extendedClassesOrInterfaces() {
		return extendedInterfaces();
	}

	Stream< ? extends GroundInterface > extendedInterfaces();

	Stream< GroundInterface > allExtendedInterfaces();

	Stream< Member.Field > declaredFields();

	Stream< Member.HigherMethod > declaredMethods();

	@Override
	default boolean isEquivalentToErasureOf( GroundDataType type ) {
		// revise when adding RAW types
		if( type.isClass() || type.isInterface() ) {
			return typeArguments().isEmpty() && isEquivalentTo( type );
		}
		if( type.isTypeParameter() ) {
			GroundTypeParameter other = (GroundTypeParameter) type;
			if( isInterface() && !( other.isUpperClassImplicit() && other.isSubtypeOf( this ) ) ) {
				return false;
			}
			if( isClass() && !isEquivalentToErasureOf( other.upperClass() ) ) {
				return false;
			}
			return other.upperInterfaces().allMatch( this::isSubtypeOf ) &&
					this.extendedInterfaces().allMatch( other::isSubtypeOf );
		}
		return false;
	}
}
