package choral.examples.VitalsStreaming.ChoralUnit;
import choral.examples.VitalsStreaming.VitalsStreaming1;
import choral.examples.VitalsStreamingUtils.Sensor;
import org.choral.annotations.Choreography;
import org.choral.choralUnit.testUtils.TestUtils1;
import org.choral.lang.Channels.SymChannel1;
import org.choral.choralUnit.annotations.Test;
import org.choral.lang.Unit;

@Choreography( role = "Device", name = "VitalsStreamingTest" )
public class VitalsStreamingTest1 {
	@Test
	public static void test1() {
		SymChannel1 < Object > c;
		c = TestUtils1.newLocalChannel( "VitalsStreaming", Unit.id );
		new VitalsStreaming1( c, new Sensor() ).gather( Unit.id );
	}

}
