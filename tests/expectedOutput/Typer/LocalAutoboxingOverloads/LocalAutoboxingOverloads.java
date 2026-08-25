package Typer.LocalAutoboxingOverloads;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "LocalAutoboxingOverloads" )
class LocalAutoboxingOverloads {
	void process( Object left, Object right ) {
		
	}
	
	void process( int left, int right ) {
		
	}
	
	void test() {
		process( Integer.valueOf( 2 ), Integer.valueOf( 2 ) );
		process( 2, 2 );
	}

}
