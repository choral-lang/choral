package choral.examples.Mergesort.ChoralUnit;
import choral.choralUnit.annotations.Test;
import choral.lang.Unit;
import choral.examples.Mergesort.Mergesort_A;
import choral.choralUnit.testUtils.TestUtils_B;
import choral.choralUnit.Assert;
import choral.channels.SymChannel_A;
import choral.annotations.Choreography;
import java.util.ArrayList;
import choral.channels.SymChannel_B;
import java.util.List;
import choral.choralUnit.testUtils.TestUtils_A;

@Choreography( role = "A", name = "MergesortTest" )
public class MergesortTest_A {
	@Test
	public static void test1() {
		ArrayList < Integer > a;
		a = new ArrayList < Integer >();
		a.add( 5 );
		a.add( 7 );
		a.add( 12 );
		a.add( 22 );
		a.add( 1 );
		a.add( 2 );
		a.add( 45 );
		SymChannel_A < Object > ch_AB;
		ch_AB = TestUtils_A.newLocalChannel( "ch_AB", Unit.id );
		SymChannel_B < Object > ch_CA;
		ch_CA = TestUtils_B.newLocalChannel( Unit.id, "ch_CA" );
		List < Integer > sortedList;
		sortedList = new Mergesort_A( ch_AB, Unit.id, ch_CA ).sort( a );
		Assert.assertEquals( sortedList.get( 0 ), 1, "success", "failure" );
	}

}
