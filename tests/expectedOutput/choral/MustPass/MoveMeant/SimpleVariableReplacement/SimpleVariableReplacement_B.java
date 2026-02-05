package choral.MustPass.MoveMeant.SimpleVariableReplacement;

import choral.annotations.Choreography;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "B", name = "SimpleVariableReplacement" )
class SimpleVariableReplacement_B {
	public static void fun( SymChannel_B < Object > ch_AB ) {
		Integer dependencyAtB_1200280085 = ch_AB.< Integer >com( Unit.id );
		Integer dependencyAtB_1843264230 = ch_AB.< Integer >com( Unit.id );
		Integer IB = dependencyAtB_1200280085 + dependencyAtB_1843264230 + 1;
		Integer I2B = dependencyAtB_1200280085;
	}

}
