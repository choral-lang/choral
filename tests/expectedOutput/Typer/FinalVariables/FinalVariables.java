package Typer.FinalVariables;

import choral.annotations.Choreography;

@Choreography( role = "A", name = "FinalVariables" )
class FinalVariables {
	void run( final Integer parameter ) {
		final Integer x = 5;
		Integer y = x;
		y += 1;
		System.out.println( parameter );
	}

}
