package choral.MustPass.DistAuth;

import choral.annotations.Choreography;
import java.util.Optional;

@Choreography( role = "A", name = "ClientRegistry" )
public class ClientRegistry {
	public static String getSalt( String username ) {
		return username;
	}
	
	public static Optional < Profile > getProfile( String hash ) {
		return null;
	}
	
	public static Boolean check( String hash ) {
		return true;
	}

}
