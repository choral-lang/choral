package Typer.ProjectionDistinctOverloads;

class ProjectionDistinctOverloads@( A, B ) {
    void reference( Integer@A value ) {}
    void reference( Integer@B value ) {}

    void primitive( int@A value ) {}
    void primitive( int@B value ) {}
}
