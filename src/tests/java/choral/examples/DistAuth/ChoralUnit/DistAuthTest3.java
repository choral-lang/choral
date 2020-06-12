package choral.examples.DistAuth.ChoralUnit;
import choral.examples.DistAuth.DistAuth3;
import org.choral.annotations.Choreography;
import org.choral.choralUnit.testUtils.TestUtils2;
import org.choral.runtime.TLSChannel.TLSChannel2;
import org.choral.choralUnit.annotations.Test;
import org.choral.lang.Unit;

@Choreography( role = "IP", name = "DistAuthTest" )
public class DistAuthTest3 {
	@Test
	public static void test1() {
		TLSChannel2 < Object > c1;
		c1 = TestUtils2.newLocalTLSChannel( Unit.id, "DistAuthTest1" );
		TLSChannel2 < Object > c2;
		c2 = TestUtils2.newLocalTLSChannel( Unit.id, "DistAuthTest2" );
		new DistAuth3( c1, c2 ).authenticate( Unit.id );
	}

}
