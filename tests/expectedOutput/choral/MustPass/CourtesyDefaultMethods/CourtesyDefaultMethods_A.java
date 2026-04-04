package choral.MustPass.CourtesyDefaultMethods;

import choral.annotations.Choreography;
import choral.lang.Unit;

@Choreography( role = "A", name = "CourtesyDefaultMethods" )
public interface CourtesyDefaultMethods_A {
	default void m( Integer x, Unit y, Unit z ) {
		m( x );
	}
	
	default void m( Integer x ) {
		
	}
}
