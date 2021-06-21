package choral.examples.RetwisChoral;
import choral.annotations.Choreography;
import choral.runtime.Serializers.KryoSerializable;

@Choreography( role = "R", name = "LoginAction" )
@KryoSerializable
public enum LoginAction {
	SIGNIN, SIGNUP, LOGOUT
}
