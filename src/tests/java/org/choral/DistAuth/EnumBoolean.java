package org.choral.DistAuth;
import org.choral.annotations.Choreography;
import org.choral.runtime.Serializers.KryoSerializable;

@KryoSerializable
@Choreography( role = "A", name = "EnumBoolean" )
public enum EnumBoolean {
	True, False
}
