package choral.examples.DistAuth5;
import choral.annotations.Choreography;
import choral.runtime.Serializers.KryoSerializable;

@KryoSerializable
@Choreography( role = "A", name = "EnumBoolean" )
enum EnumBoolean {
	True, False
}
