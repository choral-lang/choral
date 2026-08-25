package Projector.ProjectionClashMultipleParameters;

class ProjectionClashMultipleParameters@( A, B ) {
    int@B result;

    void process( Object@A left, Object@A right ) { result = 1@B; }
    void process( int@A left, int@A right ) { result = 2@B; } //! different bodies
}
