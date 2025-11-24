package choral.examples.Quicksort.ChoralUnit;

import choral.examples.Quicksort.Quicksort;
import choral.choralUnit.testUtils.TestUtils;
import choral.channels.SymChannel;
import choral.choralUnit.annotations.Test;
import java.util.ArrayList;

public class QuicksortTest@( A, B, C ) {

	@Test
	public static void test1(){
		ArrayList@A< Integer > a = new ArrayList@A< Integer >();
		a.add( 5@A ); a.add( 7@A ); a.add( 12@A ); a.add( 22@A ); a.add( 1@A ); a.add( 2@A ); a.add( 45@A );
		SymChannel@( A, B )< Object > ch_AB = TestUtils@( A, B ).newLocalChannel( "ch_AB"@[ A, B ] );
		SymChannel@( B, C )< Object > ch_BC = TestUtils@( B, C ).newLocalChannel( "ch_BC"@[ B, C ] );
		SymChannel@( C, A )< Object > ch_CA = TestUtils@( C, A ).newLocalChannel( "ch_CA"@[ C, A ] );
		Assert@A.assertEquals(
			new Quicksort@( A, B, C )( ch_AB, ch_BC, ch_CA ).sort( a ),
			[1@A, 2@A, 5@A, 7@A, 12@A, 22@A, 45@A ],
			"success"@A,
			"failure"@A
		);
	}

}
