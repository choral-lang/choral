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

package choral.examples.HealthCareService;


import choral.examples.AuthResult.AuthResult1;
import choral.examples.DistAuth.DistAuth1;
import choral.examples.DistAuthUtils.Credentials;
import choral.examples.VitalsStreaming.VitalsStreaming2;
import org.choral.runtime.TLSChannel.TLSChannel1;

public class HealthCareService {
	public static void main ( String[] args ) {
		TLSChannel1< Object > toIP = HealthIdentityProvider.connect();
		TLSChannel1< Object > toStorage = Storage.connect();
		AuthResult1 authResult = new DistAuth1( toIP ).authenticate( getCredentials() );
		authResult.left().ifPresent( token ->
				DeviceRegistry
						.parallelStream()
						.map( Device::connect )
						.map( VitalsStreaming2::new )
						.forEach( vs ->
								vs.gather( data -> toStorage.< StorageMsg >com( new StorageMsg( token, data ) ) )
						)
		);
		Storage.disconnect();
	}

	private static Credentials getCredentials () {
		return new Credentials( "john", "doe" );
	}
}
