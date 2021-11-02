package DistAuth.DistAuthUtils;

import java.util.Optional;

public class AuthResult_B extends BiPair_B< Optional < AuthToken >, Optional < AuthToken > > {
	public AuthResult_B( Void t1, AuthToken t2 ) {
		this( t2 );
	}

	public AuthResult_B() {
		super( null, Optional.< AuthToken >empty() );
	}

	public AuthResult_B( AuthToken t2 ) {
		super( null, Optional.< AuthToken >of( t2 ) );
	}

}
