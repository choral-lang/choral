package choral.MustPass.MoveMeant.ChannelTypesExample;

import choral.annotations.Choreography;
import choral.channels.DiDataChannel_B;
import choral.channels.DiSelectChannel_B;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "B", name = "ChannelTypesExample" )
public class ChannelTypesExample_B {
	SymChannel_B < String > ch_AB_S;
	SymChannel_B < Number > ch_AB_N;
	DiDataChannel_B < String > diData_S;
	DiSelectChannel_B diSelect;
	SymChannel_B < Object > ch_AB_O;

	ChannelTypesExample_B( SymChannel_B < Object > ch_AB_O, SymChannel_B < String > ch_AB_S, SymChannel_B < Number > ch_AB_N ) {
		this.ch_AB_O = ch_AB_O;
		this.ch_AB_S = ch_AB_S;
		this.ch_AB_N = ch_AB_N;
	}

	public static void fun() {
		String dependencyAtB_226306607 = ch_AB_S.< String >com( Unit.id );
		String s_B = dependencyAtB_226306607;
		Integer dependencyAtB_1763930981 = ch_AB_N.< Integer >com( Unit.id );
		Integer i_B = dependencyAtB_1763930981;
		Object dependencyAtB_1908651707 = ch_AB_O.< Object >com( Unit.id );
		Object o_B = dependencyAtB_1908651707;
	}

}
