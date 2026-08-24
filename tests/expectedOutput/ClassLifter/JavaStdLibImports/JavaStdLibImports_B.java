package ClassLifter.JavaStdLibImports;

import choral.annotations.Choreography;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.List;

@Choreography( role = "B", name = "JavaStdLibImports" )
class JavaStdLibImports_B {
	public void test() {
		String bs = "world";
		Integer bi_b = Integer.valueOf( 99 );
		List < String > blist = new ArrayList < String >();
		blist.add( bs );
	}

}
