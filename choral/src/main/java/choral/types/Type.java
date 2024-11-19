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

import choral.types.kinds.Kind;

public interface Type extends HasSource {
	Kind kind();

	boolean isEquivalentTo( Type type );

	/**
	 * Relaxed version of {@link #isEquivalentTo}. Doesn't check world correspondence. 
	 * <p>
	 * TODO what exactly does it mean for types to be equivalent??
	 */
	boolean isEquivalentTo_relaxed( Type type );

	boolean isSubtypeOf( Type type, boolean strict );

	/**
	 * Relaxed version of {@link #isSubtypeOf}. Doesn't check world correspondence. 
	 * <p>
	 * Consider the following example
	 * <pre>
	 * {@code
	 * Integer@B b;
	 *int@A a;
	 * }
	 * </pre>
	 * <p>
	 * {@link #isSubtypeOf_relaxed} would return {@code true} when checking if {@code a} 
	 * is a subtype of {@code b} even though {@code a} and {@code b} are at different 
	 * roles. On the same check {@link #isSubtypeOf} would return {@code false}.
	 */
	boolean isSubtypeOf_relaxed( Type type, boolean strict );

	default boolean isStrictSubtypeOf( Type type ) {
		return isSubtypeOf( type, true );
	}

	/**
	 * Relaxed version of {@link #isStrictSubtypeOf}. Doesn't check world correspondence. 
	 * <p>
	 * Consider the following example
	 * <pre>
	 * {@code
	 * Integer@B b;
	 *int@A a;
	 * }
	 * </pre>
	 * <p>
	 * {@link #isStrictSubtypeOf_relaxed} would return {@code true} when checking if {@code a} 
	 * is a subtype of {@code b} even though {@code a} and {@code b} are at different roles. 
	 * On the same check {@link #isStrictSubtypeOf} would return {@code false}.
	 */
	default boolean isStrictSubtypeOf_relaxed( Type type ) {
		return isSubtypeOf_relaxed( type, true );
	}

	default boolean isSubtypeOf( Type type ) {
		return isSubtypeOf( type, false );
	}

	/**
	 * Relaxed version of {@link #isSubtypeOf}. Doesn't check world correspondence. 
	 * <p>
	 * Consider the following example
	 * <pre>
	 * {@code
	 * Integer@B b;
	 *int@A a;
	 * }
	 * </pre>
	 * <p>
	 * {@link #isSubtypeOf_relaxed} would return {@code true} when checking if {@code a} 
	 * is a subtype of {@code b} even though {@code a} and {@code b} are at different roles. 
	 * On the same check {@link #isSubtypeOf} would return {@code false}.
	 */
	default boolean isSubtypeOf_relaxed( Type type ) {
		return isSubtypeOf_relaxed( type, false );
	}

	Universe universe();
}
