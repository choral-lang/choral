package choral.examples.DistAuth5;
import org.choral.annotations.Choreography;
import org.choral.runtime.Serializers.KryoSerializable;

@KryoSerializable
@Choreography( role = "A", name = "EnumBoolean" )
enum EnumBoolean {
	True, False
}
