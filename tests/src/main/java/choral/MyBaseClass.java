package choral;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "MyBaseClass" )
class MyBaseClass< T > implements InterfaceA,InterfaceB {
	T baseMethod( T param ) {
		return param;
	}

}
