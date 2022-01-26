package choral.examples.AuthResult;

import choral.annotations.Choreography;

import java.util.Optional;

import choral.examples.BiPair.BiPair_B;
import choral.examples.DistAuthUtils.AuthToken;
import choral.lang.Unit;

@Choreography( role = "B", name = "AuthResult" )
public class AuthResult_B extends BiPair_B< Optional< AuthToken >, Optional< AuthToken > > {
	public AuthResult_B( Unit t1, AuthToken t2 ) {
		this( t2 );
	}

	public AuthResult_B() {
		super( Unit.id, Optional.< AuthToken >empty() );
	}

	public AuthResult_B( AuthToken t2 ) {
		super( Unit.id, Optional.< AuthToken >of( t2 ) );
	}

}
