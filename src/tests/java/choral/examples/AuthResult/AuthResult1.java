package choral.examples.AuthResult;
import choral.examples.BiPair.BiPair1;
import choral.examples.DistAuthUtils.AuthToken;
import java.util.Optional;
import org.choral.lang.Unit;
import org.choral.annotations.Choreography;

@Choreography( role = "A", name = "AuthResult" )
public class AuthResult1 extends BiPair1 < Optional < AuthToken >, Unit > {
	public AuthResult1( AuthToken t1, Unit t2 ) {
		this( t1 );
	}

	public AuthResult1() {
		super( Optional.< AuthToken >empty(), Unit.id );
	}

	public AuthResult1( AuthToken t1 ) {
		super( Optional.< AuthToken >of( t1 ), Unit.id );
	}

}
