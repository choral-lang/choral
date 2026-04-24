package choral.examples.Karatsuba.ChoralUnit;

import choral.choralUnit.testUtils.TestUtils_A;
import choral.annotations.Choreography;
import choral.lang.Unit;
import choral.choralUnit.annotations.Test;
import choral.channels.SymChannel_A;
import choral.examples.Karatsuba.Karatsuba_A;
import choral.channels.SymChannel_B;
import choral.choralUnit.testUtils.TestUtils_B;

import choral.choralUnit.Assert;

@Choreography(
		role = "A",
		name = "KaratsubaTest" )
public class KaratsubaTest_A {
	@Test
	public static void test1() {
		SymChannel_A< Object > ch_AB;
		ch_AB = TestUtils_A.newLocalChannel( "ch_AB", Unit.id );
		SymChannel_B< Object > ch_CA;
		ch_CA = TestUtils_B.newLocalChannel( Unit.id, "ch_CA" );
		Long result = Karatsuba_A.multiply( Long.valueOf( 15012 ), Long.valueOf( 153531 ), ch_AB,
				Unit.id, ch_CA );
		Assert.assertEquals( result, Long.valueOf( 15012L * 153531L ), "success", "failure" );
	}

}
