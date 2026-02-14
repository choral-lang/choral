/*
 * Copyright (C) 2019-2020 by Saverio Giallorenzo <saverio.giallorenzo@gmail.com>
 * Copyright (C) 2019-2020 by Fabrizio Montesi <famontesi@gmail.com>
 * Copyright (C) 2019-2020 by Marco Peressotti <marco.peressotti@gmail.com>
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

/** @see choral.types.GroundDataType */
public interface GroundDataTypeOrVoid extends DataTypeOrVoid {

	GroundDataTypeOrVoid applySubstitution( Substitution substitution );

	boolean isAssignableTo( GroundDataTypeOrVoid type );

	/**
	 * A version of {@link #isAssignableTo} that doesn't check world correspondence.
	 * As an example, consider the following:
	 * <pre><code>
	 * int@B b = 0@B;
	 * int@A a = b;
	 * </code></pre>
	 * When type checking {@code int@A a = b;}, {@link #isAssignableTo} returns {@code false}
	 * because the two variables are at different roles. In contrast,
	 * {@link #isAssignableTo_relaxed} doesn't check the roles; it just checks if the
	 * underlying Java types are compatible. In the example above, {@link #isAssignableTo_relaxed}
	 * returns {@code true}.
	 */
	boolean isAssignableTo_relaxed( GroundDataTypeOrVoid type );
}
