package choral.MustPass.DistAuth;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Credentials" )
public class Credentials {
	public String username;
	public String password;

	public Credentials( String username, String password ) {
		
	}

}
