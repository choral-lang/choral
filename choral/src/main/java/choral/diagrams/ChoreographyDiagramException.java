package choral.diagrams;

/** Indicates that a choreography diagram could not be produced from the supplied source. */
public final class ChoreographyDiagramException extends RuntimeException {
    public enum Reason {
        PARSE_ERROR,
        TYPE_ERROR,
        NO_SYMBOL
    }

    private final Reason reason;

    public ChoreographyDiagramException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public ChoreographyDiagramException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }
}
