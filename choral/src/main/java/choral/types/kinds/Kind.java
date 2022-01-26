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
package choral.types.kinds;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Kind implements Serializable {

	private static final At AT_INSTANCE = new At();
	private static final Star STAR_INSTANCE = new Star();

	private static final Map< Kind, Kind > instances = new HashMap<>();

	static {
		instances.put( AT_INSTANCE, AT_INSTANCE );
		instances.put( STAR_INSTANCE, STAR_INSTANCE );
	}

	protected static Kind getDefaultInstance( Kind instance ) {
		assert instance != null;
		Kind defaultInstance = instances.get( instance );
		if( defaultInstance == null ) {
			defaultInstance = instance;
			instances.put( defaultInstance, defaultInstance );
		}
		return defaultInstance;
	}

	public static Arrow getArrow( Kind domain, Kind codomain ) {
		return (Arrow) getDefaultInstance( new Arrow( domain, codomain ) );
	}

	public static Kind getAtTower( int rank, Kind base ) {
		Kind result = base;
		while( --rank > 0 ) {
			result = new Arrow( Kind.AT_INSTANCE, result );
		}
		return getDefaultInstance( result );
	}

	public static Kind getTower( List< Kind > ks, Kind base ) {
		Kind result = base;
		for( int i = ks.size() - 1; i > 0; i-- ) {
			result = new Arrow( ks.get( i ), result );
		}
		return getDefaultInstance( result );
	}

	public static At getAt() {
		return Kind.AT_INSTANCE;
	}

	public static Star getStar() {
		return Kind.STAR_INSTANCE;
	}

	public Kind apply( Kind argument ) {
		throw new KindApplicationException( this, argument );
	}

	protected boolean isArrow() {
		return false;
	}
}
