package choral.examples.Karatsuba.ChoralUnit;
import org.choral.lang.Unit;
import choral.examples.Karatsuba.Karatsuba_C;
import org.choral.choralUnit.annotations.Test;
import org.choral.channels.SymChannel_B;
import org.choral.choralUnit.testUtils.TestUtils_B;
import org.choral.channels.SymChannel_A;
import org.choral.choralUnit.testUtils.TestUtils_A;
import org.choral.annotations.Choreography;

@Choreography( role = "C", name = "KaratsubaTest" )
public class KaratsubaTest_C {
	@Test
	public static void test1() {
		SymChannel_B < Object > ch_BC;
		ch_BC = TestUtils_B.newLocalChannel( Unit.id, "ch_BC" );
		SymChannel_A < Object > ch_CA;
		ch_CA = TestUtils_A.newLocalChannel( "ch_CA", Unit.id );
		Karatsuba_C.multiply( Unit.id, Unit.id, Unit.id, ch_BC, ch_CA );
	}

}
