package choral.diagrams;

/** Renders a neutral choreography diagram in a specific textual format. */
public interface ChoreographyDiagramPrinter {
    String format();

    String print(ChoreographyDiagram diagram);
}
