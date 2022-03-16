package choral.examples.Quicksort.ChoralUnit;

import choral.lang.Unit;
import choral.choralUnit.annotations.Test;
import choral.annotations.Choreography;

import java.util.ArrayList;

import choral.choralUnit.testUtils.TestUtils_B;
import choral.choralUnit.testUtils.TestUtils_A;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.examples.Quicksort.Quicksort_A;

@Choreography( role = "A", name = "QuicksortTest" )
public class QuicksortTest_A {
	@Test
	public static void test1() {
		ArrayList< Integer > a;
		a = new ArrayList< Integer >();
		a.add( 5 );
		a.add( 7 );
		a.add( 12 );
		a.add( 22 );
		a.add( 1 );
		a.add( 2 );
		a.add( 45 );
		SymChannel_A< Object > ch_AB;
		ch_AB = TestUtils_A.newLocalChannel( "ch_AB", Unit.id );
		SymChannel_B< Object > ch_CA;
		ch_CA = TestUtils_B.newLocalChannel( Unit.id, "ch_CA" );
		System.out.println( new Quicksort_A( ch_AB, Unit.id, ch_CA ).sort( a ) );
	}

}
