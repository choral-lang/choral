package choral.examples.VitalsStreaming.ChoralUnit;
import choral.examples.VitalsStreamingUtils.Vitals;
import org.choral.annotations.Choreography;
import org.choral.choralUnit.Assert;

import java.util.function.Consumer;

@Choreography( role = "Role", name = "MyHandler" )
public class MyHandler implements Consumer < Vitals > {
	@Override
	public void accept( Vitals o ) {
		Assert.assertNotNull( o, "success: vitals is not null", "failure: vitals is null" );
	}

}
