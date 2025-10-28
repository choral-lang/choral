package choral.MustPass.RetwisChoral;

import choral.runtime.Serializers.KryoSerializable;
import choral.annotations.Choreography;

@KryoSerializable
@Choreography( role = "R", name = "RetwisAction" )
public enum RetwisAction {
	POSTS,
	POST,
	FOLLOW,
	STOPFOLLOW,
	MENTIONS,
	STATUS,
	LOGOUT
}
