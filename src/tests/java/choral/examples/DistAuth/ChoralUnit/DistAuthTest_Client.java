package choral.examples.DistAuth.ChoralUnit;
import choral.lang.Unit;
import choral.examples.DistAuth.DistAuth_Client;
import choral.runtime.TLSChannel.TLSChannel_A;
import choral.annotations.Choreography;
import choral.examples.AuthResult.AuthResult_A;
import choral.examples.DistAuthUtils.Credentials;
import choral.choralUnit.testUtils.TestUtils_A;
import choral.choralUnit.annotations.Test;

@Choreography( role = "Client", name = "DistAuthTest" )
public class DistAuthTest_Client {
	@Test
	public static void test1() {
		TLSChannel_A < Object > c1;
		c1 = TestUtils_A.newLocalTLSChannel( "DistAuthTest1", Unit.id );
		AuthResult_A authResult;
		authResult = new DistAuth_Client( c1, Unit.id ).authenticate( new Credentials( "john", "doe" ) );
	}

}
