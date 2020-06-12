package choral.examples.VitalsStreaming.ChoralUnit;

import choral.examples.VitalsStreaming.VitalsStreaming1;
import choral.examples.VitalsStreaming.VitalsStreaming2;
import choral.examples.VitalsStreamingUtils.Sensor;
import choral.examples.VitalsStreamingUtils.Vitals;
import org.choral.annotations.Choreography;
import org.choral.choralUnit.Assert;
import org.choral.choralUnit.testUtils.TestUtils1;
import org.choral.choralUnit.testUtils.TestUtils2;
import org.choral.lang.Channels.SymChannel1;
import org.choral.lang.Channels.SymChannel2;
import java.util.function.Consumer;
import org.choral.choralUnit.annotations.Test;

public class VitalsStreamingTest@( Device, Gatherer ) {

	@Test
	public static void test1(){
		SymChannel@( Device, Gatherer )< Object > c = TestUtils@( Device, Gatherer ).newLocalChannel( "VitalsStreaming"@[ Device, Gatherer ] );
		new VitalsStreaming@( Device, Gatherer )( c, new Sensor@Device() ).gather( new MyHandler@Gatherer() );
	}

}

public class MyHandler@Role implements Consumer@Role< Vitals@Role > {
	@Override
	public void accept( Vitals@Role o ){
		Assert@Role.assertNotNull( o, "success: vitals is not null"@Role, "failure: vitals is null"@Role );
	}
}
