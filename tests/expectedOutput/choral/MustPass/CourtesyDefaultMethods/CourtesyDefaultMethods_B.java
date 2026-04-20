package choral.MustPass.CourtesyDefaultMethods;

import choral.annotations.Choreography;
import choral.lang.Unit;

@Choreography( role = "B", name = "CourtesyDefaultMethods" )
public interface CourtesyDefaultMethods_B {
	default void m( Unit x, Integer y, Unit z ) {
		m( y );
	}
	
	default void m( Integer y ) {
		
	}
}
