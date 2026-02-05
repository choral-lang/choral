package choral.MustPass.MoveMeant.RemoteFunction;

import choral.annotations.Choreography;
import choral.channels.BiDataChannel_A;
import choral.lang.Unit;

@Choreography( role = "Client", name = "RemoteFunction" )
class RemoteFunction_Client< T, R > {
	private BiDataChannel_A < T, R > ch;

	public RemoteFunction_Client( BiDataChannel_A < T, R > ch, Unit f ) {
		this( ch );
	}
	
	public RemoteFunction_Client( BiDataChannel_A < T, R > ch ) {
		this.ch = ch;
	}

	public R call( T t ) {
		ch.< T >com( t );
		R dependencyAtClient_2066044953 = ch.< R >com( Unit.id );
		return dependencyAtClient_2066044953;
	}

}
