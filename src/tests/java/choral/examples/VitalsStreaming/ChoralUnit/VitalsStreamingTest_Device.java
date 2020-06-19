package choral.examples.VitalsStreaming.ChoralUnit;
import org.choral.choralUnit.annotations.Test;
import org.choral.channels.SymChannel_A;
import choral.examples.VitalsStreamingUtils.Sensor;
import org.choral.lang.Unit;
import choral.examples.VitalsStreaming.VitalsStreaming_Device;
import org.choral.choralUnit.testUtils.TestUtils_A;
import org.choral.annotations.Choreography;

@Choreography( role = "Device", name = "VitalsStreamingTest" )
public class VitalsStreamingTest_Device {
	@Test
	public static void test1() {
		SymChannel_A < Object > c;
		c = TestUtils_A.newLocalChannel( "VitalsStreaming", Unit.id );
		new VitalsStreaming_Device( c, new Sensor() ).gather( Unit.id );
	}

}
