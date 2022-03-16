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

package choral.examples.DistAuthUtils;

import choral.runtime.Serializers.KryoSerializable;

import java.util.Objects;
import java.util.UUID;

@KryoSerializable
public class AuthToken {

	private final String id;

	public AuthToken( String id ) {
		this.id = id;
	}

	@Override
	public boolean equals( Object o ) {
		if( this == o ) return true;
		if( o == null || getClass() != o.getClass() ) return false;
		AuthToken authToken = (AuthToken) o;
		return Objects.equals( id, authToken.id );
	}

	@Override
	public int hashCode() {
		return Objects.hash( id );
	}

	public String id() {
		return id;
	}

	public static AuthToken create() {
		return new AuthToken( UUID.randomUUID().toString() );
	}
}
