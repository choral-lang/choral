package Misc.CourtesyDefaultMethods;

import choral.annotations.Choreography;
import choral.lang.Unit;

@Choreography( role = "C", name = "CourtesyDefaultMethods" )
public interface CourtesyDefaultMethods_C {
	default void m( Unit x, Unit y, Integer z ) {
		m( z );
	}
	
	default void m( Integer z ) {
		
	}
}
