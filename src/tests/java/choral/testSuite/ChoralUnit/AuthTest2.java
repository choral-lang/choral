package choral.testSuite.ChoralUnit;

import org.choral.annotations.Choreography;
import org.choral.choralUnit.Assert;
import org.choral.choralUnit.testUtils.TestUtils2;
import org.choral.lang.Channels.SymChannel2;
import org.choral.lang.Unit;

@Choreography( name = "AuthTest", role = "B" )
public class AuthTest2 {
	static public void test1() {
		SymChannel2< Object > c;
		c = TestUtils2.newLocalChannel( Unit.id, "AuthTestChannel1" );
		new Auth2( c ).run( Unit.id, Unit.id );
	}

	static public void test2() {
		Auth2 auth;
		auth = new Auth2( TestUtils2.newLocalChannel( Unit.id, "AuthTestChannel2" ) );
		auth.run( Unit.id, Unit.id );
	}

	static public void test3() {
		Auth2 auth;
		auth = new Auth2( TestUtils2.newLocalChannel( Unit.id, "AuthTestChannel3" ) );
		auth.run( Unit.id, Unit.id );
	}

	static public void test4() {
		Auth2 auth;
		auth = new Auth2( TestUtils2.newLocalChannel( Unit.id, "AuthTestChannel4" ) );
		Boolean result;
		result = auth.checkID( 1 );
		Assert.assertEquals( result, Boolean.TRUE, "Test 4: equality check passed", "Test 4: error" );
	}

}
