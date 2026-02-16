package choral.MustPass.MoveMeant.SimpleVariableReplacement;

import choral.annotations.Choreography;
import choral.channels.SymChannel_A;

@Choreography( role = "A", name = "SimpleVariableReplacement" )
class SimpleVariableReplacement_A {
	public static void fun( SymChannel_A < Object > ch_AB ) {
		Integer IA = 0;
		Integer I2A = 0;
		ch_AB.< Integer >com( IA );
		ch_AB.< Integer >com( I2A );
	}

}
