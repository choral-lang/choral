package choral.MustPass.ClassLifter.OnDemandImports;

import choral.annotations.Choreography;
import java.time.*;

@Choreography( role = "A", name = "OnDemandImports" )
class OnDemandImports_A {
	public void test() {
		LocalTime time = LocalTime.now();
	}

}
