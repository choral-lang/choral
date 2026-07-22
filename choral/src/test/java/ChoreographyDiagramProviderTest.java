import lsp.features.ChoreographyDiagramProvider;
import org.eclipse.lsp4j.Position;
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

        Object result = new ChoreographyDiagramProvider().diagram(source, new Position(3, 20));
        ChoreographyDiagramProvider.Diagram diagram = assertInstanceOf(
                ChoreographyDiagramProvider.Diagram.class, result);

        assertEquals(1, diagram.version);
        assertEquals("Example", diagram.symbol.name());
        assertEquals(2, diagram.participants.size());
        assertEquals(2, diagram.events.size());
        assertEquals("message", diagram.events.get(0).kind());
        assertEquals("Buyer", diagram.events.get(0).from());
        assertEquals("Seller", diagram.events.get(0).to());
        assertEquals("selection", diagram.events.get(1).kind());
        assertEquals("Seller", diagram.events.get(1).from());
        assertEquals("Buyer", diagram.events.get(1).to());
    }
}
