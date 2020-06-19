package choral.examples.VitalsStreaming.ChoralUnit;
import org.choral.choralUnit.Assert;
import java.util.function.Consumer;
import choral.examples.VitalsStreamingUtils.Vitals;
import org.choral.annotations.Choreography;

@Choreography( role = "Role", name = "MyHandler" )
class MyHandler implements Consumer < Vitals > {
	@Override
	public void accept( Vitals o ) {
		Assert.assertNotNull( o, "success: vitals is not null", "failure: vitals is null" );
	}

}
