package Typer.ProjectionDistinctOverloads;

import choral.annotations.Choreography;
import choral.lang.Unit;

@Choreography( role = "A", name = "ProjectionDistinctOverloads" )
class ProjectionDistinctOverloads_A {
	void reference( Integer value ) {
		
	}
	
	void reference( Unit value ) {
		reference();
	}
	
	void primitive( int value ) {
		
	}
	
	void primitive( Unit value ) {
		primitive();
	}
	
	void reference() {
		
	}
	
	void primitive() {
		
	}

}
