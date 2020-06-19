package choral.examples.Mergesort.ChoralUnit;

import choral.examples.Mergesort.Mergesort;
import org.choral.choralUnit.testUtils.TestUtils;
import org.choral.channels.SymChannel;
import org.choral.choralUnit.annotations.Test;
import java.util.ArrayList;
import java.util.List;
import org.choral.choralUnit.Assert;

public class MergesortTest@( A, B, C ) {

	@Test
	public static void test1(){
		ArrayList@A< Integer > a = new ArrayList@A< Integer >();
		a.add( 5@A ); a.add( 7@A ); a.add( 12@A ); a.add( 22@A ); a.add( 1@A ); a.add( 2@A ); a.add( 45@A );
		SymChannel@( A, B )< Object > ch_AB = TestUtils@( A, B ).newLocalChannel( "ch_AB"@[ A, B ] );
		SymChannel@( B, C )< Object > ch_BC = TestUtils@( B, C ).newLocalChannel( "ch_BC"@[ B, C ] );
		SymChannel@( C, A )< Object > ch_CA = TestUtils@( C, A ).newLocalChannel( "ch_CA"@[ C, A ] );
		List@A< Integer > sortedList = new Mergesort@( A, B, C )( ch_AB, ch_BC, ch_CA ).sort( a );
		Assert@A.assertEquals( sortedList.get( 0@A ), 1@A, "success"@A, "failure"@A );
	}

}
