package choral.examples.AuthResult;
import choral.examples.BiPair.BiPair_A;
import choral.examples.DistAuthUtils.AuthToken;
import java.util.Optional;
import org.choral.lang.Unit;
import org.choral.annotations.Choreography;

@Choreography( role = "A", name = "AuthResult" )
public class AuthResult_A extends BiPair_A < Optional < AuthToken >, Unit > {
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
