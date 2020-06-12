package choral.examples.DistAuth.ChoralUnit;

import choral.examples.AuthResult.AuthResult1;
import choral.examples.AuthResult.AuthResult2;
import choral.examples.DistAuth.DistAuth1;
import choral.examples.DistAuth.DistAuth2;
import choral.examples.DistAuth.DistAuth3;
import choral.examples.DistAuthUtils.Credentials;
import org.choral.annotations.Choreography;
import org.choral.choralUnit.testUtils.TestUtils1;
import org.choral.choralUnit.testUtils.TestUtils2;
import org.choral.runtime.TLSChannel.TLSChannel1;
import org.choral.runtime.TLSChannel.TLSChannel2;
import org.choral.choralUnit.annotations.Test;


public class DistAuthTest@( Client, Service, IP ) {

	@Test
	public static void test1(){
		TLSChannel@( Client, IP )< Object > c1 = TestUtils@( Client, IP ).newLocalTLSChannel( "DistAuthTest1"@[ Client, IP ] );
		TLSChannel@( Service, IP )< Object > c2 = TestUtils@( Service, IP ).newLocalTLSChannel( "DistAuthTest2"@[ Service, IP ] );
		AuthResult@( Client, Service ) authResult = new DistAuth@( Client, Service, IP )( c1, c2 ).authenticate( new Credentials@Client( "john"@Client, "doe"@Client ) );
//		if( profile.isEmpty() ){
//			System@Service.out.println( "Profile empty"@Service );
//		} else {
//			System@Service.out.println( "Profile id: "@Service + profile.get().id() );
//		}
	}

}
