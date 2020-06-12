package choral.examples.AuthResult;
import choral.examples.BiPair.BiPair2;
import choral.examples.DistAuthUtils.AuthToken;
import java.util.Optional;
import org.choral.lang.Unit;
import org.choral.annotations.Choreography;

@Choreography( role = "B", name = "AuthResult" )
public class AuthResult2 extends BiPair2 < Unit, Optional < AuthToken > > {
	public AuthResult2( Unit t1, AuthToken t2 ) {
		this( t2 );
	}

	public AuthResult2() {
		super( Unit.id, Optional.< AuthToken >empty() );
	}

	public AuthResult2( AuthToken t2 ) {
		super( Unit.id, Optional.< AuthToken >of( t2 ) );
	}

}
