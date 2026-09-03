package Typer.FinalVarReassignment;

class FinalVarReassignment@( A ) {
	void run() {
		final var x = 5@A;
		x += 1@A; //! Cannot assign a value to final variable 'x'.
	}
}
