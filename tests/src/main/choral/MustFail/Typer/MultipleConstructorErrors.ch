package Typer.MultipleConstructorErrors;

class MultipleConstructorErrors@( A ) {
	MultipleConstructorErrors( Object first ) {} //! Data type expected
	MultipleConstructorErrors( Object second, int@A discriminator ) {} //! Data type expected
}
