package choral.MustPass.MoveMeant.OverloadOnRoles;

import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.channels.SymChannel_B;
import choral.lang.Unit;
import java.lang.Integer;
import java.lang.Object;

@Choreography( role = "C", name = "OverloadOnRoles" )
public class OverloadOnRoles_C {
	SymChannel_B < Object > ch_AC;
	SymChannel_A < Object > ch_CB;

	public void fun( Unit client ) {
		fun();
	}
	
	public void fun() {
		Integer I_C = 0;
		ch_AC.< Integer >com( I_C );
	}

}
