package choral.examples.Mergesort.ChoralUnit;
import choral.examples.Mergesort.Mergesort3;
import org.choral.annotations.Choreography;
import org.choral.choralUnit.testUtils.TestUtils1;
import org.choral.choralUnit.testUtils.TestUtils2;
import org.choral.lang.Channels.SymChannel1;
import org.choral.lang.Channels.SymChannel2;
import org.choral.choralUnit.annotations.Test;
import org.choral.lang.Unit;

@Choreography( role = "C", name = "MergesortTest" )
public class MergesortTest3 {
	@Test
	public static void test1() {
		SymChannel2 < Object > ch_BC;
		ch_BC = TestUtils2.newLocalChannel( Unit.id, "ch_BC" );
		SymChannel1 < Object > ch_CA;
		ch_CA = TestUtils1.newLocalChannel( "ch_CA", Unit.id );
		new Mergesort3( Unit.id, ch_BC, ch_CA ).sort( Unit.id );
	}

}
