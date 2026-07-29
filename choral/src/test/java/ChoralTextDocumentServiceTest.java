import java.util.List;
import java.util.Map;

import lsp.ChoralTextDocumentService;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class ChoralTextDocumentServiceTest {
    @Test
    public void returnsMermaidForPositionalCustomRequestParameters() {
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

        assertEquals(
                """
                sequenceDiagram
                participant p_A as A
                participant p_B as B
                p_A->>p_B: com
                """.strip(),
                result);
    }

    @Test
    public void ownsChoreographyRequestErrorResults() {
        ChoralTextDocumentService service = new ChoralTextDocumentService();

        ChoralTextDocumentService.ChoreographyDiagramErrorResult result = assertInstanceOf(
                ChoralTextDocumentService.ChoreographyDiagramErrorResult.class,
                service.choreographyDiagram(Map.of()).join());

        assertEquals("invalidParams", result.error.code());
    }
}
