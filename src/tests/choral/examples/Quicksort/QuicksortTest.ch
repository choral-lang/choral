package choral.examples.Quicksort.ChoralUnit;

import choral.examples.Quicksort.Quicksort1;
import choral.examples.Quicksort.Quicksort2;
import choral.examples.Quicksort.Quicksort3;
import org.choral.annotations.Choreography;
import org.choral.choralUnit.testUtils.TestUtils1;
import org.choral.choralUnit.testUtils.TestUtils2;
import org.choral.lang.Channels.SymChannel1;
import org.choral.lang.Channels.SymChannel2;
import org.choral.choralUnit.annotations.Test;
import java.util.ArrayList;

public class QuicksortTest@( A, B, C ) {

	@Test
	public static void test1(){
		ArrayList@A< Integer > a = new ArrayList@A< Integer >();
		a.add( 5@A ); a.add( 7@A ); a.add( 12@A ); a.add( 22@A ); a.add( 1@A ); a.add( 2@A ); a.add( 45@A );
		SymChannel@( A, B )< Object > ch_AB = TestUtils@( A, B ).newLocalChannel( "ch_AB"@[ A, B ] );
		SymChannel@( B, C )< Object > ch_BC = TestUtils@( B, C ).newLocalChannel( "ch_BC"@[ B, C ] );
		SymChannel@( C, A )< Object > ch_CA = TestUtils@( C, A ).newLocalChannel( "ch_CA"@[ C, A ] );
		new Quicksort@( A, B, C )( ch_AB, ch_BC, ch_CA ).sort( a )
			>> System@A.out::println;
	}

}
