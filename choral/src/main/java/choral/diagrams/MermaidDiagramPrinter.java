package choral.diagrams;

import choral.diagrams.ChoreographyDiagram.Branch;
import choral.diagrams.ChoreographyDiagram.Event;
import choral.diagrams.ChoreographyDiagram.Group;
import choral.diagrams.ChoreographyDiagram.Message;
import choral.diagrams.ChoreographyDiagram.Participant;

import java.util.ArrayList;
import java.util.List;

/** Renders choreography diagrams as Mermaid sequence diagrams. */
public final class MermaidDiagramPrinter implements ChoreographyDiagramPrinter {
    @Override
    public String format() {
        return "mermaid";
    }

    @Override
    public String print(ChoreographyDiagram diagram) {
        List<String> lines = new ArrayList<>();
        lines.add("sequenceDiagram");
        for (Participant participant : diagram.participants)
            lines.add("participant " + participantId(participant.id()) + " as "
                    + escapeMermaid(participant.label()));
        renderEvents(lines, diagram.events, "");
        return String.join("\n", lines);
    }

    private static void renderEvents(List<String> lines, List<Event> events, String indentation) {
        for (Event event : events) {
            if (event instanceof Message message) {
                String arrow = "selection".equals(message.kind()) ? "-->>" : "->>";
                lines.add(indentation + participantId(message.from()) + arrow + participantId(message.to())
                        + ": " + escapeMermaid(message.label()));
                continue;
            }
            if (!(event instanceof Group group))
                throw new IllegalArgumentException("Unsupported choreography event: " + event.getClass().getName());
            if ("alt".equals(group.kind())) {
                List<Branch> branches = group.branches() == null ? List.of() : group.branches();
                for (int index = 0; index < branches.size(); index++) {
                    Branch branch = branches.get(index);
                    lines.add(indentation + (index == 0 ? "alt" : "else") + " "
                            + escapeMermaid(branch.label()));
                    renderEvents(lines, branch.events(), indentation + "\t");
                }
                if (branches.isEmpty())
                    lines.add(indentation + "alt " + escapeMermaid(group.label()));
                lines.add(indentation + "end");
                continue;
            }
            lines.add(indentation + group.kind() + " " + escapeMermaid(group.label()));
            renderEvents(lines, group.events() == null ? List.of() : group.events(), indentation + "\t");
            lines.add(indentation + "end");
        }
    }

    private static String participantId(String value) {
        return "p_" + value.replaceAll("[^A-Za-z0-9_]", "_");
    }

    private static String escapeMermaid(String value) {
        String escaped = value.replaceAll("[\\n\\r]+", " ")
                .replaceAll("[:{};]", " ")
                .strip();
        return escaped.isEmpty() ? " " : escaped;
    }
}
