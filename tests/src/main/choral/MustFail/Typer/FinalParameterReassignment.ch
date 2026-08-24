package Typer.FinalParameterReassignment;

class FinalParameterReassignment@( A ) {
	void run( final Integer@A x ) {
		x += 1@A; //! Cannot assign a value to final variable 'x'.
	}
}
