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

import org.choral.choralUnit.testUtils.TestUtils_A;
import org.choral.runtime.TLSChannel.TLSChannel_A;
import org.choral.lang.Unit;

import java.util.UUID;

public class HealthIdentityProvider {

	public static TLSChannel_A< Object > connect () {
		String CLIENT_IP = UUID.randomUUID().toString();
		String SERVICE_IP = UUID.randomUUID().toString();
//		new Thread( () -> new Storage().authenticate( TestUtils1.newLocalTLSChannel( SERVICE_IP, Unit.id ) ) ).start();
//		new Thread( () -> new DistAuth3(
//				TestUtils2.newLocalTLSChannel( Unit.id, CLIENT_IP ),
//				TestUtils2.newLocalTLSChannel( Unit.id, SERVICE_IP )
//		).authenticate() ).start();
		return TestUtils_A.newLocalTLSChannel( CLIENT_IP, Unit.id );
	}
}
