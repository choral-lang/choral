package choral.examples.Karatsuba.ChoralUnit;

import choral.examples.Karatsuba.Karatsuba1;
import choral.examples.Karatsuba.Karatsuba2;
import choral.examples.Karatsuba.Karatsuba3;
import org.choral.annotations.Choreography;
import org.choral.choralUnit.testUtils.TestUtils1;
import org.choral.choralUnit.testUtils.TestUtils2;
import org.choral.lang.Channels.SymChannel1;
import org.choral.lang.Channels.SymChannel2;
import org.choral.choralUnit.annotations.Test;

public class KaratsubaTest@( A, B, C ) {

	@Test
	public static void test1(){
		SymChannel@( A, B )< Object > ch_AB = TestUtils@( A, B ).newLocalChannel( "ch_AB"@[ A, B ] );
		SymChannel@( B, C )< Object > ch_BC = TestUtils@( B, C ).newLocalChannel( "ch_BC"@[ B, C ] );
		SymChannel@( C, A )< Object > ch_CA = TestUtils@( C, A ).newLocalChannel( "ch_CA"@[ C, A ] );
		System@A.out.println( Karatsuba@( A, B, C )
			.multiply( Long@A.valueOf( 15012@A ), Long@A.valueOf( 153531@A ), ch_AB, ch_BC, ch_CA ) );
	}

}
