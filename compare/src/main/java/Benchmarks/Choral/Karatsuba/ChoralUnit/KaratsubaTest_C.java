package Benchmarks.Choral.Karatsuba.ChoralUnit;

import Benchmarks.Choral.Karatsuba.Karatsuba_C;
import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.choralUnit.annotations.Test;
import choral.choralUnit.testUtils.TestUtils_A;
import choral.choralUnit.testUtils.TestUtils_B;
import choral.lang.Unit;

@Choreography( role = "C", name = "KaratsubaTest" )
public class KaratsubaTest_C {
	@Test
	public static void test1() {
		SymChannel_B< Object > ch_BC;
		ch_BC = TestUtils_B.newLocalChannel( Unit.id, "ch_BC" );
		SymChannel_A< Object > ch_CA;
		ch_CA = TestUtils_A.newLocalChannel( "ch_CA", Unit.id );
		Karatsuba_C.multiply( Unit.id, Unit.id, Unit.id, ch_BC, ch_CA );
	}

}
