package choral.examples.AuthResult;

import java.util.Optional;

import choral.annotations.Choreography;
import choral.examples.BiPair.BiPair_A;
import choral.examples.DistAuthUtils.AuthToken;
import choral.lang.Unit;

@Choreography( role = "A", name = "AuthResult" )
public class AuthResult_A extends BiPair_A< Optional< AuthToken >, Optional< AuthToken > > {
	public AuthResult_A( AuthToken t1, Unit t2 ) {
		this( t1 );
	}

	public AuthResult_A() {
		super( Optional.< AuthToken >empty(), Unit.id );
	}

	public AuthResult_A( AuthToken t1 ) {
		super( Optional.< AuthToken >of( t1 ), Unit.id );
	}

}
