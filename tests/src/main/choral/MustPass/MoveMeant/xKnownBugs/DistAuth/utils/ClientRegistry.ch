package choral.MustPass.MoveMeant.DistributedAuthentication.utils;

import java.util.Optional;

public class ClientRegistry@A {

	public static String@A getSalt ( String@A username ) { return username; }

	public static Optional@A< Profile > getProfile ( String@A hash ) { return Optional@A.< Profile >empty(); }

    public static Boolean@A check ( String@A hash ) { return true@A; }
}
