package Projector.DuplicateProjectedMethods;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "DuplicateProjectedMethods" )
class DuplicateProjectedMethods_A {
	Integer identity( Integer value ) {
		return value;
	}
	
	int identity( int value ) {
		return value;
	}
	
	void accept( Object left, Object right ) {
		
	}
	
	void accept( int left, int right ) {
		
	}

}
