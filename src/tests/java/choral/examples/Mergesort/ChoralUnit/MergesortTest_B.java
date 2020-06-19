package choral.examples.Mergesort.ChoralUnit;
import org.choral.channels.SymChannel_B;
import org.choral.lang.Unit;
import org.choral.annotations.Choreography;
import org.choral.choralUnit.annotations.Test;
import org.choral.channels.SymChannel_A;
import org.choral.choralUnit.testUtils.TestUtils_A;
import org.choral.choralUnit.testUtils.TestUtils_B;
import choral.examples.Mergesort.Mergesort_B;

@Choreography( role = "B", name = "MergesortTest" )
public class MergesortTest_B {
	@Test
	public static void test1() {
		SymChannel_B < Object > ch_AB;
		ch_AB = TestUtils_B.newLocalChannel( Unit.id, "ch_AB" );
		SymChannel_A < Object > ch_BC;
		ch_BC = TestUtils_A.newLocalChannel( "ch_BC", Unit.id );
		new Mergesort_B( ch_AB, ch_BC, Unit.id ).sort( Unit.id );
	}

}
