package choral.DistAuth;

import choral.annotations.Choreography;
import choral.runtime.Serializers.KryoSerializable;

@KryoSerializable
@Choreography( role = "A", name = "EnumBoolean" )
public enum EnumBoolean {
	True,
	False
}
