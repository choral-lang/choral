package choral.MustPass.DistAuthUtils;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "AuthToken" )
public class AuthToken {
	private String id;

	public AuthToken( String id ) {
		this.id = id;
	}

	public String id() {
		return id;
	}

}
