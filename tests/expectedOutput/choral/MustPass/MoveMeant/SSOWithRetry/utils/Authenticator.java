package choral.MustPass.MoveMeant.SSOWithRetry.utils;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Authenticator" )
public class Authenticator {
	public boolean valid( Creds x ) {
		return true;
	}

}
