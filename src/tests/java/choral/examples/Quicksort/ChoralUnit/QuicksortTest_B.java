package choral.examples.Quicksort.ChoralUnit;
import org.choral.channels.SymChannel_A;
import org.choral.choralUnit.annotations.Test;
import org.choral.channels.SymChannel_B;
import org.choral.choralUnit.testUtils.TestUtils_B;
import org.choral.choralUnit.testUtils.TestUtils_A;
import org.choral.annotations.Choreography;
import choral.examples.Quicksort.Quicksort_B;
import org.choral.lang.Unit;

@Choreography( role = "B", name = "QuicksortTest" )
public class QuicksortTest_B {
	@Test
	public static void test1() {
		SymChannel_B < Object > ch_AB;
		ch_AB = TestUtils_B.newLocalChannel( Unit.id, "ch_AB" );
		SymChannel_A < Object > ch_BC;
		ch_BC = TestUtils_A.newLocalChannel( "ch_BC", Unit.id );
		new Quicksort_B( ch_AB, ch_BC, Unit.id ).sort( Unit.id );
	}

}