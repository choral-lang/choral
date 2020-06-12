package choral.examples.Karatsuba.ChoralUnit;
import choral.examples.Karatsuba.Karatsuba1;
import org.choral.annotations.Choreography;
import org.choral.choralUnit.testUtils.TestUtils1;
import org.choral.choralUnit.testUtils.TestUtils2;
import org.choral.lang.Channels.SymChannel1;
import org.choral.lang.Channels.SymChannel2;
import org.choral.choralUnit.annotations.Test;
import org.choral.lang.Unit;

@Choreography( role = "A", name = "KaratsubaTest" )
public class KaratsubaTest1 {
	@Test
	public static void test1() {
		SymChannel1 < Object > ch_AB;
		ch_AB = TestUtils1.newLocalChannel( "ch_AB", Unit.id );
		SymChannel2 < Object > ch_CA;
		ch_CA = TestUtils2.newLocalChannel( Unit.id, "ch_CA" );
		System.out.println( Karatsuba1.multiply( Long.valueOf( 15012 ), Long.valueOf( 153531 ), ch_AB, Unit.id, ch_CA ) );
	}

}
