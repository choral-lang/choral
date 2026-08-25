package Typer.ProjectionDistinctOverloads;

import choral.annotations.Choreography;
import choral.lang.Unit;

@Choreography( role = "B", name = "ProjectionDistinctOverloads" )
class ProjectionDistinctOverloads_B {
	void reference( Unit value ) {
		reference();
	}
	
	void reference( Integer value ) {
		
	}
	
	void primitive( Unit value ) {
		primitive();
	}
	
	void primitive( int value ) {
		
	}
	
	void reference() {
		
	}
	
	void primitive() {
		
	}

}
