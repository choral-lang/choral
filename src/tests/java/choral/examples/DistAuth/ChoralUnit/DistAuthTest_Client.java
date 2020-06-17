package choral.examples.DistAuth.ChoralUnit;
import choral.examples.AuthResult.AuthResult_A;
import choral.examples.DistAuth.DistAuth_Client;
import choral.examples.DistAuthUtils.Credentials;
import org.choral.choralUnit.annotations.Test;
import org.choral.choralUnit.testUtils.TestUtils;
import org.choral.choralUnit.testUtils.TestUtils_A;
import org.choral.lang.Unit;
import org.choral.runtime.TLSChannel.TLSChannel_A;

public class DistAuthTest_Client {
	@Test
	public static void test1() {
		TLSChannel_A< Object > c1;
		c1 = TestUtils_A.newLocalTLSChannel( "DistAuthTest1", Unit.id );
		AuthResult_A authResult;
		authResult = new DistAuth_Client( c1, Unit.id ).authenticate( new Credentials( "john", "doe" ) );
	}

}
