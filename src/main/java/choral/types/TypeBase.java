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

import choral.ast.Node;
import choral.utils.Formatting;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class TypeBase implements Type {

	public TypeBase( Universe universe ) {
		this.universe = universe;
	}

	protected final boolean isSameKind( Type type ) {
		return ( type != null )
				&& ( this.universe() == type.universe() )
				&& ( this.kind() != null )
				&& ( this.kind().equals( type.kind() ) );
	}

	@Override
	public final boolean isStrictSubtypeOf( Type type ) {
		return isSubtypeOf( type, true );
	}

	@Override
	public final boolean isSubtypeOf( Type type ) {
		return isSubtypeOf( type, false );
	}

	// source code position for error reporting
	private Node source;

	@Override
	public final Optional< Node > sourceCode() {
		return Optional.ofNullable( source );
	}

	@Override
	public final void setSourceCode( Node source ) {
		this.source = source;
	}

	private final Universe universe;

	@Override
	public Universe universe() {
		return universe;
	}

	static String prettyTypeList( List< ? extends Type > types ) {
		return prettyTypeList( types.stream() );
	}

	static String prettyTypeList( Stream< ? extends Type > types ) {
		return prettyQuotedList( types );
	}

	static String prettyQuotedList( Stream< ? > elements ) {
		return elements.map( x -> "'" + x + "'" )
				.collect( Collectors.collectingAndThen( Collectors.toList(),
						Formatting.joiningOxfordComma() ) );
	}
}
