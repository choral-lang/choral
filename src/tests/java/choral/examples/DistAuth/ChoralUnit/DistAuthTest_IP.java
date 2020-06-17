package choral.examples.DistAuth.ChoralUnit;
import choral.examples.DistAuth.DistAuth_IP;
import choral.examples.DistAuthUtils.Credentials;
import org.choral.choralUnit.annotations.Test;
import org.choral.choralUnit.testUtils.TestUtils;
import org.choral.choralUnit.testUtils.TestUtils_B;
import org.choral.lang.Unit;
import org.choral.runtime.TLSChannel.TLSChannel_B;

public class DistAuthTest_IP {
	@Test
	public static void test1() {
		TLSChannel_B< Object > c1;
		c1 = TestUtils_B.newLocalTLSChannel( Unit.id, "DistAuthTest1" );
		TLSChannel_B < Object > c2;
		c2 = TestUtils_B.newLocalTLSChannel( Unit.id, "DistAuthTest2" );
		new DistAuth_IP( c1, c2 ).authenticate( Unit.id );
	}

}
