package choral.MustPass.DistAuth.ChoralUnit;

import choral.MustPass.AuthResult.AuthResult;
import choral.MustPass.DistAuth.DistAuth;
import choral.MustPass.DistAuthUtils.Credentials;
import choral.choralUnit.testUtils.TestUtils;
import choral.runtime.TLSChannel.TLSChannel;
import choral.choralUnit.annotations.Test;

public class DistAuthTest@( Client, Service, IP ) {

	@Test
	public static void test1(){
		TLSChannel@( Client, IP )< Object > c1 = TestUtils@( Client, IP ).newLocalTLSChannel( "DistAuthTest1"@[ Client, IP ] );
		TLSChannel@( Service, IP )< Object > c2 = TestUtils@( Service, IP ).newLocalTLSChannel( "DistAuthTest2"@[ Service, IP ] );
		AuthResult@( Client, Service ) authResult = new DistAuth@( Client, Service, IP )( c1, c2 ).authenticate( new Credentials@Client( "john"@Client, "doe"@Client ) );
	}

}
