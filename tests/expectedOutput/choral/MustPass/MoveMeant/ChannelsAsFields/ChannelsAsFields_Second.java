package choral.MustPass.MoveMeant.ChannelsAsFields;

import choral.MustPass.MoveMeant.ChannelsAsFields.utils.Client;
import choral.annotations.Choreography;
import choral.channels.DiDataChannel_B;
import choral.channels.DiSelectChannel_B;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "Second", name = "ChannelsAsFields" )
public class ChannelsAsFields_Second {
	SymChannel_B < Object > ch_AB;
	DiDataChannel_B < Object > diData;
	DiSelectChannel_B diSelect;
	String var2;

	ChannelsAsFields_Second( SymChannel_B < Object > ch_AB, Unit ch_AC ) {
		this( ch_AB );
	}
	
	ChannelsAsFields_Second( SymChannel_B < Object > ch_AB ) {
		this.ch_AB = ch_AB;
	}

	public static void fun( Unit c_A, Client c_B ) {
		fun( c_B );
	}
	
	private void helper( Unit in_A, Integer in_B ) {
		
	}
	
	public static void fun( Client c_B ) {
		String dependencyAtSecond_1949594459 = ch_AB.< String >com( Unit.id );
		String s_B = dependencyAtSecond_1949594459;
		Integer i_B = 0;
		ch_AB.< Integer >com( i_B );
		ch_AB.< Integer >com( c_B.fun_out() );
		ch_AB.< String >com( c_B.price.currency );
		helper( Unit.id, i_B );
		helper( Unit.id, 0 );
	}

}
