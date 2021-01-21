package choral.examples.VitalsStreaming.ChoralUnit;
import choral.annotations.Choreography;
import choral.choralUnit.Assert;
import choral.examples.VitalsStreamingUtils.Vitals;
import java.util.function.Consumer;

@Choreography( role = "Role", name = "MyHandler" )
class MyHandler implements Consumer < Vitals > {
	@Override
	public void accept( Vitals o ) {
		Assert.assertNotNull( o, "success: vitals is not null", "failure: vitals is null" );
	}

}
