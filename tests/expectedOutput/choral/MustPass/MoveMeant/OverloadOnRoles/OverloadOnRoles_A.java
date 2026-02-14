package choral.MustPass.MoveMeant.OverloadOnRoles;

import choral.annotations.Choreography;
import choral.channels.SymChannel_A;
import choral.lang.Unit;
import java.lang.Integer;
import java.lang.Object;

@Choreography( role = "A", name = "OverloadOnRoles" )
public class OverloadOnRoles_A {
	SymChannel_A < Object > ch_AB;
	SymChannel_A < Object > ch_AC;

	public void fun( Client_A client ) {
		Integer I_A = 0;
		client.fun( I_A );
		client.fun( Unit.id );
		client.fun2( I_A, I_A );
		client.fun2( I_A, Unit.id );
		Integer msg0 = ch_AB.< Integer >com( Unit.id );
		client.fun2( msg0, I_A );
		Integer msg1 = ch_AC.< Integer >com( Unit.id );
		client.fun2( msg1, Unit.id );
	}

}
