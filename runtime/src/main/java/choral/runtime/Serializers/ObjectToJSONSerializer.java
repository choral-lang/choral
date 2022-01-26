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

package choral.runtime.Serializers;

import com.google.gson.Gson;

public class ObjectToJSONSerializer implements ChoralSerializer< Object, String > {

	static final private Gson gson = new Gson();
	static final private String header = "!";
	static final private ObjectToJSONSerializer INSTANCE = new ObjectToJSONSerializer();

	private ObjectToJSONSerializer() {
	}

	public static ObjectToJSONSerializer getInstance() {
		return INSTANCE;
	}

	@Override
	public < M > String fromObject( M o ) {
		return o.getClass().getName() + header
				+ gson.toJson( o );
	}

	@Override
	public < T > T toObject( String s ) {
		s = s.trim();
		String className = s.substring( 0, s.indexOf( header ) );
		String object = s.substring( s.indexOf( header ) + header.length() );
		try {
			return (T) gson.fromJson( object, Class.forName( className ) );
		} catch( ClassNotFoundException e ) {
			e.printStackTrace();
		}
		return null;
	}
}
