package choral.MustPass.DistAuth;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "Profile" )
public class Profile {
    private String id;

    public Profile ( String id ) {
        this.id = id;
	}

	public String id () {
        return id;
	}
}
