package choral.examples.DistAuth.ChoralUnit;
import org.choral.annotations.Choreography;
import org.choral.choralUnit.testUtils.TestUtils_A;
import org.choral.runtime.TLSChannel.TLSChannel_A;
import choral.examples.AuthResult.AuthResult_B;
import org.choral.choralUnit.annotations.Test;
import choral.examples.DistAuth.DistAuth_Service;
import org.choral.lang.Unit;

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
