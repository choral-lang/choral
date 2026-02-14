package choral.MustPass.MoveMeant.OverloadOnRoles;

import choral.annotations.Choreography;
import choral.channels.SymChannel_B;
import choral.lang.Unit;
import java.lang.Integer;
import java.lang.Object;

@Choreography( role = "B", name = "OverloadOnRoles" )
public class OverloadOnRoles_B {
	SymChannel_B < Object > ch_AB;
	SymChannel_B < Object > ch_CB;

	public void fun( Client_B client ) {
		Integer I_B = 0;
		client.fun( Unit.id );
		client.fun( I_B );
		client.fun2( Unit.id, Unit.id );
		client.fun2( Unit.id, I_B );
		ch_AB.< Integer >com( I_B );
		client.fun2( Unit.id, Unit.id );
		client.fun2( Unit.id, I_B );
	}

}
