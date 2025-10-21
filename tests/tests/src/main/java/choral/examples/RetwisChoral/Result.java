package choral.examples.RetwisChoral;

import choral.annotations.Choreography;
import choral.runtime.Serializers.KryoSerializable;

@KryoSerializable
@Choreography( role = "R", name = "Result" )
public enum Result {
	OK,
	ERROR
}
