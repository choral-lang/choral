package Benchmarks.Akka.DistAuth.DistAuthUtils;

import java.util.Optional;

public class AuthResult_A extends BiPair_A< Optional< AuthToken >, Optional< AuthToken > > {
	public AuthResult_A( AuthToken t1, Void t2 ) {
		this( t1 );
	}

	public AuthResult_A() {
		super( Optional.< AuthToken >empty(), null );
	}

	public AuthResult_A( AuthToken t1 ) {
		super( Optional.< AuthToken >of( t1 ), null );
	}

}
