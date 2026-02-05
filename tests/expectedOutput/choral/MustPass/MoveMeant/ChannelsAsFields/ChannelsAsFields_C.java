package choral.MustPass.MoveMeant.ChannelsAsFields;

import choral.annotations.Choreography;
import choral.channels.SymChannel_B;
import choral.lang.Unit;

@Choreography( role = "C", name = "ChannelsAsFields" )
public class ChannelsAsFields_C {
	SymChannel_B < Object > ch_AC;
	Object var3;

	ChannelsAsFields_C( Unit ch_AB, SymChannel_B < Object > ch_AC ) {
		this( ch_AC );
	}
	
	ChannelsAsFields_C( SymChannel_B < Object > ch_AC ) {
		this.ch_AC = ch_AC;
	}

	public static void fun( Unit c_A, Unit c_B ) {
		fun();
	}
	
	private void helper( Unit in_A, Unit in_B ) {
		
	}
	
	public static void fun() {
		helper( Unit.id, Unit.id );
		helper( Unit.id, Unit.id );
	}

}
