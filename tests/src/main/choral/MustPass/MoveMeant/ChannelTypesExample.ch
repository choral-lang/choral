package choral.MustPass.MoveMeant.ChannelTypesExample;

import choral.channels.SymChannel;
import choral.channels.DiDataChannel;
import choral.channels.BiDataChannel;
import choral.channels.DiSelectChannel;

public class ChannelTypesExample@( A, B ){

	// BiDataChannel@( A, B )< String, Number > biData_SN; // illegal channel
	SymChannel@( A, B )< String > ch_AB_S;
	SymChannel@( A, B )< Number > ch_AB_N;
	DiDataChannel@( A, B )< String > diData_S;
	DiSelectChannel@( A, B ) diSelect;
	SymChannel@( A, B )< Object > ch_AB_O;

	ChannelTypesExample( 
        SymChannel@( A, B )< Object > ch_AB_O,
        SymChannel@( A, B )< String > ch_AB_S,
        SymChannel@( A, B )< Number > ch_AB_N ) {
		this.ch_AB_O = ch_AB_O;
		this.ch_AB_S = ch_AB_S;
		this.ch_AB_N = ch_AB_N;
	}
    public void fun(  ) {

        String@A s_A = "A"@A;
		String@B s_B = s_A;
		Integer@A i_A = 0@A;
		Integer@B i_B = i_A;
        Object@A o_A = new Object@A();
        Object@B o_B = o_A;

    }

}