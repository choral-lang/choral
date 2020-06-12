package choral.examples.DistAuth.ChoralUnit;
import choral.examples.AuthResult.AuthResult1;
import choral.examples.DistAuth.DistAuth1;
import choral.examples.DistAuthUtils.Credentials;
import org.choral.annotations.Choreography;
import org.choral.choralUnit.testUtils.TestUtils1;
import org.choral.runtime.TLSChannel.TLSChannel1;
import org.choral.choralUnit.annotations.Test;
import org.choral.lang.Unit;

@Choreography( role = "Client", name = "DistAuthTest" )
public class DistAuthTest1 {
	@Test
	public static void test1() {
		TLSChannel1 < Object > c1;
		c1 = TestUtils1.newLocalTLSChannel( "DistAuthTest1", Unit.id );
		AuthResult1 authResult;
		authResult = new DistAuth1( c1, Unit.id ).authenticate( new Credentials( "john", "doe" ) );
	}

}
