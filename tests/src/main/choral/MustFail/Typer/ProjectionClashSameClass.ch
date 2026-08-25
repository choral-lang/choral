package Typer.ProjectionClashSameClass;

class ProjectionClashSameClass@( A, B ) {
    void process( Integer@A value ) {}
    void process( int@A value ) {} //! same projected erasure
}
