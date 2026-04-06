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

import java.util.Optional;
import java.util.stream.Stream;

/** @see choral.types.GroundDataType */
public interface GroundReferenceType extends GroundDataType {

	@Override
	HigherReferenceType typeConstructor();

	@Override
	GroundReferenceType applySubstitution( Substitution substitution );

	Stream< ? extends Member.Field > fields();

	Optional< ? extends Member.Field > field( String name );

	/**
	 * Returns this type's <i>member methods</i>, i.e. the methods this type declares and the methods
	 * it inherits from superclasses and superinterfaces. See the Java Language Spec for details.
	 */
	Stream< ? extends Member.HigherMethod > methods();

	Stream< ? extends Member.HigherMethod > methods( String name );

	boolean isInterfaceFinalised();

}
