package choral.MustPass.DistAuth;

import java.util.Optional;
import choral.annotations.Choreography;

@Choreography( role = "A", name = "ClientRegistry" )
public class ClientRegistry {
    public static String getSalt ( String username ) {
        return username + "salt";
    }

	public static Optional< Profile > getProfile ( String hash ) {
        return Optional.of(new Profile(hash));
    }

    public static Boolean check ( String hash ) {
        return hash.isEmpty();
    }
}
