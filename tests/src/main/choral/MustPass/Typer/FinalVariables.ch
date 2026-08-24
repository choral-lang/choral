package Typer.FinalVariables;

class FinalVariables@( A ) {
	void run( final Integer@A parameter ) {
		final Integer@A x = 5@A;
		Integer@A y = x;
		y += 1@A;
		System@A.out.println( parameter );
	}
}
