package choral.diagrams;

import java.util.ArrayList;
import java.util.List;

/** Format-neutral model of a choreography diagram. */
public final class ChoreographyDiagram {
    public final int version = 1;
    public Symbol symbol;
    public final List<Participant> participants = new ArrayList<>();
    public final List<Event> events = new ArrayList<>();

    public record Symbol(String name, Range range) {
    }

    public record Participant(String id, String label) {
    }

    public interface Event {
        String kind();

        Range range();
    }

    public record Message(String kind, String from, String to, String label, Range range) implements Event {
    }

    public record Group(String kind, String label, List<Branch> branches, List<Event> events,
                        Range range) implements Event {
    }

    public record Branch(String label, List<Event> events) {
    }

    /** Zero-based source position, matching the coordinates used by the diagram protocol. */
    public record Position(int line, int character) {
    }

    public record Range(Position start, Position end) {
    }
}
