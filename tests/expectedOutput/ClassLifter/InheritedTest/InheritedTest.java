package ClassLifter.InheritedTest;

import choral.MustPass.ClassLifter.InheritedTest.*;
import choral.annotations.Choreography;

@Choreography( role = "A", name = "InheritedTest" )
public class InheritedTest {
	public void run() {
		Ping ping = new Ping();
		ping.toString();
	}

}
