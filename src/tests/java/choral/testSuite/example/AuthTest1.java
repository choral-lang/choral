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

package choral.testSuite.example;

import org.choral.choralUnit.Assert;
import org.choral.choralUnit.testUtils.TestUtils1;
import org.choral.lang.Channels.SymChannel1;
import org.choral.lang.Unit;

//@ChoralTest( sourceClass = "AuthTest" )
public class AuthTest1 {

//	@ChoralMethodTest
	public static void test1(){
		SymChannel1< Object > c = TestUtils1.newLocalChannel( "AuthTestChannel1", Unit.id );
		MyAuth1 auth = new MyAuth1( c );
		Boolean result = auth.run( "user", "pwd" );
		Assert.assertEquals( result, Boolean.TRUE, "Test 1: correctly executed", "Test 1: failed" );
	}


}
