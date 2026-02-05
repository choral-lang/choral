package choral.MustPass.MoveMeant.ChannelsAsFields;

import choral.MustPass.MoveMeant.ChannelsAsFields.utils.Client;
import choral.annotations.Choreography;
import choral.channels.DiDataChannel_A;
import choral.channels.DiSelectChannel_A;
import choral.channels.SymChannel_A;
import choral.lang.Unit;

@Choreography( role = "First", name = "ChannelsAsFields" )
public class ChannelsAsFields_First {
	SymChannel_A < Object > ch_AB;
	SymChannel_A < Object > ch_AC;
	DiDataChannel_A < Object > diData;
	DiSelectChannel_A diSelect;
	int var;

	ChannelsAsFields_First( SymChannel_A < Object > ch_AB, SymChannel_A < Object > ch_AC ) {
		this.ch_AB = ch_AB;
		this.ch_AC = ch_AC;
	}

	public static void fun( Client c_A, Unit c_B ) {
		fun( c_A );
	}
	
	private void helper( Integer in_A, Unit in_B ) {
		
	}
	
	public static void fun( Client c_A ) {
		String s_A = "A";
		ch_AB.< String >com( s_A );
		Integer i_A = 0;
		c_A.fun0();
		c_A.fun_in( i_A );
		Integer dependencyAtFirst_1748674892 = ch_AB.< Integer >com( Unit.id );
		c_A.fun_in( dependencyAtFirst_1748674892 );
		c_A.fun_in( c_A.fun_out() );
		Integer dependencyAtFirst_291864914 = ch_AB.< Integer >com( Unit.id );
		c_A.fun_in( dependencyAtFirst_291864914 );
		String dependencyAtFirst_1450700441 = ch_AB.< String >com( Unit.id );
		c_A.fun_in( dependencyAtFirst_1450700441 );
		helper( i_A, Unit.id );
		helper( 0, Unit.id );
	}

}
