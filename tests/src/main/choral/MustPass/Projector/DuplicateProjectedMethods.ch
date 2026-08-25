package Projector.DuplicateProjectedMethods;

class DuplicateProjectedMethods@( A, B ) {
    Integer@A identity( Integer@A value ) { return value; }
    int@A identity( int@A value ) { return value; }

    void accept( Object@A left, Object@A right ) {}
    void accept( int@A left, int@A right ) {}
}
