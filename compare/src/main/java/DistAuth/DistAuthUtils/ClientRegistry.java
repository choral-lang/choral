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

package DistAuth.DistAuthUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ClientRegistry {

	private static final String username_seed = "3dc24197-e783-4e16-8156-8ee47ec6de8f";
	private static final Map< String, Profile > profiles = new HashMap<>();

	static {
		profiles.put( "m1/iyOJP/zBM7F4FuBJxBfDqCc+K1iI1UzJjjATkk9c=",
				new Profile( "uuid_" + getSalt( "JohnDoe" ) ) );
	}

	public static String getSalt( String username ) {
		return getSalt( username, username_seed );
	}

	private static String getHash( String text ) {
		return getSalt( text, "" );
	}

	private static String getSalt( String text, String seed ) {
		try {
			return Base64.getEncoder().encodeToString(
					MessageDigest.getInstance( "SHA3-256" ).digest( ( text + seed ).getBytes() ) );
		} catch( NoSuchAlgorithmException e ) {
			e.printStackTrace();
			return "";
		}
	}

	public static Optional< Profile > getProfile( String hash ) {
		return Optional.ofNullable( profiles.get( hash ) );
	}

	public static Boolean check( String hash ) {
		return profiles.get( hash ) != null;
	}
}
