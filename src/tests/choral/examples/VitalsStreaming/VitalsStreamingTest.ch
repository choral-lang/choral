package choral.examples.VitalsStreaming.ChoralUnit;

import choral.examples.VitalsStreaming.VitalsStreaming;
import choral.examples.VitalsStreamingUtils.Sensor;
import choral.examples.VitalsStreamingUtils.Vitals;
import choral.choralUnit.Assert;
import choral.choralUnit.testUtils.TestUtils;
import choral.channels.SymChannel;
import java.util.function.Consumer;
import choral.choralUnit.annotations.Test;

public class VitalsStreamingTest@( Device, Gatherer ) {

	@Test
	public static void test1(){
		SymChannel@( Device, Gatherer )< Object > c = TestUtils@( Device, Gatherer ).newLocalChannel( "VitalsStreaming"@[ Device, Gatherer ] );
		new VitalsStreaming@( Device, Gatherer )( c, new Sensor@Device() ).gather( new MyHandler@Gatherer() );
	}

}

class MyHandler@Role implements Consumer@Role< Vitals > {
	@Override
	public void accept( Vitals@Role o ){
		Assert@Role.assertNotNull( o, "success: vitals is not null"@Role, "failure: vitals is null"@Role );
	}
}
