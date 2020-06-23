package choral.examples.Quicksort.ChoralUnit;
import java.util.ArrayList;
import org.choral.choralUnit.annotations.Test;
import org.choral.choralUnit.testUtils.TestUtils_B;
import org.choral.choralUnit.testUtils.TestUtils_A;
import choral.examples.Quicksort.Quicksort_A;
import org.choral.channels.SymChannel_B;
import org.choral.annotations.Choreography;
import org.choral.channels.SymChannel_A;
import org.choral.lang.Unit;

@Choreography( role = "A", name = "QuicksortTest" )
public class QuicksortTest_A {
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
		System.out.println( new Quicksort_A( ch_AB, Unit.id, ch_CA ).sort( a ) );
	}

}
