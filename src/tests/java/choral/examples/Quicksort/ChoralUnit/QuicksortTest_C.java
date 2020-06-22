package choral.examples.Quicksort.ChoralUnit;
import org.choral.choralUnit.annotations.Test;
import org.choral.choralUnit.testUtils.TestUtils_B;
import org.choral.channels.SymChannel_B;
import choral.examples.Quicksort.Quicksort_C;
import org.choral.channels.SymChannel_A;
import org.choral.annotations.Choreography;
import org.choral.choralUnit.testUtils.TestUtils_A;
import org.choral.lang.Unit;

@Choreography( role = "C", name = "QuicksortTest" )
public class QuicksortTest_C {
	@Test
	public static void test1() {
		SymChannel_B < Object > ch_BC;
		ch_BC = TestUtils_B.newLocalChannel( Unit.id, "ch_BC" );
		SymChannel_A < Object > ch_CA;
		ch_CA = TestUtils_A.newLocalChannel( "ch_CA", Unit.id );
		new Quicksort_C( Unit.id, ch_BC, ch_CA ).sort( Unit.id );
	}

}
