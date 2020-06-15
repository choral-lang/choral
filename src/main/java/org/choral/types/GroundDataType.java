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

import java.util.List;

public interface GroundDataType extends DataType, GroundDataTypeOrVoid {

	@Override
	default boolean isHigherType() {
		return false;
	}

	@Override
	default Universe.SpecialTypeTag specialTypeTag() {
		return typeConstructor().specialTypeTag();
	}

	@Override
	default Universe.PrimitiveTypeTag primitiveTypeTag() {
		return typeConstructor().primitiveTypeTag();
	}

	HigherDataType typeConstructor();

	List< ? extends World > worldArguments();

	boolean isInstantiationChecked();

	void checkInstantiation();

	@Override
	GroundDataType applySubstitution( Substitution substitution );

	default boolean isAssignableTo( GroundDataTypeOrVoid type ) {
		return !type.isVoid() && ( type instanceof GroundDataType ) && isSubtypeOf(
				(GroundDataType) type );
	}

}
