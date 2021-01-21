package choral.examples.DistAuth.ChoralUnit;
import choral.lang.Unit;
import choral.choralUnit.testUtils.TestUtils_A;
import choral.examples.AuthResult.AuthResult_B;
import choral.examples.DistAuth.DistAuth_Service;
import choral.annotations.Choreography;
import choral.runtime.TLSChannel.TLSChannel_A;
import choral.choralUnit.annotations.Test;

@Choreography( role = "Service", name = "DistAuthTest" )
public class DistAuthTest_Service {
	@Test
	public static void test1() {
		TLSChannel_A < Object > c2;
		c2 = TestUtils_A.newLocalTLSChannel( "DistAuthTest2", Unit.id );
		AuthResult_B authResult;
		authResult = new DistAuth_Service( Unit.id, c2 ).authenticate( Unit.id );
	}

}
