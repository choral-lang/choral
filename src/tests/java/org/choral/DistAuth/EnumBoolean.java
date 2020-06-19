package org.choral.DistAuth;
import org.choral.runtime.Serializers.KryoSerializable;
import org.choral.annotations.Choreography;

@KryoSerializable
@Choreography( role = "A", name = "EnumBoolean" )
public enum EnumBoolean {
	True, False
}
