package Projector.DuplicateProjectedMethods;

import choral.annotations.Choreography;
import choral.lang.Unit;

@Choreography( role = "B", name = "DuplicateProjectedMethods" )
class DuplicateProjectedMethods_B {
	Unit identity( Unit value ) {
		return identity();
	}
	
	void accept( Unit left, Unit right ) {
		accept();
	}
	
	Unit identity() {
		return Unit.id;
	}
	
	void accept() {
		
	}

}
