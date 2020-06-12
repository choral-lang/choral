package choral.examples.VitalsStreaming.ChoralUnit;
import choral.examples.VitalsStreaming.VitalsStreaming2;
import org.choral.annotations.Choreography;
import org.choral.choralUnit.testUtils.TestUtils2;
import org.choral.lang.Channels.SymChannel2;
import org.choral.choralUnit.annotations.Test;
import org.choral.lang.Unit;

@Choreography( role = "Gatherer", name = "VitalsStreamingTest" )
public class VitalsStreamingTest2 {
	@Test
	public static void test1() {
		SymChannel2 < Object > c;
		c = TestUtils2.newLocalChannel( Unit.id, "VitalsStreaming" );
		new VitalsStreaming2( c, Unit.id ).gather( new MyHandler() );
	}

}
