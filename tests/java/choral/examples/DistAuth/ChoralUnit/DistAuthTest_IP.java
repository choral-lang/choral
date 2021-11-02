package choral.examples.DistAuth.ChoralUnit;
import choral.lang.Unit;
import choral.choralUnit.annotations.Test;
import choral.choralUnit.testUtils.TestUtils_B;
import choral.runtime.TLSChannel.TLSChannel_B;
import choral.examples.DistAuth.DistAuth_IP;
import choral.annotations.Choreography;

@Choreography( role = "IP", name = "DistAuthTest" )
public class DistAuthTest_IP {
	@Test
	public static void test1() {
		TLSChannel_B < Object > c1;
		c1 = TestUtils_B.newLocalTLSChannel( Unit.id, "DistAuthTest1" );
		TLSChannel_B < Object > c2;
		c2 = TestUtils_B.newLocalTLSChannel( Unit.id, "DistAuthTest2" );
		new DistAuth_IP( c1, c2 ).authenticate( Unit.id );
	}

}
