package Typer.FinalLocalVariableReassignment;

class FinalLocalVariableReassignment@( A ) {
	void run() {
		final Integer@A x = 5@A;
		x += 1@A; //! Cannot assign a value to final variable 'x'.
	}
}
