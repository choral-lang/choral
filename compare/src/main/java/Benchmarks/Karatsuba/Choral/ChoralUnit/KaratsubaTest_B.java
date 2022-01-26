package Benchmarks.Karatsuba.Choral.ChoralUnit;

import Benchmarks.Karatsuba.Choral.Karatsuba_B;
import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.choralUnit.annotations.Test;
import choral.choralUnit.testUtils.TestUtils_A;
import choral.choralUnit.testUtils.TestUtils_B;
import choral.lang.Unit;

@Choreography( role = "B", name = "KaratsubaTest" )
public class KaratsubaTest_B {
	@Test
	public static void test1() {
		SymChannel_B < Object > ch_AB;
		ch_AB = TestUtils_B.newLocalChannel( Unit.id, "ch_AB" );
		SymChannel_A < Object > ch_BC;
		ch_BC = TestUtils_A.newLocalChannel( "ch_BC", Unit.id );
		Karatsuba_B.multiply( Unit.id, Unit.id, ch_AB, ch_BC, Unit.id );
	}

}
