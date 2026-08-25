package Typer.ProjectionClashMultipleParameters;

class ProjectionClashMultipleParameters@( A, B ) {
    void process( Object@A left, Object@A right ) {}
    void process( int@A left, int@A right ) {} //! same projected erasure
}
