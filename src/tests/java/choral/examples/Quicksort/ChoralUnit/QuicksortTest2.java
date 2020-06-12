package choral.examples.Quicksort.ChoralUnit;
import choral.examples.Quicksort.Quicksort2;
import org.choral.annotations.Choreography;
import org.choral.choralUnit.testUtils.TestUtils1;
import org.choral.choralUnit.testUtils.TestUtils2;
import org.choral.lang.Channels.SymChannel1;
import org.choral.lang.Channels.SymChannel2;
import org.choral.choralUnit.annotations.Test;
import org.choral.lang.Unit;

@Choreography( role = "B", name = "QuicksortTest" )
public class QuicksortTest2 {
	@Test
	public static void test1() {
		SymChannel2 < Object > ch_AB;
		ch_AB = TestUtils2.newLocalChannel( Unit.id, "ch_AB" );
		SymChannel1 < Object > ch_BC;
		ch_BC = TestUtils1.newLocalChannel( "ch_BC", Unit.id );
		new Quicksort2( ch_AB, ch_BC, Unit.id ).sort( Unit.id );
	}

}
