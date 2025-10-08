package choral.examples.RetwisChoral;

import choral.runtime.Serializers.KryoSerializable;
import choral.annotations.Choreography;

@KryoSerializable
@Choreography( role = "R", name = "Result" )
public enum Result {
	OK,
	ERROR
}
