package choral.examples.Karatsuba.ChoralUnit;
import org.choral.lang.Unit;
import org.choral.choralUnit.testUtils.TestUtils_A;
import org.choral.channels.SymChannel_B;
import org.choral.choralUnit.annotations.Test;
import org.choral.channels.SymChannel_A;
import choral.examples.Karatsuba.Karatsuba_B;
import org.choral.choralUnit.testUtils.TestUtils_B;
import org.choral.annotations.Choreography;

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
