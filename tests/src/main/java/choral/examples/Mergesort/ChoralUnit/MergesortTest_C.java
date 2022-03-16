package choral.examples.Mergesort.ChoralUnit;

import choral.choralUnit.annotations.Test;
import choral.lang.Unit;
import choral.choralUnit.testUtils.TestUtils_B;
import choral.channels.SymChannel_B;
import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.choralUnit.testUtils.TestUtils_A;
import choral.examples.Mergesort.Mergesort_C;

@Choreography( role = "C", name = "MergesortTest" )
public class MergesortTest_C {
	@Test
	public static void test1() {
		SymChannel_B< Object > ch_BC;
		ch_BC = TestUtils_B.newLocalChannel( Unit.id, "ch_BC" );
		SymChannel_A< Object > ch_CA;
		ch_CA = TestUtils_A.newLocalChannel( "ch_CA", Unit.id );
		new Mergesort_C( Unit.id, ch_BC, ch_CA ).sort( Unit.id );
	}

}
