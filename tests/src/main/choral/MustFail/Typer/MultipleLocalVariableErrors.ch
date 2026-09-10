package Typer.MultipleLocalVariableErrors;

class MultipleLocalVariableErrors@( A ) {
	void method() {
		int@A first = "first"@A, second = "second"@A; //! Required type 'int@(A)', found 'java.lang.String@(A)' //! Required type 'int@(A)', found 'java.lang.String@(A)'
	}
}
