import java.util.List;
import java.util.Map;

import lsp.ChoralTextDocumentService;
import lsp.features.ChoreographyDiagramProvider;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class ChoralTextDocumentServiceTest {
    @Test
    public void acceptsPositionalCustomRequestParameters() {
        String uri = "file:///Example.ch";
        String source = """
                class Example@( A, B ) {
                    SymChannel@( A, B )< Object > c;
                    void run( String@A value ) { c.< String >com( value ); }
                }
                """;
        ChoralTextDocumentService service = new ChoralTextDocumentService();
        service.didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(uri, "choral", 1, source)));

        Object result = service.choreographyDiagram(List.of(Map.of(
                "textDocument", Map.of("uri", uri),
                "position", Map.of("line", 2, "character", 45)))).join();

        assertInstanceOf(ChoreographyDiagramProvider.Diagram.class, result);
    }
}
