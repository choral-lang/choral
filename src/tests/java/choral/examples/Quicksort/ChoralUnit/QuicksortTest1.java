package choral.examples.Quicksort.ChoralUnit;
import choral.examples.Quicksort.Quicksort1;
import org.choral.annotations.Choreography;
import org.choral.choralUnit.testUtils.TestUtils1;
import org.choral.choralUnit.testUtils.TestUtils2;
import org.choral.lang.Channels.SymChannel1;
import org.choral.lang.Channels.SymChannel2;
import org.choral.choralUnit.annotations.Test;
import java.util.ArrayList;
import org.choral.lang.Unit;

@Choreography( role = "A", name = "QuicksortTest" )
public class QuicksortTest1 {
	@Test
	public static void test1() {
		ArrayList < Integer > a;
		a = new ArrayList< Integer >();
		a.add( 5 );
		a.add( 7 );
		a.add( 12 );
		a.add( 22 );
		a.add( 1 );
		a.add( 2 );
		a.add( 45 );
		SymChannel1 < Object > ch_AB;
		ch_AB = TestUtils1.newLocalChannel( "ch_AB", Unit.id );
		SymChannel2 < Object > ch_CA;
		ch_CA = TestUtils2.newLocalChannel( Unit.id, "ch_CA" );
		System.out.println( new Quicksort1( ch_AB, Unit.id, ch_CA ).sort( a ) );
	}

}
