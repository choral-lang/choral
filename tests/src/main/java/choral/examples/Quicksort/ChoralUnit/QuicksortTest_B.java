package choral.examples.Quicksort.ChoralUnit;

import choral.channels.SymChannel_A;
import choral.lang.Unit;
import choral.choralUnit.annotations.Test;
import choral.choralUnit.testUtils.TestUtils_A;
import choral.annotations.Choreography;
import choral.channels.SymChannel_B;
import choral.choralUnit.testUtils.TestUtils_B;
import choral.examples.Quicksort.Quicksort_B;

@Choreography( role = "B", name = "QuicksortTest" )
public class QuicksortTest_B {
	@Test
	public static void test1() {
		SymChannel_B< Object > ch_AB;
		ch_AB = TestUtils_B.newLocalChannel( Unit.id, "ch_AB" );
		SymChannel_A< Object > ch_BC;
		ch_BC = TestUtils_A.newLocalChannel( "ch_BC", Unit.id );
		new Quicksort_B( ch_AB, ch_BC, Unit.id ).sort( Unit.id );
	}

}
