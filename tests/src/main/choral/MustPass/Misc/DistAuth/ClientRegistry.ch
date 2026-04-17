package choral.MustPass.DistAuth;

import java.util.Optional;

public class ClientRegistry@A {

	public static String@A getSalt ( String@A username ) { return username; }

	public static Optional@A< Profile > getProfile ( String@A hash ) { return null@A; }

    public static Boolean@A check ( String@A hash ) { return true@A;}
}
