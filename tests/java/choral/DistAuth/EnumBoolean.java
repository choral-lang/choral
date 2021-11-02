package choral.DistAuth;
import choral.runtime.Serializers.KryoSerializable;
import choral.annotations.Choreography;

@KryoSerializable
@Choreography( role = "A", name = "EnumBoolean" )
public enum EnumBoolean {
	True, False
}
