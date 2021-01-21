package choral.examples.VitalsStreaming.ChoralUnit;
import choral.examples.VitalsStreaming.VitalsStreaming_Device;
import choral.lang.Unit;
import choral.choralUnit.testUtils.TestUtils_A;
import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.choralUnit.annotations.Test;
import choral.examples.VitalsStreamingUtils.Sensor;

@Choreography( role = "Device", name = "VitalsStreamingTest" )
public class VitalsStreamingTest_Device {
	@Test
	public static void test1() {
		SymChannel_A < Object > c;
		c = TestUtils_A.newLocalChannel( "VitalsStreaming", Unit.id );
		new VitalsStreaming_Device( c, new Sensor() ).gather( Unit.id );
	}

}
