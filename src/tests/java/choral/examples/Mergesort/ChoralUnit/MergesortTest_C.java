package choral.examples.Mergesort.ChoralUnit;
import org.choral.choralUnit.testUtils.TestUtils_A;
import org.choral.choralUnit.testUtils.TestUtils_B;
import choral.examples.Mergesort.Mergesort_C;
import org.choral.channels.SymChannel_A;
import org.choral.lang.Unit;
import org.choral.choralUnit.annotations.Test;
import org.choral.channels.SymChannel_B;
import org.choral.annotations.Choreography;

@Choreography( role = "C", name = "MergesortTest" )
public class MergesortTest_C {
	@Test
	public static void test1() {
		SymChannel_B < Object > ch_BC;
		ch_BC = TestUtils_B.newLocalChannel( Unit.id, "ch_BC" );
		SymChannel_A < Object > ch_CA;
		ch_CA = TestUtils_A.newLocalChannel( "ch_CA", Unit.id );
		new Mergesort_C( Unit.id, ch_BC, ch_CA ).sort( Unit.id );
	}

}
