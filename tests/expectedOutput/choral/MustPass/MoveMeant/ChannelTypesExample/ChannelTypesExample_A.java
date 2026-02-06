package choral.MustPass.MoveMeant.ChannelTypesExample;

import choral.annotations.Choreography;
import choral.channels.DiDataChannel_A;
import choral.channels.DiSelectChannel_A;
import choral.channels.SymChannel_A;

@Choreography( role = "A", name = "ChannelTypesExample" )
public class ChannelTypesExample_A {
	SymChannel_A < String > ch_AB_S;
	SymChannel_A < Number > ch_AB_N;
	DiDataChannel_A < String > diData_S;
	DiSelectChannel_A diSelect;
	SymChannel_A < Object > ch_AB_O;

	ChannelTypesExample_A( SymChannel_A < Object > ch_AB_O, SymChannel_A < String > ch_AB_S, SymChannel_A < Number > ch_AB_N ) {
		this.ch_AB_O = ch_AB_O;
		this.ch_AB_S = ch_AB_S;
		this.ch_AB_N = ch_AB_N;
	}

	public void fun() {
		String s_A = "A";
		ch_AB_S.< String >com( s_A );
		Integer i_A = 0;
		ch_AB_N.< Integer >com( i_A );
		Object o_A = new Object();
		ch_AB_O.< Object >com( o_A );
	}

}
