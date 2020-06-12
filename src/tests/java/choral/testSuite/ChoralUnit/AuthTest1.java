package choral.testSuite.ChoralUnit;

import org.choral.annotations.Choreography;
import org.choral.choralUnit.Assert;
import org.choral.choralUnit.AssertException;
import org.choral.choralUnit.testUtils.TestUtils1;
import org.choral.lang.Channels.SymChannel1;
import org.choral.runtime.Enum.EnumBoolean;
import org.choral.lang.Unit;

@Choreography( name = "AuthTest", role = "A" )
public class AuthTest1 {
	static public void test1() {
		SymChannel1< Object > c;
		c = TestUtils1.newLocalChannel( "AuthTestChannel1", Unit.id );
		EnumBoolean result;
		result = new Auth1( c ).run( "USR", "PSW" );
		Assert.assertEquals( result, EnumBoolean.True, "Test 1: Valid login tested correctly", "Test 1: error" );
	}

	static public void test2() {
		Auth1 auth;
		auth = new Auth1( TestUtils1.newLocalChannel( "AuthTestChannel2", Unit.id ) );
		EnumBoolean result;
		result = auth.run( "WRONG_USER", "PSW" );
		Assert.assertEquals( result, EnumBoolean.False, "Test 2: Invalid login tested correctly", "Test 2: error" );
	}

	static public void test3() {
		Auth1 auth;
		auth = new Auth1( TestUtils1.newLocalChannel( "AuthTestChannel3", Unit.id ) );
		EnumBoolean result;
		result = auth.run( "WRONG_USER", "PSW" );
		try {
			Assert.assertEquals( result, EnumBoolean.True, "Test 3: error, should have failed", "Test 3: should fail" );
		}
		catch ( AssertException e ) {
			System.out.println( "Assertion Exception correctly caught in Test 3" );
		}
	}

	static public void test4() {
		Auth1 auth;
		auth = new Auth1( TestUtils1.newLocalChannel( "AuthTestChannel4", Unit.id ) );
		auth.checkID( Unit.id );
	}

}
