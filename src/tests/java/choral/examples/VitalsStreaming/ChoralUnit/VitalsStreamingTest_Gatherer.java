package choral.examples.VitalsStreaming.ChoralUnit;
import org.choral.choralUnit.annotations.Test;
import org.choral.channels.SymChannel_B;
import choral.examples.VitalsStreaming.VitalsStreaming_Gatherer;
import org.choral.annotations.Choreography;
import org.choral.choralUnit.testUtils.TestUtils_B;
import org.choral.lang.Unit;

@Choreography( role = "Gatherer", name = "VitalsStreamingTest" )
public class VitalsStreamingTest_Gatherer {
	@Test
	public static void test1() {
		SymChannel_B < Object > c;
		c = TestUtils_B.newLocalChannel( Unit.id, "VitalsStreaming" );
		new VitalsStreaming_Gatherer( c, Unit.id ).gather( new MyHandler() );
	}

}
