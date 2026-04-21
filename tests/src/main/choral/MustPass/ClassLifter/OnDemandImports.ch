package choral.MustPass.ClassLifter.OnDemandImports;

import java.time.*;

class OnDemandImports@( A, B ) {
	public void test(){
		LocalTime@A time  = LocalTime@A.now();
	}
}
