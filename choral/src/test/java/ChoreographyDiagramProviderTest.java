import choral.diagrams.ChoreographyDiagram;
import choral.diagrams.ChoreographyDiagram.Message;
import choral.diagrams.ChoreographyDiagram.Position;
import choral.diagrams.ChoreographyDiagramProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class ChoreographyDiagramProviderTest {
    @Test
    public void producesParticipantsAndChannelEvents() {
        String source = """
                class Example@( Seller, Buyer ) {
                    SymChannel@( Seller, Buyer )< Object > c;
                    void run( String@Buyer book ) {
                        String@Seller title = c.< String >com( book );
                        c.< Flag >select( Flag@Seller.Yes );
                    }
                }
                """;

        ChoreographyDiagram diagram = new ChoreographyDiagramProvider().diagram(
                source, new Position(3, 20));
        Message message = assertInstanceOf(Message.class, diagram.events.get(0));
        Message selection = assertInstanceOf(Message.class, diagram.events.get(1));

        assertEquals(1, diagram.version);
        assertEquals("Example", diagram.symbol.name());
        assertEquals(2, diagram.participants.size());
        assertEquals(2, diagram.events.size());
        assertEquals("message", message.kind());
        assertEquals("Buyer", message.from());
        assertEquals("Seller", message.to());
        assertEquals("selection", selection.kind());
        assertEquals("Seller", selection.from());
        assertEquals("Buyer", selection.to());
    }
}
