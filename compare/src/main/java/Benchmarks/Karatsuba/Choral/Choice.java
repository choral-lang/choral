package Benchmarks.Karatsuba.Choral;
import choral.annotations.Choreography;
import choral.runtime.Serializers.KryoSerializable;

@Choreography( role = "R", name = "Choice" )
@KryoSerializable
enum Choice {
	RECUR, DONE
}
