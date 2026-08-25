package Projector.ProjectionClashSameClass;

class ProjectionClashSameClass@( A, B ) {
    int@B result;

    void process( Integer@A value ) { result = 1@B; }
    void process( int@A value ) { result = 2@B; } //! different projected bodies
}
