package choral.examples.RetwisChoral;
import choral.annotations.Choreography;
import choral.runtime.Serializers.KryoSerializable;

@Choreography( role = "R", name = "RetwisAction" )
@KryoSerializable
public enum RetwisAction {
	POSTS, POST, FOLLOW, STOPFOLLOW, MENTIONS, STATUS, LOGOUT
}
