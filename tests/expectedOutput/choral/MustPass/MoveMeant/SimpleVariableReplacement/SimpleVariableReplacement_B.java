package choral.MustPass.MoveMeant.SimpleVariableReplacement;

import choral.annotations.Choreography;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "B", name = "SimpleVariableReplacement" )
class SimpleVariableReplacement_B {
	public static void fun( SymChannel_B < Object > ch_AB ) {
		Integer msg0 = ch_AB.< Integer >com( Unit.id );
		Integer msg1 = ch_AB.< Integer >com( Unit.id );
		Integer IB = msg0 + msg1 + 1;
		Integer I2B = msg0;
	}

}
