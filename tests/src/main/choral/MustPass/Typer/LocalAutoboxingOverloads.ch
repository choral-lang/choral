package Typer.LocalAutoboxingOverloads;

class LocalAutoboxingOverloads@( A ) {
    void process( Object@A left, Object@A right ) {}
    void process( int@A left, int@A right ) {}

    void test() {
        process( Integer@A.valueOf( 2@A ), Integer@A.valueOf( 2@A ) );
        process( 2@A, 2@A );
    }
}
