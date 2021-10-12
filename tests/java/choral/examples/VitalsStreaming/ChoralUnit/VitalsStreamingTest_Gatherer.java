package choral.examples.VitalsStreaming.ChoralUnit;
import choral.annotations.Choreography;
import choral.lang.Unit;
import choral.choralUnit.annotations.Test;
import choral.choralUnit.testUtils.TestUtils_B;
import choral.channels.SymChannel_B;
import choral.examples.VitalsStreaming.VitalsStreaming_Gatherer;

@Choreography( role = "Gatherer", name = "VitalsStreamingTest" )
public class VitalsStreamingTest_Gatherer {
	@Test
	public static void test1() {
		SymChannel_B < Object > c;
		c = TestUtils_B.newLocalChannel( Unit.id, "VitalsStreaming" );
		new VitalsStreaming_Gatherer( c, Unit.id ).gather( new MyHandler() );
	}

}
