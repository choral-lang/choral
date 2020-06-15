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

import choral.examples.AuthResult.AuthResult_B;
import choral.examples.DistAuth.DistAuth2;
import choral.examples.DistAuthUtils.AuthToken;
import org.choral.choralUnit.testUtils.TestUtils1;
import org.choral.choralUnit.testUtils.TestUtils2;
import org.choral.runtime.TLSChannel.TLSChannel1;
import org.choral.runtime.TLSChannel.TLSChannel2;
import org.choral.lang.Unit;

import java.util.UUID;

public class Storage {

	private static final String STORAGE_CHANNEL = UUID.randomUUID().toString();
	private static final TLSChannel1< Object > forClients = TestUtils1.newLocalTLSChannel( STORAGE_CHANNEL, Unit.id );
	private static final TLSChannel2< Object > recvChannel = TestUtils2.newLocalTLSChannel( Unit.id, STORAGE_CHANNEL );
	private static boolean keepRunning = false;

	public static TLSChannel1< Object> connect () {
		keepRunning = true;
		return forClients;
	}

	public static void disconnect(){
		keepRunning = false;
		forClients.< StorageMsg >com( new StorageMsg( AuthToken.create(), null ) );
	}

	public void authenticate( TLSChannel1< Object > channel ){
		AuthResult_B authResult = new DistAuth2( channel ).authenticate();
		authResult.right().ifPresent( this::loop );
	}

	public void loop( AuthToken token ){
		StorageMsg m = recvChannel.< StorageMsg >com( Unit.id );
		if( m.token.equals( token ) ){
			Database.store( m.token.id(), m.data );
		}
		if( keepRunning ){
			loop( token );
		}
	}

}
