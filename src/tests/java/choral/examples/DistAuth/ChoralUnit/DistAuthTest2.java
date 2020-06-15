package choral.examples.DistAuth.ChoralUnit;
import choral.examples.AuthResult.AuthResult_B;
import choral.examples.DistAuth.DistAuth2;
import org.choral.annotations.Choreography;
import org.choral.choralUnit.testUtils.TestUtils1;
import org.choral.runtime.TLSChannel.TLSChannel1;
import org.choral.choralUnit.annotations.Test;
import org.choral.lang.Unit;

@Choreography( role = "Service", name = "DistAuthTest" )
public class DistAuthTest2 {
	@Test
	public static void test1() {
		TLSChannel1 < Object > c2;
		c2 = TestUtils1.newLocalTLSChannel( "DistAuthTest2", Unit.id );
		AuthResult_B authResult;
		authResult = new DistAuth2( Unit.id, c2 ).authenticate( Unit.id );
	}

}
