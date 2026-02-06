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
import choral.types.kinds.Kind;

import java.util.ArrayList;
import java.util.List;

public final class World extends TypeBase implements Comparable< World > {

	static final String DEFAULT_NAME = "A";

	public World( Universe universe, String identifier ) {
		super( universe );
		this.identifier = identifier;
	}

	public World( Universe universe, String identifier, Node sourceCode ) {
		this( universe, identifier );
		setSourceCode( sourceCode );
	}

	@Override
	public Kind kind() {
		return Kind.getStar();
	}

	private final String identifier;

	public String identifier() {
		return identifier;
	}

	@Override
	public String toString() {
		return identifier() /* + "#[" + declarationContext.toString() + "]#" */;
	}

	private WorldParameterDeclarationContext declarationContext;

	WorldParameterDeclarationContext declarationContext() {
		return declarationContext;
	}

	public void setDeclarationContext( WorldParameterDeclarationContext declarationContext ) {
		if( this.declarationContext != null ) {
			throw new UnsupportedOperationException(
					"world parameter '" + identifier + "' is already associated with a declaration context" );
		}
		this.declarationContext = declarationContext;
	}

	@Override
	public boolean isEquivalentTo( Type type ) {
		return this.equals( type );
	}

	@Override
	public boolean isEquivalentTo_relaxed( Type type ) {
		return true;
	}

	@Override
	public boolean isSubtypeOf( Type type, boolean strict ) {
		return ( !strict && this.isEquivalentTo( type ) );
	}

	@Override
	public boolean isSubtypeOf_relaxed( Type type, boolean strict ) {
		return ( !strict && this.isEquivalentTo_relaxed( type ) );
	}

	public static List< World > freshWorlds( Universe universe, int size ) {
		return freshWorlds( universe, size, DEFAULT_NAME );
	}

	public static List< World > freshWorlds( Universe universe, int size, String basename ) {
		List< World > ws = new ArrayList<>( size );
		if( size == 1 ) {
			ws.add( new World( universe, basename ) );
		} else {
			for( int i = 1; i <= size; i++ ) {
				ws.add( new World( universe, basename + i ) );
			}
		}
		return ws;
	}

	@Override
	public int compareTo( World other ) {
		return this.identifier.compareTo( other.identifier );
	}
}
